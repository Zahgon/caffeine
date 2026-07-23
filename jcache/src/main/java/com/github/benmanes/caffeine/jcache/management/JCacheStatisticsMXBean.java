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
package com.github.benmanes.caffeine.jcache.management;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import javax.cache.management.CacheStatisticsMXBean;

/**
 * Caffeine JCache statistics.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("IdentifierName")
public final class JCacheStatisticsMXBean implements CacheStatisticsMXBean {

    private final LongAdder puts = new LongAdder();

    private final LongAdder hits = new LongAdder();

    private final LongAdder misses = new LongAdder();

    private final LongAdder removals = new LongAdder();

    private final LongAdder evictions = new LongAdder();

    private final LongAdder putTimeNanos = new LongAdder();

    private final LongAdder getTimeNanos = new LongAdder();

    private final LongAdder removeTimeNanos = new LongAdder();

    private volatile boolean enabled;

    public boolean isEnabled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void enable(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCacheHits() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public float getCacheHitPercentage() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordHits(long count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCacheMisses() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public float getCacheMissPercentage() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordMisses(long count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCacheGets() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCachePuts() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordPuts(long count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCacheRemovals() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordRemovals(long count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long getCacheEvictions() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordEvictions(long count) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public float getAverageGetTime() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordGetTime(long durationNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public float getAveragePutTime() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordPutTime(long durationNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public float getAverageRemoveTime() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordRemoveTime(long durationNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private static float average(long requestCount, long opsTimeNanos) {
        if ((requestCount == 0) || (opsTimeNanos == 0)) {
            return 0;
        }
        long opsTimeMicro = TimeUnit.NANOSECONDS.toMicros(opsTimeNanos);
        return (float) opsTimeMicro / requestCount;
    }
}
