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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.References.LookupKeyReference;
import com.github.benmanes.caffeine.cache.References.WeakKeyReference;
import com.google.errorprone.annotations.Var;

/**
 * A factory for cache nodes optimized for a particular configuration.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
interface NodeFactory<K, V> {

    MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    MethodType FACTORY = MethodType.methodType(void.class);

    ConcurrentMap<String, NodeFactory<Object, Object>> FACTORIES = new ConcurrentHashMap<>();

    RetiredStrongKey RETIRED_STRONG_KEY = new RetiredStrongKey();

    RetiredWeakKey RETIRED_WEAK_KEY = new RetiredWeakKey();

    DeadStrongKey DEAD_STRONG_KEY = new DeadStrongKey();

    DeadWeakKey DEAD_WEAK_KEY = new DeadWeakKey();

    String ACCESS_TIME = "accessTime";

    String WRITE_TIME = "writeTime";

    String VALUE = "value";

    String KEY = "key";

    default boolean weakValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default boolean softValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Returns a node optimized for the specified features.
     */
    Node<K, V> newNode(K key, @Nullable ReferenceQueue<K> keyReferenceQueue, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now);

    /**
     * Returns a node optimized for the specified features.
     */
    Node<K, V> newNode(Object keyReference, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now);

    default Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    default Object newLookupKey(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    static <K, V> NodeFactory<K, V> newFactory(Caffeine<K, V> builder, boolean isAsync) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static String getClassName(Caffeine<?, ?> builder, boolean isAsync) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    static <K, V> NodeFactory<K, V> loadFactory(String className) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    static NodeFactory<Object, Object> newFactory(String className) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    final class RetiredWeakKey extends WeakKeyReference<Object> {

        RetiredWeakKey() {
            super(/* key= */
            null, /* queue= */
            null);
        }
    }

    final class DeadWeakKey extends WeakKeyReference<Object> {

        DeadWeakKey() {
            super(/* key= */
            null, /* queue= */
            null);
        }
    }

    final class RetiredStrongKey {
    }

    final class DeadStrongKey {
    }
}
