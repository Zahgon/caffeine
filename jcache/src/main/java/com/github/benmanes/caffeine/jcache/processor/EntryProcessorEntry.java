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
package com.github.benmanes.caffeine.jcache.processor;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.cache.integration.CacheLoader;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;
import org.jspecify.annotations.Nullable;

/**
 * An entry that is consumed by an {@link EntryProcessor}. The updates to the entry are replayed
 * on the cache when the processor completes.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class EntryProcessorEntry<K, V> implements MutableEntry<K, V> {

    private final boolean hasEntry;

    private final K key;

    private Action action;

    @Nullable
    private V value;

    private Optional<CacheLoader<K, V>> cacheLoader;

    public EntryProcessorEntry(K key, @Nullable V value, Optional<CacheLoader<K, V>> cacheLoader) {
        this.hasEntry = (value != null);
        this.cacheLoader = cacheLoader;
        this.action = Action.NONE;
        this.value = value;
        this.key = key;
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public K getKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ConstantValue")
    @Nullable
    public V getValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void setValue(V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Action getAction() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
