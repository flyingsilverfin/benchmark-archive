package grakn.benchmark.runner.storage;

import grakn.core.client.Grakn;
import grakn.core.concept.ConceptId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class NotInRelationshipConceptIdPicker extends FromIdStoragePicker<ConceptId> {

    private static final Logger LOG = LoggerFactory.getLogger(NotInRelationshipConceptIdPicker.class);

    private String relationshipLabel;
    private String roleLabel;

    public NotInRelationshipConceptIdPicker(Random rand,
                                            IdStoreInterface conceptStore,
                                            String rolePlayerTypeLabel,
                                            String relationshipLabel,
                                            String roleLabel
                                            ) {
        super(rand, conceptStore, rolePlayerTypeLabel);
        this.typeLabel = rolePlayerTypeLabel;
        this.relationshipLabel = relationshipLabel;
        this.roleLabel = roleLabel;

    }

    @Override
    public Stream<ConceptId> getStream(Grakn.Transaction tx) {
        Stream<Integer> randomUniqueOffsetStream = this.getStreamOfRandomOffsets(tx);
        List<String> notInRelationshipConceptIds = conceptStore.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
        LOG.info("Ids not playing role " + roleLabel + ": " );
        LOG.info("\t" + String.join(", ", notInRelationshipConceptIds));
        return randomUniqueOffsetStream.map(randomOffset -> ConceptId.of(notInRelationshipConceptIds.get(randomOffset)));
    }

    @Override
    public Integer getConceptCount(Grakn.Transaction tx) {
        Integer count = conceptStore.numIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
        LOG.info("  Count for " + typeLabel + " not playing " + roleLabel + " in relationship: " + relationshipLabel + ": " +count);
//        count = Math.min(1, count);
        return count;
    }

}
