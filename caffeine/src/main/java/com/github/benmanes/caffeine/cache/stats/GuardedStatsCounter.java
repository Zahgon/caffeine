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
package com.github.benmanes.caffeine.cache.stats;

import static java.util.Objects.requireNonNull;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import com.github.benmanes.caffeine.cache.RemovalCause;

/**
 * A {@link StatsCounter} implementation that suppresses and logs any exception thrown by the
 * delegate <code>statsCounter</code>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class GuardedStatsCounter implements StatsCounter {

    static final Logger logger = System.getLogger(GuardedStatsCounter.class.getName());

    final StatsCounter delegate;

    GuardedStatsCounter(StatsCounter delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void recordHits(int count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void recordMisses(int count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void recordLoadFailure(long loadTime) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void recordEviction(int weight, RemovalCause cause) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheStats snapshot() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
