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
package com.github.benmanes.caffeine.cache;

import static com.github.benmanes.caffeine.cache.Caffeine.ceilingPowerOfTwo;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

/**
 * A pacing scheduler that prevents executions from happening too frequently. Only one task may be
 * scheduled at any given time, the earliest pending task takes precedence, and the delay may be
 * increased if it is less than a tolerance threshold.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class Pacer {

    // 1.07s
    static final long TOLERANCE = ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1));

    final Scheduler scheduler;

    long nextFireTime;

    @Nullable
    Future<?> future;

    Pacer(Scheduler scheduler) {
        this.scheduler = requireNonNull(scheduler);
    }

    public void schedule(Executor executor, Runnable command, long now, long delay) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void cancel() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isScheduled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean maySkip(long scheduleAt) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long calculateSchedule(long now, long delay, long scheduleAt) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
