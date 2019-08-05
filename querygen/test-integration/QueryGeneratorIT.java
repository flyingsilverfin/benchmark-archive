/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.Type;
import grakn.core.rule.GraknTestServer;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlQuery;
import graql.lang.statement.Variable;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryGeneratorIT {

    private static final String testKeyspace = "querygen_test";

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer(
            Paths.get("querygen/test-integration/conf/grakn.properties"),
            Paths.get("querygen/test-integration/conf/cassandra-embedded.yaml")
    );

    @BeforeClass
    public static void loadSchema() {
        Path path = Paths.get("querygen");
        GraknClient client = new GraknClient(server.grpcUri());
        GraknClient.Session session = client.session(testKeyspace);
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            List<String> lines = Files.readAllLines(Paths.get("querygen/test-integration/resources/schema.gql"));
            String graqlQuery = String.join("\n", lines);
            transaction.execute((GraqlQuery) Graql.parse(graqlQuery));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.close();
        client.close();
    }

    @Test
    public void queryGeneratorReturnsCorrectNumberOfQueries() {
        QueryGenerator queryGenerator = new QueryGenerator(server.grpcUri(), testKeyspace);
        int queriesToGenerate = 100;
        List<GraqlGet> queries = queryGenerator.generate(queriesToGenerate);
        assertEquals(queries.size(), queriesToGenerate);
        for (GraqlGet query : queries) {
            assertNotNull(query);
        }
    }


    /**
     * Test that a single new query is generated as a QueryBuilder
     * This query builder should have all the reserved vars mapped to a type
     */
    @Test
    public void newQueryIsReturnedAsBuilderWithAllVarsMapped() {
        // a empty queryGenerator
        QueryGenerator queryGenerator = new QueryGenerator(null, null);

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace);
             GraknClient.Transaction tx = session.transaction().write()) {

            // directly generate a new query which contains concepts bound to this tx
            QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

            int generatedVars = queryBuilder.nextVar;
            assertEquals(queryBuilder.variableTypeMap.size(), generatedVars);
        }
    }


    /**
     * QueryBuilder contains mappings from owner types
     */
    @Test
    public void ownedVariablesAreMappedToAttributeTypes() {
        QueryGenerator queryGenerator = new QueryGenerator(null, null);

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace);
             GraknClient.Transaction tx = session.transaction().write()) {

            // directly generate a new query which contains concepts bound to this tx
            QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

            for (Variable attributeOwned : queryBuilder.attributeOwnership.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())) {
                Type attributeOwnedType = queryBuilder.getType(attributeOwned);
                assertTrue(attributeOwnedType.isAttributeType());
            }
        }
    }

}
