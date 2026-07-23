/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
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

import static com.google.common.base.Preconditions.checkState;

/**
 * A skeleton for hill climbers that walk using the hit rate.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public abstract class AbstractClimber implements HillClimber {

    private static final boolean debug = false;

    protected int sampleSize;

    protected int hitsInMain;

    protected int hitsInWindow;

    protected int hitsInSample;

    protected int missesInSample;

    protected double previousHitRate;

    @Override
    public void onMiss(long key, boolean isFull) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void onHit(long key, QueueType queueType, boolean isFull) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Adaptation adapt(double windowSize, double probationSize, double protectedSize, boolean isFull) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Returns the amount to adapt by.
     */
    protected abstract double adjust(double hitRate);

    protected void resetSample(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
