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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A hill climbing algorithm to tune the admission window size.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public interface HillClimber {

    /**
     * Records that a hit occurred with a full cache.
     *
     * @param key the key accessed
     * @param queue the queue the entry was found in
     * @param isFull if the cache is fully populated
     */
    void onHit(long key, QueueType queue, boolean isFull);

    /**
     * Records that a miss occurred with a full cache.
     *
     * @param key the key accessed
     * @param isFull if the cache is fully populated and had to evict
     */
    void onMiss(long key, boolean isFull);

    /**
     * Determines how to adapt the segment sizes.
     *
     * @param windowSize the current window size
     * @param probationSize the current probation size
     * @param protectedSize the current protected size
     * @param isFull if the cache is fully populated
     * @return the adjustment to the segments
     */
    Adaptation adapt(double windowSize, double probationSize, double protectedSize, boolean isFull);

    enum QueueType {

        WINDOW, PROBATION, PROTECTED
    }

    /**
     * The adaptation type and its magnitude.
     */
    record Adaptation(double amount, Type type) {

        private static final Adaptation HOLD = new Adaptation(0, Type.HOLD);

        public Adaptation {
            checkArgument(amount >= 0, "Step size %s must be positive", amount);
        }

        public static Adaptation adaptBy(double amount) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public static Adaptation hold() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public static Adaptation increaseWindow(double amount) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public static Adaptation decreaseWindow(double amount) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public enum Type {

            HOLD, INCREASE_WINDOW, DECREASE_WINDOW
        }
    }
}
