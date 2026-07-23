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

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.membership.Membership;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;

/**
 * A sketch where the aging process is a periodic reset.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class PeriodicResetCountMin4 extends CountMin4 {

    static final long ONE_MASK = 0x1111111111111111L;

    final Membership doorkeeper;

    int additions;

    int period;

    public PeriodicResetCountMin4(Config config) {
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
}
