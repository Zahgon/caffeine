/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.admission.countmin4;

import static com.google.common.base.Preconditions.checkArgument;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Frequency;
import com.google.common.math.IntMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;

/**
 * A probabilistic multiset for estimating the popularity of an element within a time window. The
 * maximum frequency of an element is limited to 15 (4-bits) and extensions provide the aging
 * process.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("NotNullFieldNotInitialized")
public abstract class CountMin4 implements Frequency {

    static final long[] SEED = { // A mixture of seeds from FNV-1a, CityHash, and Murmur3
    0xc3a5c85c97cb3127L, 0xb492b66fbe98f273L, 0x9ae16a3b2f90404fL, 0xcbf29ce484222325L };

    static final long RESET_MASK = 0x7777777777777777L;

    protected final boolean conservative;

    protected int tableMask;

    protected long[] table;

    protected int step = 1;

    /**
     * Creates a frequency sketch that can accurately estimate the popularity of elements given
     * the maximum size of the cache.
     */
    @SuppressWarnings({ "NullAway.Init", "this-escape", "Varifier" })
    protected CountMin4(Config config) {
        var settings = new BasicSettings(config);
        conservative = settings.tinyLfu().conservative();
        double countersMultiplier = settings.tinyLfu().countMin4().countersMultiplier();
        long counters = (long) (countersMultiplier * settings.maximumSize());
        ensureCapacity(counters);
    }

    @SuppressWarnings("Varifier")
    protected void ensureCapacity(long maximumSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("Varifier")
    public int frequency(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void increment(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void regularIncrement(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void conservativeIncrement(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Performs the aging process after an addition to allow old entries to fade away.
     */
    protected abstract void tryReset(boolean added);

    @CanIgnoreReturnValue
    boolean incrementAt(int i, int j, long step) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    int indexOf(int item, int i) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    int spread(@Var int x) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
