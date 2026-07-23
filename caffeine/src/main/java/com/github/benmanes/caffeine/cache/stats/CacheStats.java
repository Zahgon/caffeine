/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.errorprone.annotations.Immutable;

/**
 * Statistics about the performance of a {@link Cache}.
 * <p>
 * Cache statistics are incremented according to the following rules:
 * <ul>
 *   <li>When a cache lookup encounters an existing cache entry {@code hitCount} is incremented.
 *   <li>When a cache lookup first encounters a missing cache entry, a new entry is loaded.
 *   <ul>
 *     <li>After successfully loading an entry {@code missCount} and {@code loadSuccessCount} are
 *         incremented, and the total loading time, in nanoseconds, is added to
 *         {@code totalLoadTime}.
 *     <li>When an exception is thrown while loading an entry or if the loaded value is {code null},
 *         {@code missCount} and {@code loadFailureCount} are incremented, and the total loading
 *         time, in nanoseconds, is added to {@code totalLoadTime}.
 *     <li>Cache lookups that encounter a missing cache entry that is still loading will wait
 *         for loading to complete (whether successful or not) and then increment {@code missCount}.
 *   </ul>
 *   <li>When an entry is computed through the {@linkplain Cache#asMap asMap} the
 *       {@code loadSuccessCount} or {@code loadFailureCount} is incremented.
 *   <li>When an entry is evicted from the cache, {@code evictionCount} is incremented and the
 *       weight added to {@code evictionWeight}.
 *   <li>No stats are modified when a cache entry is invalidated or manually removed.
 *   <li>No stats are modified by non-computing operations invoked on the
 *       {@linkplain Cache#asMap asMap} view of the cache.
 * </ul>
 * <p>
 * A lookup is specifically defined as an invocation of one of the methods
 * {@link LoadingCache#get(Object)}, {@link Cache#get(Object, java.util.function.Function)}, or
 * {@link LoadingCache#getAll(Iterable)}.
 * <p>
 * This is a <em>value-based</em> class; use of identity-sensitive operations (including reference
 * equality ({@code ==}), identity hash code, or synchronization) on instances of {@code CacheStats}
 * may have unpredictable results and should be avoided.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@Immutable
@NullMarked
public final class CacheStats {

    private static final CacheStats EMPTY_STATS = CacheStats.of(0L, 0L, 0L, 0L, 0L, 0L, 0L);

    private final long hitCount;

    private final long missCount;

    private final long loadSuccessCount;

    private final long loadFailureCount;

    private final long totalLoadTime;

    private final long evictionCount;

    private final long evictionWeight;

    private CacheStats(long hitCount, long missCount, long loadSuccessCount, long loadFailureCount, long totalLoadTime, long evictionCount, long evictionWeight) {
        if ((hitCount < 0) || (missCount < 0) || (loadSuccessCount < 0) || (loadFailureCount < 0) || (totalLoadTime < 0) || (evictionCount < 0) || (evictionWeight < 0)) {
            throw new IllegalArgumentException();
        }
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.loadSuccessCount = loadSuccessCount;
        this.loadFailureCount = loadFailureCount;
        this.totalLoadTime = totalLoadTime;
        this.evictionCount = evictionCount;
        this.evictionWeight = evictionWeight;
    }

    public static CacheStats of(long hitCount, long missCount, long loadSuccessCount, long loadFailureCount, long totalLoadTime, long evictionCount, long evictionWeight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static CacheStats empty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long requestCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long hitCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double hitRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long missCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double missRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long loadCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long loadSuccessCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long loadFailureCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double loadFailureRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long totalLoadTime() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double averageLoadPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long evictionCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long evictionWeight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public CacheStats minus(CacheStats other) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public CacheStats plus(CacheStats other) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("ShortCircuitBoolean")
    static long saturatedAdd(long a, long b) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
