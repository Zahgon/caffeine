/*
 * Copyright 2019 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.policy;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.jspecify.annotations.Nullable;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The key and metadata for accessing a cache.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@Immutable
public class AccessEvent {

    private final long key;

    private AccessEvent(long key) {
        this.key = key;
    }

    public long key() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Long longKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int weight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double hitPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double missPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isPenaltyAware() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public boolean equals(@Nullable Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static AccessEvent forKey(long key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static AccessEvent forKeyAndWeight(long key, int weight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static AccessEvent forKeyAndPenalties(long key, double hitPenalty, double missPenalty) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private static final class LongInterner {

        static final AtomicReferenceArray<@Nullable Long> cache = new AtomicReferenceArray<>(1 << 20);

        static final int MASK = cache.length() - 1;

        static Long boxed(long l) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    private static final class WeightedAccessEvent extends AccessEvent {

        private final int weight;

        WeightedAccessEvent(long key, int weight) {
            super(key);
            this.weight = weight;
            checkArgument(weight >= 0);
        }

        @Override
        public int weight() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    private static final class PenaltiesAccessEvent extends AccessEvent {

        private final double missPenalty;

        private final double hitPenalty;

        PenaltiesAccessEvent(long key, double hitPenalty, double missPenalty) {
            super(key);
            this.hitPenalty = hitPenalty;
            this.missPenalty = missPenalty;
            checkArgument(hitPenalty >= 0);
            checkArgument(missPenalty >= hitPenalty);
        }

        @Override
        public double missPenalty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public double hitPenalty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean isPenaltyAware() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
