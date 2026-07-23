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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * A factory for caches optimized for a particular configuration.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@FunctionalInterface
interface LocalCacheFactory {

    MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    MethodType FACTORY = MethodType.methodType(void.class, Caffeine.class, AsyncCacheLoader.class, boolean.class);

    MethodType FACTORY_CALL = FACTORY.changeReturnType(BoundedLocalCache.class);

    ConcurrentMap<String, LocalCacheFactory> FACTORIES = new ConcurrentHashMap<>();

    String EXPIRES_AFTER_ACCESS_NANOS = "expiresAfterAccessNanos";

    String EXPIRES_AFTER_WRITE_NANOS = "expiresAfterWriteNanos";

    String REFRESH_AFTER_WRITE_NANOS = "refreshAfterWriteNanos";

    String WEIGHTED_SIZE = "weightedSize";

    String MAXIMUM = "maximum";

    /**
     * Returns a cache optimized for this configuration.
     */
    <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) throws Throwable;

    static <K, V> BoundedLocalCache<K, V> newBoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static String getClassName(Caffeine<?, ?> builder) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static LocalCacheFactory loadFactory(String className) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static LocalCacheFactory newFactory(String className) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    final class MethodHandleBasedFactory implements LocalCacheFactory {

        final MethodHandle methodHandle;

        MethodHandleBasedFactory(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
            this.methodHandle = LOOKUP.findConstructor(clazz, FACTORY).asType(FACTORY_CALL);
        }

        @SuppressWarnings({ "ClassEscapesDefinedScope", "unchecked" })
        @Override
        public <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean async) throws Throwable {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
