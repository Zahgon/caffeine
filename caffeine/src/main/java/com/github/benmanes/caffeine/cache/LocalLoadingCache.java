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
import static com.github.benmanes.caffeine.cache.Caffeine.hasMethodOverride;
import static com.github.benmanes.caffeine.cache.LocalAsyncCache.composeResult;
import static java.util.Objects.requireNonNull;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * This class provides a skeletal implementation of the {@link LoadingCache} interface to minimize
 * the effort required to implement a {@link LocalCache}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
interface LocalLoadingCache<K, V> extends LocalManualCache<K, V>, LoadingCache<K, V> {

    Logger logger = System.getLogger(LocalLoadingCache.class.getName());

    /**
     * Returns the {@link AsyncCacheLoader} used by this cache.
     */
    AsyncCacheLoader<? super K, V> cacheLoader();

    /**
     * Returns the {@link CacheLoader#load} as a mapping function.
     */
    Function<K, @Nullable V> mappingFunction();

    /**
     * Returns the {@link CacheLoader#loadAll} as a mapping function, if implemented.
     */
    @Nullable
    Function<Set<? extends K>, Map<K, V>> bulkMappingFunction();

    @Override
    @SuppressWarnings("NullAway")
    default V get(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default Map<K, V> getAll(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default Map<K, V> loadSequentially(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    default CompletableFuture<V> refresh(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V> Function<K, @Nullable V> newMappingFunction(CacheLoader<? super K, V> cacheLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    static <K, V> Function<Set<? extends K>, Map<K, V>> newBulkMappingFunction(CacheLoader<? super K, V> cacheLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static boolean hasLoadAll(CacheLoader<?, ?> cacheLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
