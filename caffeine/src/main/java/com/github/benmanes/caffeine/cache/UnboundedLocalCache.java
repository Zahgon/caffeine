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

import static com.github.benmanes.caffeine.cache.Caffeine.calculateHashMapCapacity;
import static com.github.benmanes.caffeine.cache.LocalLoadingCache.newBulkMappingFunction;
import static com.github.benmanes.caffeine.cache.LocalLoadingCache.newMappingFunction;
import static java.util.Objects.requireNonNull;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;

/**
 * An in-memory cache that has no capabilities for bounding the map. This implementation provides
 * a lightweight wrapper on top of {@link ConcurrentHashMap}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("serial")
final class UnboundedLocalCache<K, V> implements LocalCache<K, V> {

    static final Logger logger = System.getLogger(UnboundedLocalCache.class.getName());

    static final VarHandle REFRESHES = findVarHandle(UnboundedLocalCache.class, "refreshes", ConcurrentMap.class);

    @Nullable
    final RemovalListener<K, V> removalListener;

    final ConcurrentHashMap<K, V> data;

    final StatsCounter statsCounter;

    final boolean isRecordingStats;

    final Executor executor;

    final boolean isAsync;

    @Nullable
    Set<K> keySet;

    @Nullable
    Collection<V> values;

    @Nullable
    Set<Entry<K, V>> entrySet;

    @Nullable
    volatile ConcurrentMap<Object, CompletableFuture<?>> refreshes;

    UnboundedLocalCache(Caffeine<? super K, ? super V> builder, boolean isAsync) {
        this.data = new ConcurrentHashMap<>(builder.getInitialCapacity());
        this.statsCounter = builder.getStatsCounterSupplier().get();
        this.removalListener = builder.getRemovalListener(isAsync);
        this.isRecordingStats = builder.isRecordingStats();
        this.executor = builder.getExecutor();
        this.isAsync = isAsync;
    }

    static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isAsync() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Expiry<K, V> expiry() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean collectKeys() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @CanIgnoreReturnValue
    public Object referenceKey(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isPendingEviction(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Cache --------------- */
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    @Nullable
    public V getIfPresent(Object key, boolean recordStats) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    @Nullable
    public V getIfPresentQuietly(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long estimatedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public StatsCounter statsCounter() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isRecordingStats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Executor executor() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public ConcurrentMap<Object, CompletableFuture<?>> refreshes() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void discardRefresh(Object keyReference) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Ticker statsTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- JDK8+ Map extensions --------------- */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V computeIfAbsent(K key, Function<? super K, ? extends @Nullable V> mappingFunction, boolean recordStats, boolean recordLoad) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, boolean recordLoad, boolean recordLoadFailure, boolean @Nullable [] preserveTimestamps) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    V remap(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Concurrent Map --------------- */
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V get(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V remove(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean remove(Object key, @Nullable Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V replace(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * An adapter to safely externalize the keys.
     */
    static final class KeySetView<K> extends AbstractSet<K> {

        final UnboundedLocalCache<K, ?> cache;

        KeySetView(UnboundedLocalCache<K, ?> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super K> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<K> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<K> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <T> T[] toArray(T[] array) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the key iterator.
     */
    static final class KeyIterator<K> implements Iterator<K> {

        final UnboundedLocalCache<K, ?> cache;

        final Iterator<K> iterator;

        @Nullable
        K current;

        KeyIterator(UnboundedLocalCache<K, ?> cache) {
            this.iterator = cache.data.keySet().iterator();
            this.cache = cache;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public K next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the key spliterator.
     */
    static final class KeySpliterator<K, V> implements Spliterator<K> {

        final Spliterator<K> spliterator;

        KeySpliterator(UnboundedLocalCache<K, V> cache) {
            this(cache.data.keySet().spliterator());
        }

        KeySpliterator(Spliterator<K> spliterator) {
            this.spliterator = requireNonNull(spliterator);
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public KeySpliterator<K, V> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the values.
     */
    static final class ValuesView<K, V> extends AbstractCollection<V> {

        final UnboundedLocalCache<K, V> cache;

        ValuesView(UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(@Nullable Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<V> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<V> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <T> T[] toArray(T[] array) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the value iterator.
     */
    static final class ValueIterator<K, V> implements Iterator<V> {

        final UnboundedLocalCache<K, V> cache;

        final Iterator<Entry<K, V>> iterator;

        @Nullable
        Entry<K, V> entry;

        ValueIterator(UnboundedLocalCache<K, V> cache) {
            this.iterator = cache.data.entrySet().iterator();
            this.cache = cache;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the value spliterator.
     */
    static final class ValueSpliterator<K, V> implements Spliterator<V> {

        final Spliterator<V> spliterator;

        ValueSpliterator(UnboundedLocalCache<K, V> cache) {
            this(cache.data.values().spliterator());
        }

        ValueSpliterator(Spliterator<V> spliterator) {
            this.spliterator = requireNonNull(spliterator);
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public ValueSpliterator<K, V> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entries.
     */
    static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {

        final UnboundedLocalCache<K, V> cache;

        EntrySetView(UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entry iterator.
     */
    static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {

        final UnboundedLocalCache<K, V> cache;

        final Iterator<Entry<K, V>> iterator;

        @Nullable
        Entry<K, V> entry;

        EntryIterator(UnboundedLocalCache<K, V> cache) {
            this.iterator = cache.data.entrySet().iterator();
            this.cache = cache;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Entry<K, V> next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entry spliterator.
     */
    static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {

        final Spliterator<Entry<K, V>> spliterator;

        final UnboundedLocalCache<K, V> cache;

        EntrySpliterator(UnboundedLocalCache<K, V> cache) {
            this(cache, cache.data.entrySet().spliterator());
        }

        EntrySpliterator(UnboundedLocalCache<K, V> cache, Spliterator<Entry<K, V>> spliterator) {
            this.spliterator = requireNonNull(spliterator);
            this.cache = requireNonNull(cache);
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public EntrySpliterator<K, V> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /* --------------- Manual Cache --------------- */
    static class UnboundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {

        private static final long serialVersionUID = 1;

        final UnboundedLocalCache<K, V> cache;

        @Nullable
        Policy<K, V> policy;

        UnboundedLocalManualCache(Caffeine<K, V> builder) {
            cache = new UnboundedLocalCache<>(builder, /* isAsync= */
            false);
        }

        @Override
        public final UnboundedLocalCache<K, V> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public final Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An eviction policy that supports no bounding.
     */
    static final class UnboundedPolicy<K, V> implements Policy<K, V> {

        final Function<@Nullable V, @Nullable V> transformer;

        final UnboundedLocalCache<K, V> cache;

        UnboundedPolicy(UnboundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer) {
            this.transformer = transformer;
            this.cache = cache;
        }

        @Override
        public boolean isRecordingStats() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public V getIfPresentQuietly(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("Java9CollectionFactory")
        @Override
        public Map<K, CompletableFuture<V>> refreshes() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<Eviction<K, V>> eviction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedExpiration<K, V>> expireAfterAccess() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedExpiration<K, V>> expireAfterWrite() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<VarExpiration<K, V>> expireVariably() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedRefresh<K, V>> refreshAfterWrite() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /* --------------- Loading Cache --------------- */
    static final class UnboundedLocalLoadingCache<K, V> extends UnboundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {

        private static final long serialVersionUID = 1;

        final Function<K, @Nullable V> mappingFunction;

        final CacheLoader<? super K, V> cacheLoader;

        @Nullable
        final Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

        UnboundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader) {
            super(builder);
            this.cacheLoader = cacheLoader;
            this.mappingFunction = newMappingFunction(cacheLoader);
            this.bulkMappingFunction = newBulkMappingFunction(cacheLoader);
        }

        @Override
        public AsyncCacheLoader<? super K, V> cacheLoader() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Function<K, @Nullable V> mappingFunction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }

    /* --------------- Async Cache --------------- */
    static final class UnboundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {

        private static final long serialVersionUID = 1;

        final UnboundedLocalCache<K, CompletableFuture<V>> cache;

        @Nullable
        ConcurrentMap<K, CompletableFuture<V>> mapView;

        @Nullable
        CacheView<K, V> cacheView;

        @Nullable
        Policy<K, V> policy;

        @SuppressWarnings("unchecked")
        UnboundedLocalAsyncCache(Caffeine<K, V> builder) {
            cache = new UnboundedLocalCache<>((Caffeine<K, CompletableFuture<V>>) builder, /* isAsync= */
            true);
        }

        @Override
        public UnboundedLocalCache<K, CompletableFuture<V>> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ConcurrentMap<K, CompletableFuture<V>> asMap() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Cache<K, V> synchronous() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /* --------------- Async Loading Cache --------------- */
    static final class UnboundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        final UnboundedLocalCache<K, CompletableFuture<V>> cache;

        @Nullable
        ConcurrentMap<K, CompletableFuture<V>> mapView;

        @Nullable
        Policy<K, V> policy;

        @SuppressWarnings("unchecked")
        UnboundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
            super(loader);
            cache = new UnboundedLocalCache<>((Caffeine<K, CompletableFuture<V>>) builder, /* isAsync= */
            true);
        }

        @Override
        public LocalCache<K, CompletableFuture<V>> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ConcurrentMap<K, CompletableFuture<V>> asMap() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
