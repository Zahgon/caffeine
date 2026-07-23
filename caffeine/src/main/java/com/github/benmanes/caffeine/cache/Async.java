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

import static com.github.benmanes.caffeine.cache.BoundedLocalCache.MAXIMUM_EXPIRY;
import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;

/**
 * Static utility methods and classes pertaining to asynchronous operations.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("serial")
final class Async {

    // 220 years
    static final long ASYNC_EXPIRY = (Long.MAX_VALUE >> 1) + (Long.MAX_VALUE >> 2);

    static final Logger logger = System.getLogger(Async.class.getName());

    private Async() {
    }

    static boolean isReady(@Nullable CompletableFuture<?> future) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    static <V> V getIfReady(@Nullable CompletableFuture<V> future) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    static <V> V getWhenSuccessful(@Nullable CompletableFuture<V> future) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * A removal listener that asynchronously forwards the value stored in a {@link CompletableFuture}
     * if successful to the user-supplied removal listener.
     */
    static final class AsyncRemovalListener<K, V> implements RemovalListener<K, CompletableFuture<@Nullable V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final RemovalListener<K, V> delegate;

        final Executor executor;

        AsyncRemovalListener(RemovalListener<K, V> delegate, Executor executor) {
            this.delegate = requireNonNull(delegate);
            this.executor = requireNonNull(executor);
        }

        @Override
        @SuppressWarnings("FutureReturnValueIgnored")
        public void onRemoval(@Nullable K key, @Nullable CompletableFuture<@Nullable V> future, RemovalCause cause) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An eviction listener that forwards the value stored in a {@link CompletableFuture} to the
     * user-supplied eviction listener.
     */
    static final class AsyncEvictionListener<K, V> implements RemovalListener<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final RemovalListener<K, V> delegate;

        AsyncEvictionListener(RemovalListener<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public void onRemoval(@Nullable K key, @Nullable CompletableFuture<V> future, RemovalCause cause) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A weigher for asynchronous computations. When the value is being loaded this weigher returns
     * {@code 0} to indicate that the entry should not be evicted due to a size constraint. If the
     * value is computed successfully then the entry must be reinserted so that the weight is updated
     * and the expiration timeouts reflect the value once present. This can be done safely using
     * {@link java.util.Map#replace(Object, Object, Object)}.
     */
    static final class AsyncWeigher<K, V> implements Weigher<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final Weigher<K, V> delegate;

        AsyncWeigher(Weigher<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public int weigh(K key, CompletableFuture<V> future) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An expiry for asynchronous computations. When the value is being loaded this expiry returns
     * {@code ASYNC_EXPIRY} to indicate that the entry should not be evicted due to an expiry
     * constraint. If the value is computed successfully then the entry must be reinserted so that the
     * expiration is updated and the expiration timeouts reflect the value once present. The
     * duration's maximum range is reserved to coordinate with the asynchronous life cycle.
     */
    static final class AsyncExpiry<K, V> implements Expiry<K, CompletableFuture<V>>, Serializable {

        private static final long serialVersionUID = 1L;

        final Expiry<? super K, ? super V> delegate;

        AsyncExpiry(Expiry<? super K, ? super V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public long expireAfterCreate(K key, CompletableFuture<V> future, long currentTime) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterUpdate(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterRead(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
