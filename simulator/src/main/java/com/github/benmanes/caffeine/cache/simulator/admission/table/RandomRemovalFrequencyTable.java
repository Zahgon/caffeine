/*
 * Copyright 2015 Gilga Einziger. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator.admission.table;

import java.util.Random;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Frequency;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

/**
 * A probabilistic multiset for estimating the popularity of an element within a time window. The
 * maximum frequency of an element. The size of the sample in relation to the cache size can be
 * controlled with a sample factor. Instead of halving the popularity of elements a random element
 * is dropped when table is full.
 * <p>
 * This class is used to check the feasibility of using TinyTable instead of CountMin Sketch.
 *
 * @author gilg1983@gmail.com (Gil Einziger)
 */
public final class RandomRemovalFrequencyTable implements Frequency {

    /**
     * controls both the max count and how many items are remembered (the sum)
     */
    private static final int sampleFactor = 8;

    /**
     * a placeholder for TinyTable
     */
    private final Long2IntMap table;

    /**
     * used to drop items at random
     */
    private final Random random;

    /**
     * sum of total items
     */
    private final int maxSum;

    /**
     * total sum of stored items *
     */
    private int currSum;

    public RandomRemovalFrequencyTable(Config config) {
        var settings = new BasicSettings(config);
        maxSum = Math.toIntExact(sampleFactor * settings.maximumSize());
        random = new Random(settings.randomSeed());
        table = new Long2IntOpenHashMap(maxSum);
    }

    @Override
    public int frequency(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void increment(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
