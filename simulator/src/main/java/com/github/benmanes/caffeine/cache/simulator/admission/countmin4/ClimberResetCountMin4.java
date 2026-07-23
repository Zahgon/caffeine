/*
 * Copyright 2017 Gilga Einziger. All Rights Reserved.
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

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.membership.Membership;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;

/**
 * A sketch where the aging process is a dynamic process and adjusts to the
 * recency/frequency bias of the actual workload.
 *
 * @author gilga1983@gmail.com (Gil Einziger)
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class ClimberResetCountMin4 extends CountMin4 {

    static final long ONE_MASK = 0x1111111111111111L;

    final Membership doorkeeper;

    int additions;

    int period;

    // misses in previous interval
    int prevMisses;

    // misses in this interval
    int misses;

    // are we increasing the 'step size' or decreasing it
    int direction = 1;

    // events yet to count before we make a decision.
    int eventsToCount;

    public ClimberResetCountMin4(Config config) {
        super(config);
        var settings = new BasicSettings(config);
        doorkeeper = settings.tinyLfu().countMin4().periodic().doorkeeper().enabled() ? settings.membership().filter().create(config) : Membership.disabled();
    }

    @Override
    protected void ensureCapacity(long maximumSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int frequency(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void increment(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    protected void tryReset(boolean added) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void reportMiss() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unused")
    public int getStep() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void setStep(int x) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getEventsToCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void resetEventsToCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getPeriod() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
