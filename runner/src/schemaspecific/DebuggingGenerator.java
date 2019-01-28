package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;

import java.util.Random;

public class DebuggingGenerator implements SchemaSpecificDataGenerator {

    public DebuggingGenerator(Random random, ConceptStore storage) {
        // TODO
    }

    @Override
    public RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy() {
        return null;
    }

    @Override
    public ConceptStore getConceptStore() {
        return null;
    }
}
