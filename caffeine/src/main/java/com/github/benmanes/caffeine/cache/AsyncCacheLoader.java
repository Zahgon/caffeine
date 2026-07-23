/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
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

import static java.util.Objects.requireNonNull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Computes or retrieves values asynchronously based on a key, for use in populating a
 * {@link AsyncLoadingCache}.
 * <p>
 * Most implementations will only need to implement {@link #asyncLoad}. Other methods may be
 * overridden as desired.
 * <p>
 * Usage example:
 * {@snippet class=com.github.benmanes.caffeine.cache.Snippets region=asyncLoader_basic lang=java}
 *
 * @param <K> the type of keys
 * @param <V> the type of values. A loader may return null values if and only if it declares a
 *     nullable value type. A cache may return null values if and only if its loader does. (Null
 *     values are still never <i>stored</i> in the cache.)
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@FunctionalInterface
@SuppressWarnings({ "JavadocDeclaration", "JavadocReference", "PMD.SignatureDeclareThrowsException" })
public interface AsyncCacheLoader<K, V extends @Nullable Object> {

    /**
     * Asynchronously computes or retrieves the value corresponding to {@code key}.
     * <p>
     * <b>Warning:</b> loading <b>must not</b> attempt to update any mappings of this cache directly.
     *
     * @param key the non-null key whose value should be loaded
     * @param executor the executor with which the entry may be asynchronously loaded
     * @return the future value associated with {@code key}
     * @throws Exception or Error, in which case the mapping is unchanged
     * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
     *         treated like any other {@code Exception} in all respects except that, when it is
     *         caught, the thread's interrupt status is set
     */
    CompletableFuture<? extends V> asyncLoad(K key, Executor executor) throws Exception;

    default CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>> asyncLoadAll(Set<? extends K> keys, Executor executor) throws Exception {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default CompletableFuture<? extends V> asyncReload(K key, @NonNull V oldValue, Executor executor) throws Exception {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V extends @Nullable Object> AsyncCacheLoader<K, V> bulk(Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V extends @Nullable Object> AsyncCacheLoader<K, V> bulk(BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
