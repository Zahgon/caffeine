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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A Caffeine-backed loading cache through a Guava facade.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("serial")
final class CaffeinatedGuavaLoadingCache<K, V> extends CaffeinatedGuavaCache<K, V> implements LoadingCache<K, V> {

    private static final ThreadLocal<Boolean> nullBulkLoad = new ThreadLocal<>();

    private static final long serialVersionUID = 1L;

    private final com.github.benmanes.caffeine.cache.LoadingCache<K, V> cache;

    CaffeinatedGuavaLoadingCache(com.github.benmanes.caffeine.cache.LoadingCache<K, V> cache) {
        super(cache);
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public V get(K key) throws ExecutionException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "CatchingUnchecked", "PMD.PreserveStackTrace" })
    public V getUnchecked(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "CatchingUnchecked", "PMD.PreserveStackTrace" })
    public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("deprecation")
    public V apply(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void refresh(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    abstract static class CaffeinatedLoader<K, V> implements CacheLoader<K, V>, Serializable {

        private static final long serialVersionUID = 1L;

        final com.google.common.cache.CacheLoader<K, V> cacheLoader;

        CaffeinatedLoader(com.google.common.cache.CacheLoader<K, V> cacheLoader) {
            this.cacheLoader = requireNonNull(cacheLoader);
        }

        @SuppressWarnings("ConstantValue")
        @Override
        public CompletableFuture<V> asyncReload(K key, V oldValue, Executor executor) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static class InternalSingleLoader<K, V> extends CaffeinatedLoader<K, V> {

        private static final long serialVersionUID = 1L;

        InternalSingleLoader(com.google.common.cache.CacheLoader<K, V> cacheLoader) {
            super(cacheLoader);
        }

        @SuppressWarnings("ConstantValue")
        @Override
        public V load(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class InternalBulkLoader<K, V> extends InternalSingleLoader<K, V> {

        private static final long serialVersionUID = 1L;

        InternalBulkLoader(com.google.common.cache.CacheLoader<K, V> cacheLoader) {
            super(cacheLoader);
        }

        @SuppressWarnings("ConstantValue")
        @Override
        public Map<K, V> loadAll(Set<? extends K> keys) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static class ExternalSingleLoader<K, V> extends CaffeinatedLoader<K, V> {

        private static final long serialVersionUID = 1L;

        ExternalSingleLoader(com.google.common.cache.CacheLoader<K, V> cacheLoader) {
            super(cacheLoader);
        }

        @SuppressWarnings("ConstantValue")
        @Override
        public V load(K key) throws Exception {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class ExternalBulkLoader<K, V> extends ExternalSingleLoader<K, V> {

        private static final long serialVersionUID = 1L;

        ExternalBulkLoader(com.google.common.cache.CacheLoader<K, V> cacheLoader) {
            super(cacheLoader);
        }

        @Override
        public Map<K, V> loadAll(Set<? extends K> keys) throws Exception {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class FutureCompleter<V> implements FutureCallback<V> {

        final CompletableFuture<V> future;

        FutureCompleter(CompletableFuture<V> future) {
            this.future = future;
        }

        @Override
        public void onSuccess(@Nullable V value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void onFailure(Throwable t) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
