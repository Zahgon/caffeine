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
package com.github.benmanes.caffeine.cache;

import static com.github.benmanes.caffeine.cache.Caffeine.calculateHashMapCapacity;
import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.errorprone.annotations.Var;

/**
 * This class provides a skeletal implementation of the {@link Cache} interface to minimize the
 * effort required to implement a {@link LocalCache}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
interface LocalManualCache<K, V> extends Cache<K, V> {

    /**
     * Returns the backing {@link LocalCache} data store.
     */
    LocalCache<K, V> cache();

    @Override
    default long estimatedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void cleanUp() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    default V getIfPresent(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("NullAway")
    @Nullable
    default V get(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default void bulkLoad(Set<K> keysToLoad, Map<K, @Nullable V> result, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void put(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void invalidate(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void invalidateAll(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default void invalidateAll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default CacheStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default ConcurrentMap<K, V> asMap() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
