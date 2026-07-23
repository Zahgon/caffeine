/*
 * Copyright 2024 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.examples.indexable;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;

/**
 * A cache abstraction that allows the entry to be looked up by alternative keys. This approach
 * mirrors a database table where a row is stored by its primary key, it contains all of the columns
 * that identify it, and the unique indexes are additional mappings defined by the column mappings.
 * This class similarly stores the value once in the cache by its primary key and maintains a
 * secondary mapping for lookups by using indexing functions to derive the keys.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class IndexedCache<K, V> {

    final ConcurrentMap<K, SequencedSet<K>> indexes;

    final SequencedSet<Function<V, K>> indexers;

    final Function<K, V> mappingFunction;

    final Striped<Lock> locks;

    final Cache<K, V> store;

    private IndexedCache(Caffeine<Object, Object> cacheBuilder, Function<K, V> mappingFunction, SequencedSet<Function<V, K>> indexers) {
        this.indexes = new ConcurrentHashMap<>();
        this.mappingFunction = mappingFunction;
        this.locks = Striped.lock(1_024);
        this.indexers = indexers;
        this.store = cacheBuilder.evictionListener((key, value, cause) -> indexes.keySet().removeAll(indexes.get(key))).build();
    }

    public V getIfPresent(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public V get(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void put(V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void invalidate(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Returns a sequence of keys where the first item is the primary key.
     */
    private SequencedSet<K> buildIndex(V value) {
        var index = LinkedHashSet.<K>newLinkedHashSet(indexers.size());
        for (var indexer : indexers) {
            var key = indexer.apply(value);
            if (key == null) {
                checkState(!index.isEmpty(), "The primary key may not be null");
            } else {
                index.add(key);
            }
        }
        return Collections.unmodifiableSequencedSet(index);
    }

    /**
     * This builder could be extended to support most cache options, but not weak keys.
     */
    public static final class Builder<K, V> {

        final SequencedSet<Function<V, K>> indexers;

        final Caffeine<Object, Object> cacheBuilder;

        boolean hasPrimary;

        public Builder() {
            indexers = new LinkedHashSet<>();
            cacheBuilder = Caffeine.newBuilder();
        }

        public Builder<K, V> expireAfterWrite(Duration duration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Builder<K, V> ticker(Ticker ticker) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Builder<K, V> primaryKey(Function<V, K> primary) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Builder<K, V> addSecondaryKey(Function<V, K> secondary) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public IndexedCache<K, V> build(Function<K, V> mappingFunction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
