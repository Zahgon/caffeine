/*
 * Copyright 2022 Ben Manes. All Rights Reserved.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.References.LookupKeyEqualsReference;
import com.github.benmanes.caffeine.cache.References.WeakKeyEqualsReference;

/**
 * Provides similar behavior to {@link String#intern} for any immutable type.
 * <p>
 * Note that {@code String.intern()} has some well-known performance limitations and should
 * generally be avoided. Prefer {@link Interner#newWeakInterner} or another {@code Interner}
 * implementation even for {@code String} interning.
 *
 * @param <E> the type of elements
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@FunctionalInterface
public interface Interner<E> {

    /**
     * Chooses and returns the representative instance for any collection of instances that are
     * equal to each other. If two {@linkplain Object#equals equal} inputs are given to this method,
     * both calls will return the same instance. That is, {@code intern(a).equals(a)} always holds,
     * and {@code intern(a) == intern(b)} if and only if {@code a.equals(b)}. Note that {@code
     * intern(a)} is permitted to return one instance now and a different instance later if the
     * original interned instance was garbage-collected.
     * <p>
     * <b>Warning:</b> Do not use with mutable objects.
     *
     * @param sample the element to add if absent
     * @return the representative instance, possibly the {@code sample} if absent
     * @throws NullPointerException if {@code sample} is null
     */
    E intern(E sample);

    static <E> Interner<E> newStrongInterner() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <E> Interner<E> newWeakInterner() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

final class StrongInterner<E> implements Interner<E> {

    final ConcurrentMap<E, E> map;

    StrongInterner() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public E intern(E sample) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

final class WeakInterner<E> implements Interner<E> {

    final BoundedLocalCache<E, Boolean> cache;

    WeakInterner() {
        cache = Caffeine.newWeakInterner();
    }

    @Override
    public E intern(E sample) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

@SuppressWarnings({ "BooleanLiteral", "unchecked" })
final class Interned<K, V> extends Node<K, V> implements NodeFactory<K, V> {

    static final NodeFactory<Object, Object> FACTORY = new Interned<>();

    volatile Reference<?> keyReference;

    Interned() {
        this.keyReference = NodeFactory.DEAD_WEAK_KEY;
    }

    Interned(Reference<K> keyReference) {
        this.keyReference = keyReference;
    }

    @Override
    @Nullable
    public K getKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Object getKeyReference() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Object getKeyReferenceOrNull() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public V getValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public V getValueReference() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Node<K, V> newNode(K key, @Nullable ReferenceQueue<K> keyReferenceQueue, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Node<K, V> newNode(Object keyReference, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Object newLookupKey(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isRetired() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void retire() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isDead() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void die() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
