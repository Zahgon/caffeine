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
package com.github.benmanes.caffeine.jcache.event;

import static java.util.Objects.requireNonNull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;
import org.jspecify.annotations.Nullable;

/**
 * A cache event dispatched to a listener.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("serial")
final class JCacheEntryEvent<K, V> extends CacheEntryEvent<K, V> implements Iterable<CacheEntryEvent<? extends K, ? extends V>> {

    private static final long serialVersionUID = 1L;

    private final K key;

    private final boolean hasOldValue;

    @Nullable
    private final V oldValue;

    @Nullable
    private final V newValue;

    JCacheEntryEvent(Cache<K, V> source, EventType eventType, K key, boolean hasOldValue, @Nullable V oldValue, @Nullable V newValue) {
        super(source, eventType);
        this.key = requireNonNull(key);
        this.hasOldValue = hasOldValue;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public K getKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getOldValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isOldValueAvailable() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Iterator<CacheEntryEvent<? extends K, ? extends V>> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
