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
package com.github.benmanes.caffeine.cache;

import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Async.AsyncEvictionListener;
import com.github.benmanes.caffeine.cache.Async.AsyncExpiry;
import com.github.benmanes.caffeine.cache.Async.AsyncRemovalListener;
import com.github.benmanes.caffeine.cache.Async.AsyncWeigher;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.ConcurrentStatsCounter;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.FormatMethod;

/**
 * A builder of {@link Cache}, {@link LoadingCache}, {@link AsyncCache}, and
 * {@link AsyncLoadingCache} instances having a combination of the following features:
 * <ul>
 *   <li>automatic loading of entries into the cache, optionally asynchronously
 *   <li>size-based eviction when a maximum is exceeded based on frequency and recency
 *   <li>time-based expiration of entries, measured since last access or last write
 *   <li>asynchronously refresh when the first stale request for an entry occurs
 *   <li>keys automatically wrapped in {@linkplain WeakReference weak} references
 *   <li>values automatically wrapped in {@linkplain WeakReference weak} or
 *       {@linkplain SoftReference soft} references
 *   <li>writes propagated to an external resource
 *   <li>notification of evicted (or otherwise removed) entries
 *   <li>accumulation of cache access statistics
 * </ul>
 * <p>
 * These features are all optional; caches can be created using all or none of them. By default,
 * cache instances created by {@code Caffeine} will not perform any type of eviction.
 * <p>
 * Usage example:
 * {@snippet class=com.github.benmanes.caffeine.cache.Snippets region=builder lang=java}
 * <p>
 * The returned cache is implemented as a hash table with similar performance characteristics to
 * {@link ConcurrentHashMap}. The {@code asMap} view (and its collection views) have <i>weakly
 * consistent iterators</i>. This means that they are safe for concurrent use, but if other threads
 * modify the cache after the iterator is created, it is undefined which of these changes, if any,
 * are reflected in that iterator. These iterators never throw
 * {@link ConcurrentModificationException}.
 * <p>
 * <b>Note:</b> By default, the returned cache uses equality comparisons (the
 * {@link Object#equals equals} method) to determine equality for keys or values. However, if
 * {@link #weakKeys} was specified, the cache uses identity ({@code ==}) comparisons instead for
 * keys. Likewise, if {@link #weakValues} or {@link #softValues} was specified, the cache uses
 * identity comparisons for values.
 * <p>
 * Entries are automatically evicted from the cache when any of
 * {@linkplain #maximumSize(long) maximumSize}, {@linkplain #maximumWeight(long) maximumWeight},
 * {@linkplain #expireAfter(Expiry) expireAfter}, {@linkplain #expireAfterWrite expireAfterWrite},
 * {@linkplain #expireAfterAccess expireAfterAccess}, {@linkplain #weakKeys weakKeys},
 * {@linkplain #weakValues weakValues}, or {@linkplain #softValues softValues} are requested.
 * <p>
 * If {@linkplain #maximumSize(long) maximumSize} or {@linkplain #maximumWeight(long) maximumWeight}
 * is requested, entries may be evicted on each cache modification.
 * <p>
 * If {@linkplain #expireAfter(Expiry) expireAfter},
 * {@linkplain #expireAfterWrite expireAfterWrite}, or
 * {@linkplain #expireAfterAccess expireAfterAccess} is requested, then entries may be evicted on
 * each cache modification, on occasional cache accesses, or on calls to {@link Cache#cleanUp}. A
 * {@linkplain #scheduler(Scheduler)} may be specified to provide prompt removal of expired entries
 * rather than waiting until activity triggers the periodic maintenance. Expired entries may be
 * counted by {@link Cache#estimatedSize()}, but will never be visible to read or write operations.
 * <p>
 * If {@linkplain #weakKeys weakKeys}, {@linkplain #weakValues weakValues}, or
 * {@linkplain #softValues softValues} are requested, it is possible for a key or value present in
 * the cache to be reclaimed by the garbage collector. Entries with reclaimed keys or values may be
 * removed from the cache on each cache modification, on occasional cache accesses, or on calls to
 * {@link Cache#cleanUp}; such entries may be counted in {@link Cache#estimatedSize()}, but will
 * never be visible to read or write operations.
 * <p>
 * Certain cache configurations will result in the accrual of periodic maintenance tasks that
 * will be performed during write operations, or during occasional read operations in the absence of
 * writes. The {@link Cache#cleanUp} method of the returned cache will also perform maintenance, but
 * calling it should not be necessary with a high-throughput cache. Only caches built with
 * {@linkplain #maximumSize maximumSize}, {@linkplain #maximumWeight maximumWeight},
 * {@linkplain #expireAfter(Expiry) expireAfter}, {@linkplain #expireAfterWrite expireAfterWrite},
 * {@linkplain #expireAfterAccess expireAfterAccess}, {@linkplain #weakKeys weakKeys},
 * {@linkplain #weakValues weakValues}, or {@linkplain #softValues softValues} perform periodic
 * maintenance.
 * <p>
 * The caches produced by {@code Caffeine} are serializable, and the deserialized caches retain all
 * the configuration properties of the original cache. Note that the serialized form does <i>not</i>
 * include cache contents but only configuration.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the most general key type this builder will be able to create caches for. This is
 *     normally {@code Object} unless it is constrained by using a method like {@code
 *     #removalListener}
 * @param <V> the most general value type this builder will be able to create caches for. This is
 *     normally {@code Object} unless it is constrained by using a method like {@code
 *     #removalListener}
 */
@NullMarked
@SuppressWarnings({ "JavadocDeclaration", "JavadocReference" })
public final class Caffeine<K, V> {

    static final Supplier<StatsCounter> ENABLED_STATS_COUNTER_SUPPLIER = ConcurrentStatsCounter::new;

    static final Logger logger = System.getLogger(Caffeine.class.getName());

    static final Duration MIN_DURATION = Duration.ofNanos(Long.MIN_VALUE);

    static final Duration MAX_DURATION = Duration.ofNanos(Long.MAX_VALUE);

    static final double DEFAULT_LOAD_FACTOR = 0.75;

    static final int DEFAULT_INITIAL_CAPACITY = 16;

    enum Strength {

        WEAK, SOFT
    }

    static final int UNSET_INT = -1;

    boolean strictParsing = true;

    boolean interner;

    long maximumSize = UNSET_INT;

    long maximumWeight = UNSET_INT;

    int initialCapacity = UNSET_INT;

    long expireAfterWriteNanos = UNSET_INT;

    long expireAfterAccessNanos = UNSET_INT;

    long refreshAfterWriteNanos = UNSET_INT;

    @Nullable
    RemovalListener<? super K, ? super V> evictionListener;

    @Nullable
    RemovalListener<? super K, ? super V> removalListener;

    @Nullable
    Supplier<StatsCounter> statsCounterSupplier;

    @Nullable
    Weigher<? super K, ? super V> weigher;

    @Nullable
    Expiry<? super K, ? super V> expiry;

    @Nullable
    Scheduler scheduler;

    @Nullable
    Executor executor;

    @Nullable
    Ticker ticker;

    @Nullable
    Strength keyStrength;

    @Nullable
    Strength valueStrength;

    private Caffeine() {
    }

    @FormatMethod
    static void requireArgument(boolean expression, String template, @Nullable Object... args) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static void requireArgument(boolean expression, String message) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static void requireArgument(boolean expression) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static void requireState(boolean expression) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @FormatMethod
    static void requireState(boolean expression, String template, @Nullable Object... args) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static int ceilingPowerOfTwo(int x) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long ceilingPowerOfTwo(long x) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static int calculateHashMapCapacity(int numMappings) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static int calculateHashMapCapacity(Iterable<?> iterable) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long toNanosSaturated(Duration duration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static boolean hasMethodOverride(Class<?> clazz, Object instance, String methodName, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Caffeine<Object, Object> newBuilder() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K> BoundedLocalCache<K, Boolean> newWeakInterner() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Caffeine<Object, Object> from(CaffeineSpec spec) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Caffeine<Object, Object> from(String spec) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> initialCapacity(int initialCapacity) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean hasInitialCapacity() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    int getInitialCapacity() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> executor(Executor executor) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Executor getExecutor() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> scheduler(Scheduler scheduler) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Scheduler getScheduler() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> maximumSize(long maximumSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> maximumWeight(long maximumWeight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends V> Caffeine<K1, V1> weigher(Weigher<? super K1, ? super V1> weigher) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean evicts() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isWeighted() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long getMaximum() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "JavaAnnotator", "PMD.TypeParameterNamingConventions", "unchecked" })
    <K1 extends K, V1 extends V> Weigher<K1, V1> getWeigher(boolean isAsync) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> weakKeys() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isStrongKeys() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> weakValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isStrongValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isWeakValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> softValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> expireAfterWrite(Duration duration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> expireAfterWrite(long duration, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long getExpiresAfterWriteNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean expiresAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> expireAfterAccess(Duration duration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> expireAfterAccess(long duration, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long getExpiresAfterAccessNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean expiresAfterAccess() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends V> Caffeine<K1, V1> expireAfter(Expiry<? super K1, ? super V1> expiry) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean expiresVariable() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    Expiry<K, V> getExpiry(boolean isAsync) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> refreshAfterWrite(Duration duration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> refreshAfterWrite(long duration, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long getRefreshAfterWriteNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean refreshAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> ticker(Ticker ticker) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Ticker getTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends V> Caffeine<K1, V1> evictionListener(RemovalListener<? super K1, ? super V1> evictionListener) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "JavaAnnotator", "PMD.TypeParameterNamingConventions", "unchecked" })
    @Nullable
    <K1 extends K, V1 extends V> RemovalListener<K1, V1> getEvictionListener(boolean async) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends V> Caffeine<K1, V1> removalListener(RemovalListener<? super K1, ? super V1> removalListener) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "JavaAnnotator", "PMD.TypeParameterNamingConventions", "unchecked" })
    @Nullable
    <K1 extends K, V1 extends V> RemovalListener<K1, V1> getRemovalListener(boolean async) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> recordStats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public Caffeine<K, V> recordStats(Supplier<? extends StatsCounter> statsCounterSupplier) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isRecordingStats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Supplier<StatsCounter> getStatsCounterSupplier() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean isBounded() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends @Nullable V> Cache<K1, V1> build() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends @Nullable V> LoadingCache<K1, V1> build(CacheLoader<? super K1, V1> loader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends @Nullable V> AsyncCache<K1, V1> buildAsync() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends @Nullable V> AsyncLoadingCache<K1, V1> buildAsync(CacheLoader<? super K1, V1> loader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.TypeParameterNamingConventions")
    public <K1 extends K, V1 extends @Nullable V> AsyncLoadingCache<K1, V1> buildAsync(AsyncCacheLoader<? super K1, V1> loader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void requireNonLoadingCache() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void requireWeightWithWeigher() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
