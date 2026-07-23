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
package com.github.benmanes.caffeine.guava;

import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingConcurrentMap;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A Caffeine-backed cache through a Guava facade.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("serial")
class CaffeinatedGuavaCache<K, V> implements Cache<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    @Nullable
    private transient ConcurrentMap<K, V> mapView;

    CaffeinatedGuavaCache(com.github.benmanes.caffeine.cache.Cache<K, V> cache) {
        this.cache = requireNonNull(cache);
    }

    @Override
    @Nullable
    public V getIfPresent(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "PMD.ExceptionAsFlowControl", "PMD.PreserveStackTrace" })
    public V get(K key, Callable<? extends @Nullable V> valueLoader) throws ExecutionException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void put(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void invalidate(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void invalidateAll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long size() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    final class AsMapView extends ForwardingConcurrentMap<K, V> {

        @Nullable
        Set<Entry<K, V>> entrySet;

        @Nullable
        Collection<V> values;

        @Nullable
        Set<K> keySet;

        @Override
        public boolean containsKey(@Nullable Object key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean containsValue(@Nullable Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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

        @Override
        protected ConcurrentMap<K, V> delegate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    final class KeySetView extends ForwardingSet<K> {

        @Override
        public boolean removeIf(Predicate<? super K> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(@Nullable Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        protected Set<K> delegate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    final class ValuesView extends ForwardingCollection<V> {

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(@Nullable Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        protected Collection<V> delegate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @SuppressWarnings("NullableProblems")
    final class EntrySetView extends ForwardingSet<Entry<K, V>> {

        @Override
        public boolean add(Entry<K, V> entry) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> entry) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        protected Set<Entry<K, V>> delegate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class CacheLoaderException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        CacheLoaderException(Throwable cause) {
            super(null, cause, /* enableSuppression= */
            false, /* writableStackTrace= */
            false);
        }
    }
}
