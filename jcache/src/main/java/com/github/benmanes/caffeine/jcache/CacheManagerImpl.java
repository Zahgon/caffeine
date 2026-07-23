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
package com.github.benmanes.caffeine.jcache;

import static java.util.Objects.requireNonNull;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import org.jspecify.annotations.Nullable;

/**
 * An implementation of JSR-107 {@link CacheManager} that manages Caffeine-based caches.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("PMD.CloseResource")
public final class CacheManagerImpl implements CacheManager {

    final WeakReference<ClassLoader> classLoaderReference;

    final Map<String, CacheProxy<?, ?>> caches;

    final CachingProvider cacheProvider;

    final Properties properties;

    final Object lock;

    final URI uri;

    final boolean runsAsAnOsgiBundle;

    volatile boolean closed;

    public CacheManagerImpl(CachingProvider cacheProvider, boolean runsAsAnOsgiBundle, URI uri, ClassLoader classLoader, Properties properties) {
        this.classLoaderReference = new WeakReference<>(requireNonNull(classLoader));
        this.cacheProvider = requireNonNull(cacheProvider);
        this.runsAsAnOsgiBundle = runsAsAnOsgiBundle;
        this.properties = requireNonNull(properties);
        this.caches = new ConcurrentHashMap<>();
        this.uri = requireNonNull(uri);
        this.lock = new Object();
    }

    @Override
    public CachingProvider getCachingProvider() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public URI getURI() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Properties getProperties() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public <K, V> CacheProxy<K, V> getCache(String cacheName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Collection<String> getCacheNames() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void destroyCache(String cacheName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Checks that the cache manager is not closed.
     */
    private void requireNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException();
        }
    }
}
