/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
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
import static com.github.benmanes.caffeine.cache.Caffeine.requireState;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;

/**
 * This class provides a skeletal implementation of the {@link AsyncCache} interface to minimize the
 * effort required to implement a {@link LocalCache}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
interface LocalAsyncCache<K, V> extends AsyncCache<K, V> {

    Logger logger = System.getLogger(LocalAsyncCache.class.getName());

    /**
     * Returns the backing {@link LocalCache} data store.
     */
    LocalCache<K, CompletableFuture<V>> cache();

    /**
     * Returns the policy supported by this implementation and its configuration.
     */
    Policy<K, V> policy();

    @Override
    @Nullable
    default CompletableFuture<V> getIfPresent(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default CompletableFuture<V> get(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction, boolean recordStats) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    default CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    default CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys, BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V> CompletableFuture<Map<K, V>> composeResult(Map<K, CompletableFuture<@Nullable V>> futures) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    default void put(K key, CompletableFuture<? extends @Nullable V> valueFuture) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "FutureReturnValueIgnored", "ResultOfMethodCallIgnored" })
    default void handleCompletion(K key, CompletableFuture<? extends V> valueFuture, long startTime, boolean recordMiss) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * A function executed asynchronously after a bulk load completes.
     */
    final class AsyncBulkCompleter<K, V> implements BiFunction<Map<? extends K, ? extends V>, Throwable, Map<? extends K, ? extends V>> {

        @SuppressWarnings("ImmutableMemberCollection")
        private final Map<K, CompletableFuture<@Nullable V>> proxies;

        private final LocalCache<K, CompletableFuture<V>> cache;

        private final long startTime;

        AsyncBulkCompleter(LocalCache<K, CompletableFuture<V>> cache, Map<K, CompletableFuture<@Nullable V>> proxies) {
            this.startTime = cache.statsTicker().read();
            this.proxies = proxies;
            this.cache = cache;
        }

        @Override
        @CanIgnoreReturnValue
        @Nullable
        public Map<? extends K, ? extends V> apply(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public CompletionException error(Throwable error) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Nullable
        private Throwable handleResponse(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
            if (result == null) {
                var failure = (error == null) ? new NullMapCompletionException() : error;
                for (var entry : proxies.entrySet()) {
                    cache.remove(entry.getKey(), entry.getValue());
                    entry.getValue().obtrudeException(failure);
                }
                if (!(failure instanceof CancellationException) && !(failure instanceof TimeoutException)) {
                    logger.log(Level.WARNING, "Exception thrown during asynchronous load", failure);
                }
                return failure;
            }
            var failure = fillProxies(result);
            return addNewEntries(result, failure);
        }

        /**
         * Populates the proxies with the computed result.
         */
        @Nullable
        private Throwable fillProxies(Map<? extends K, ? extends V> result) {
            @Var
            Throwable error = null;
            for (var entry : proxies.entrySet()) {
                var key = entry.getKey();
                var value = result.get(key);
                var future = entry.getValue();
                future.obtrudeValue(value);
                if (value == null) {
                    cache.remove(key, future);
                } else {
                    try {
                        // update the weight and expiration timestamps
                        cache.replace(key, future, future);
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                        cache.remove(key, future);
                        if (error == null) {
                            error = t;
                        } else {
                            error.addSuppressed(t);
                        }
                    }
                }
            }
            return error;
        }

        /**
         * Adds to the cache any extra entries computed that were not requested.
         */
        @Nullable
        private Throwable addNewEntries(Map<? extends K, ? extends @Nullable V> result, @Nullable Throwable failure) {
            @Var
            Throwable error = failure;
            for (Map.Entry<? extends K, ? extends @Nullable V> entry : result.entrySet()) {
                var key = entry.getKey();
                @Nullable
                V value = entry.getValue();
                if (!proxies.containsKey(key)) {
                    if (value == null) {
                        continue;
                    }
                    try {
                        cache.put(key, CompletableFuture.completedFuture(value));
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                        if (error == null) {
                            error = t;
                        } else {
                            error.addSuppressed(t);
                        }
                    }
                }
            }
            return error;
        }

        static final class NullMapCompletionException extends CompletionException {

            private static final long serialVersionUID = 1L;
        }
    }

    /* --------------- Asynchronous view --------------- */
    final class AsyncAsMapView<K, V> implements ConcurrentMap<K, CompletableFuture<V>> {

        final LocalAsyncCache<K, V> asyncCache;

        AsyncAsMapView(LocalAsyncCache<K, V> asyncCache) {
            this.asyncCache = requireNonNull(asyncCache);
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
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("CollectionUndefinedEquality")
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public CompletableFuture<V> get(Object key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public CompletableFuture<V> putIfAbsent(K key, CompletableFuture<V> value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public CompletableFuture<V> put(K key, CompletableFuture<V> value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings({ "FutureReturnValueIgnored", "ResultOfMethodCallIgnored" })
        @Override
        public void putAll(Map<? extends K, ? extends CompletableFuture<V>> map) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public CompletableFuture<V> replace(K key, CompletableFuture<V> value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean replace(K key, CompletableFuture<V> oldValue, CompletableFuture<V> newValue) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public CompletableFuture<V> remove(Object key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("FutureReturnValueIgnored")
        @Override
        @Nullable
        public CompletableFuture<V> computeIfAbsent(K key, Function<? super K, ? extends @Nullable CompletableFuture<V>> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        @Nullable
        public CompletableFuture<V> computeIfPresent(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        @Nullable
        public CompletableFuture<V> compute(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        @Nullable
        public CompletableFuture<V> merge(K key, CompletableFuture<V> value, BiFunction<? super CompletableFuture<V>, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super CompletableFuture<V>> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Set<K> keySet() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Collection<CompletableFuture<V>> values() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Set<Entry<K, CompletableFuture<V>>> entrySet() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
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
    }

    /* --------------- Synchronous view --------------- */
    final class CacheView<K, V> extends AbstractCacheView<K, V> {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("serial")
        final LocalAsyncCache<K, V> asyncCache;

        CacheView(LocalAsyncCache<K, V> asyncCache) {
            this.asyncCache = requireNonNull(asyncCache);
        }

        @Override
        LocalAsyncCache<K, V> asyncCache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    abstract class AbstractCacheView<K, V> implements Cache<K, V>, Serializable {

        private static final long serialVersionUID = 1L;

        @Nullable
        transient ConcurrentMap<K, V> asMapView;

        abstract LocalAsyncCache<K, V> asyncCache();

        @Override
        @Nullable
        public V getIfPresent(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V get(K key, Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings({ "PMD.AvoidThrowingNullPointerException", "PMD.PreserveStackTrace", "UnusedException" })
        protected static <T> T resolve(CompletableFuture<T> future) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void put(K key, V value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void invalidate(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void invalidateAll(Iterable<? extends K> keys) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void invalidateAll() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimatedSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public CacheStats stats() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void cleanUp() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ConcurrentMap<K, V> asMap() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        @SuppressWarnings("unused")
        private void readObjectNoData() throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        Object writeReplace() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    final class AsMapView<K, V> implements ConcurrentMap<K, V> {

        final LocalCache<K, CompletableFuture<V>> delegate;

        @Nullable
        Set<K> keys;

        @Nullable
        Collection<V> values;

        @Nullable
        Set<Entry<K, V>> entries;

        AsMapView(LocalCache<K, CompletableFuture<V>> delegate) {
            this.delegate = delegate;
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
        @SuppressWarnings("ResultOfMethodCallIgnored")
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
        public V put(K key, V value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public V remove(Object key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public boolean remove(Object key, @Nullable Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        public V replace(K key, V value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        public V compute(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends @Nullable V> remappingFunction) {
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

        private final class KeySet extends AbstractSet<K> {

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
            public boolean contains(Object o) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @SuppressWarnings("RedundantCollectionOperation")
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
            public Iterator<K> iterator() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Spliterator<K> spliterator() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        private static final class KeyIterator<K, V> implements Iterator<K> {

            private final Iterator<Entry<K, V>> iterator;

            KeyIterator(Iterator<Entry<K, V>> iterator) {
                this.iterator = requireNonNull(iterator);
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
            public void remove() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        private static final class KeySpliterator<K, V> implements Spliterator<K> {

            private final Spliterator<Entry<K, V>> spliterator;

            KeySpliterator(Spliterator<Entry<K, V>> iterator) {
                this.spliterator = requireNonNull(iterator);
            }

            @Override
            public boolean tryAdvance(Consumer<? super K> action) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @Nullable
            public Spliterator<K> trySplit() {
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

        private final class Values extends AbstractCollection<V> {

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
        }

        private static final class ValueIterator<K, V> implements Iterator<V> {

            private final Iterator<Entry<K, V>> iterator;

            ValueIterator(Iterator<Entry<K, V>> iterator) {
                this.iterator = requireNonNull(iterator);
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
            public void remove() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        private static final class ValueSpliterator<K, V> implements Spliterator<V> {

            private final Spliterator<Entry<K, V>> spliterator;

            ValueSpliterator(Spliterator<Entry<K, V>> iterator) {
                this.spliterator = requireNonNull(iterator);
            }

            @Override
            public boolean tryAdvance(Consumer<? super V> action) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @Nullable
            public Spliterator<V> trySplit() {
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

        private final class EntrySet extends AbstractSet<Entry<K, V>> {

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
            public boolean contains(Object o) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public boolean remove(Object obj) {
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

        private final class EntryIterator implements Iterator<Entry<K, V>> {

            final Iterator<Entry<K, CompletableFuture<V>>> iterator;

            @Nullable
            Entry<K, V> cursor;

            @Nullable
            K removalKey;

            EntryIterator() {
                this.iterator = delegate.entrySet().iterator();
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
            public void remove() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        private final class EntrySpliterator implements Spliterator<Entry<K, V>> {

            final Spliterator<Entry<K, CompletableFuture<V>>> spliterator;

            EntrySpliterator() {
                this(delegate.entrySet().spliterator());
            }

            EntrySpliterator(Spliterator<Entry<K, CompletableFuture<V>>> spliterator) {
                this.spliterator = requireNonNull(spliterator);
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
            public Spliterator<Entry<K, V>> trySplit() {
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
    }

    @SuppressWarnings("serial")
    final class SyncViewProxy<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        final AsyncCache<K, V> asyncCache;

        SyncViewProxy(AsyncCache<K, V> asyncCache) {
            this.asyncCache = requireNonNull(asyncCache);
        }

        Object readResolve() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
