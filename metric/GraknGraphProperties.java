package grakn.benchmark.metric;

import grakn.core.GraknTxType;
import grakn.core.Keyspace;
import grakn.core.client.Grakn;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.graql.ComputeQuery;
import grakn.core.graql.GetQuery;
import grakn.core.graql.Syntax;
import grakn.core.graql.VarPattern;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.graql.answer.ConceptSetMeasure;
import grakn.core.graql.answer.Value;
import grakn.core.util.SimpleURI;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static grakn.core.graql.Graql.var;

public class GraknGraphProperties implements GraphProperties {

    Grakn client;
    Grakn.Session session;

    public GraknGraphProperties(String uri, String keyspace) {
        this.client = new Grakn(new SimpleURI(uri));
        this.session = client.session(Keyspace.of(keyspace));
    }

    private Grakn.Transaction getTx(boolean useWriteTx) {
        if (useWriteTx) {
            return session.transaction(GraknTxType.WRITE);
        } else {
            return session.transaction(GraknTxType.READ);
        }
    }

    @Override
    public long maxDegreePresent() {
        // TODO do we need inference here?
        try (Grakn.Transaction tx = getTx(false)) {
            ComputeQuery<ConceptSetMeasure> query = tx.graql().compute(Syntax.Compute.Method.CENTRALITY).of("vertex");
            return query.stream().
                    map(conceptSetMeasure -> conceptSetMeasure.measurement().longValue()).
                    max(Comparator.naturalOrder())
                    .orElse(0l);
        }
    }

    @Override
    public List<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean edgeCardinalitesGreaterThanOne) {
        List<Pair<Set<String>, Set<String>>> edgePairs;


        // TODO do we need inference here?
        try (Grakn.Transaction tx = getTx(false)) {

            // `match $r1 ($x) isa edge; $r2 ($x) isa edge; $r1 != $r2; get $r1, $r2;`
            GetQuery query = tx.graql().match(
                    var("r1").isa("relationship").rel(var("x")),
                    var("r2").isa("relationship").rel(var("x")),
                    var("r1").neq(var("r2")),
                    var("x").isa("entity")
            ).get(var("r1"), var("r2"));

            Stream<Pair<Set<String>, Set<String>>> edgePairsStream= query.stream().map(
                    conceptMap -> {
                        Concept r1 = conceptMap.get("r1");
                        Concept r2 = conceptMap.get("r2");

                        // retrieve all the entities attached to r1, since r1 may be a hyperedge
                        Set<String> edge1 = r1.asRelationship()
                                .rolePlayers()
                                .filter(thing -> thing.isEntity())
                                .map(thing -> thing.id().toString())
                                .collect(Collectors.toSet());

                        // retrieve all the entities attached to r2, since r2 may be a hyperedge
                        Set<String> edge2 = r2.asRelationship()
                                .rolePlayers()
                                .filter(thing -> thing.isEntity())
                                .map(thing -> thing.id().toString())
                                .collect(Collectors.toSet());

                        return new Pair<>(edge1, edge2);
                    }
            );

            if (edgeCardinalitesGreaterThanOne) {
                // filter out edge pairs that don't touch at least 3 vertices (for instance)
                // we use a slightly stronger condition: each edge needs to touch more than 1 vertex each
                edgePairsStream = edgePairsStream.filter(
                        pair -> (pair.getFirst().size() > 1 && pair.getSecond().size() > 1)
                );
            }
            edgePairs = edgePairsStream.collect(Collectors.toList());
        }
        return edgePairs;
    }

    @Override
    public List<Pair<Long, Long>> connectedVertexDegrees() {
        List<Pair<Long, Long>> connectedVertexDegrees;

        // TODO do we need inference enabled here?
        try (Grakn.Transaction tx = getTx(false)) {
            // compute degree of each entity
            // compute degree of each  entitiy
            // returns mapping { deg_n : set(entity ids) }
            // does NOT return degree 0 entity IDs
            ComputeQuery<ConceptSetMeasure> computeQuery = tx.graql().compute(Syntax.Compute.Method.CENTRALITY).of("entity");

            // create a mapping from ID -> degree (not containing 0 degree entities)
            Map<String, Long> entityDegreeMap = computeQuery.stream()
                    .map(conceptSetMeasure ->
                            conceptSetMeasure.set().stream()
                                .map(conceptId -> new Pair<>(conceptId.toString(), conceptSetMeasure.measurement().longValue()))
                    )
                    .flatMap(e->e)
                    .collect(Collectors.toMap(pair->pair.getFirst(), pair->pair.getSecond()));

            // query for all connected entities, which by definition never have degree 0
            GetQuery allEntitiesQuery = tx.graql().match(
                    var("x").isa("entity"),
                    var("y").isa("entity"),
                    var().isa("relationship").rel(var("x")).rel(var("y"))
            ).get("x", "y");

            connectedVertexDegrees = allEntitiesQuery.stream()
                    .map(conceptMap -> new Pair<>(
                                entityDegreeMap.get(conceptMap.get("x").id().toString()),
                                entityDegreeMap.get(conceptMap.get("y").id().toString())
                            )
                    )
            .collect(Collectors.toList());
        }
        return connectedVertexDegrees;
    }

    @Override
    public List<Long> vertexDegree(int hyperedgeCardinality) {

        List<Long> vertexDegrees = new LinkedList<>();

        // TODO do we need inference enabled here?
        try (Grakn.Transaction tx = getTx(false)) {

            // since we want to restrict to a certain cardinality, we can't use compute
            // dynamically build a match finding relationships with a specific number of role players

            VarPattern vertex = var("start").isa("entity");
            VarPattern relationshipPattern = var("r").isa("relationship").rel("start");
            // enforce a specific number of endpoints of the relationship
            // one is required to be related to the vertex
            for (int i = 0; i < hyperedgeCardinality - 1; i++) {
                relationshipPattern = relationshipPattern.rel(var("end" + i));
            }

            GetQuery getQuery = tx.graql().match(vertex, relationshipPattern).get("start","r");
            // convert stream of (x, n-ary relationship) into map<x, set<n-ary relationship>>
            Map<Concept, Set<Concept>> entityRelationships = getQuery.stream()
                    .collect(Collectors.toMap(
                            conceptMap -> conceptMap.get("start"),
                            conceptMap -> Stream.of(conceptMap.get("r")).collect(Collectors.toSet()),
                            (relSet1, relSet2) -> Stream.concat(relSet1.stream(), relSet2.stream()).collect(Collectors.toSet())
                    ));

            // count how many times each relationship we retrieved contributes to
            // each attached entity's degree
            for (Map.Entry<Concept, Set<Concept>> entry : entityRelationships.entrySet()) {
                long count = 0;
                ConceptId startEntityId = entry.getKey().id();
                for (Concept relationship : entry.getValue()) {
                    List<ConceptId> endpointIds = relationship.asRelationship()
                            .rolePlayers()
                            .map(thing -> thing.id())
                            .collect(Collectors.toList());
                    // count how many times this relationship touches the starting entity
                    // it could be more than once, since we allow the same entity to play roles multiple times


                    /**
                     *
                     * TODO
                     * ****** ISSUES *****
                     *
                     * currently, things playing the same role multiples times are only returned ONCE, so we can't
                     * count the degree of an entity with respect to that relationship this way!!!
                     * Doesn't work with rolePlayerMap or rolePlayers
                     *
                     * Other major issue:
                     * querying as above for `match $r ($x, $y) isa  relationship; get $r`
                     * Breaks down hyper-relationships into binary relationships!!!!
                     * Need a better way to restrict to n-ary relationships in general, rather than having
                     * grakn return the >n -nary relationships in all combinations
                     *
                     */





                    count += endpointIds.stream().filter(conceptId -> conceptId.equals(startEntityId)).count();
                }
                // save this vertex's degree when counting only n-nary degrees (== hyperedgeCardinality)
                vertexDegrees.add(count);
            }

            int numNonzeroDegrees = vertexDegrees.size();

            // compute how many vertices have zero degree
            long numZeroDegrees = numVertices() - numNonzeroDegrees;

            // add that many zeros to the end of the list
            vertexDegrees.addAll(LongStream.range(0, numZeroDegrees).map(i->0l).boxed().collect(Collectors.toList()));
        }

        return vertexDegrees;
    }


    public long numVertices() {
        // TODO need inference enabled here?
        try (Grakn.Transaction tx = getTx(false)) {
            // count how many times to return `0` degree
            // compute this as (total number of entities) - (number we have already accounted for above)
            List<Value> conceptCounts = tx.graql().compute(Syntax.Compute.Method.COUNT).in("entity").execute();
            return conceptCounts.get(0).asValue().number().longValue();
        }
    }

    @Override
    public long maxAllowedDegree(int hyperedgeCardinality) {
        long numEntities = numVertices();
//        if (allowLoopEdges) {
            long maxAllowedDegree = 0;
            // loop over how many roles are connected the chosen entity 'x'
            for (int i = 1; i < hyperedgeCardinality+1; i++) {
                // calculate how many degrees are contributed by an edge to 'x', and how many possible ways there are
                // for such an edge to exist
                maxAllowedDegree += i*CombinatoricsUtils.binomialCoefficient((int)numEntities - 1, hyperedgeCardinality - i);
            }
            return maxAllowedDegree;
//        } else {
//            // this is (|v|-1) choose (n-ary - 1)
//            return CombinatoricsUtils.binomialCoefficient((int)numEntities - 1, hyperedgeCardinality - 1);
//        }
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        Set<String> neighborIds = new HashSet<>();

        // TODO do we need inference enabled here?
        try (Grakn.Transaction tx = getTx(false)) {
            List<ConceptMap> neighbors = tx.graql()
                    .match(
                            var("x").id(ConceptId.of(vertexId)),
                            var("r").rel(var("x")).rel(var("y"))
                    ).get(var("y")).execute();

            for (ConceptMap conceptMap : neighbors) {
                neighborIds.add(conceptMap.get(var("y")).id().toString());
            }
        }
        return neighborIds;
    }
}
