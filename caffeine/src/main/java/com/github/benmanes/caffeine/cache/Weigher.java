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

import static com.github.benmanes.caffeine.cache.Caffeine.requireArgument;
import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import org.jspecify.annotations.NullMarked;

/**
 * Calculates the weights of cache entries. The total weight threshold is used to determine when an
 * eviction is required.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@FunctionalInterface
public interface Weigher<K, V> {

    /**
     * Returns the weight of a cache entry. There is no unit for entry weights; rather they are simply
     * relative to each other.
     *
     * @param key the key to weigh
     * @param value the value to weigh
     * @return the weight of the entry; must be non-negative
     */
    int weigh(K key, V value);

    static <K, V> Weigher<K, V> singletonWeigher() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V> Weigher<K, V> boundedWeigher(Weigher<K, V> delegate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

enum SingletonWeigher implements Weigher<Object, Object> {

    INSTANCE;

    @Override
    public int weigh(Object key, Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

final class BoundedWeigher<K, V> implements Weigher<K, V>, Serializable {

    private static final long serialVersionUID = 1;

    @SuppressWarnings("serial")
    final Weigher<? super K, ? super V> delegate;

    BoundedWeigher(Weigher<? super K, ? super V> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public int weigh(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Object writeReplace() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
