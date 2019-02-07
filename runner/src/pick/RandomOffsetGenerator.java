/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.runner.pick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.stream.Stream;

/**
 *
 */
public class RandomOffsetGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(RandomOffsetGenerator.class);
    public static Stream<Integer> generate(Random rand, int offsetBound) {

        HashSet<Object> previousRandomOffsets = new HashSet<>();

        return Stream.generate(() -> {

            boolean foundUnique = false;

            int nextChoice = 0;
            while (!foundUnique) {
                nextChoice = rand.nextInt(offsetBound);
                LOG.info("RandomOffsetGenerator.nextChoice (nextInt) [nextBound]: " + nextChoice + "[" + offsetBound  +"]");
                foundUnique = previousRandomOffsets.add(nextChoice);
            }
            return nextChoice;
        }).limit(offsetBound);
    }
}
