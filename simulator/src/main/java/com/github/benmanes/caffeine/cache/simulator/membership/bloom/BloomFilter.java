/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.membership.bloom;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Arrays;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.membership.Membership;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;

/**
 * A Bloom filter is a space and time efficient probabilistic data structure that is used to test
 * whether an element is a member of a set. False positives are possible, but false negatives are
 * not. Elements can be added to the set, but not removed. The more elements that are added the
 * higher the probability of false positives. While risking false positives, Bloom filters have a
 * space advantage over other data structures for representing sets by not storing the items.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("NotNullFieldNotInitialized")
public final class BloomFilter implements Membership {

    static final long[] SEED = { // A mixture of seeds from FNV-1a, CityHash, and Murmur3
    0xc3a5c85c97cb3127L, 0xb492b66fbe98f273L, 0x9ae16a3b2f90404fL, 0xcbf29ce484222325L };

    // 64-bits
    static final int BITS_PER_LONG_SHIFT = 6;

    static final int BITS_PER_LONG_MASK = Long.SIZE - 1;

    int tableShift;

    long[] table;

    /**
     * Creates a lazily initialized membership sketch, requiring {@link #ensureCapacity} be called
     * when the expected number of insertions and the false positive probability have been determined.
     */
    @SuppressWarnings("NullAway.Init")
    public BloomFilter() {
    }

    /**
     * Creates a membership sketch based on the expected number of insertions and the false positive
     * probability.
     */
    public BloomFilter(Config config) {
        var settings = new BasicSettings(config).membership();
        ensureCapacity(settings.expectedInsertions(), settings.fpp());
    }

    @SuppressWarnings("Varifier")
    public void ensureCapacity(long expectedInsertions, double fpp) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean mightContain(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ShortCircuitBoolean")
    public boolean put(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    boolean setAt(int item, int seedIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    int spread(@Var int x) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static int seeded(int item, int i) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long bitmask(int hash) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
