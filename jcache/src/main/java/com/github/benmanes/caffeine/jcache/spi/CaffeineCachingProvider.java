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
package com.github.benmanes.caffeine.jcache.spi;

import static javax.cache.configuration.OptionalFeature.STORE_BY_REFERENCE;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import com.github.benmanes.caffeine.jcache.CacheManagerImpl;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.annotations.concurrent.GuardedBy;

/**
 * A provider that produces a JCache implementation backed by Caffeine. Typically, this provider is
 * instantiated using {@link Caching#getCachingProvider()} which discovers this implementation
 * through a {@link java.util.ServiceLoader}.
 * <p>
 * This provider is expected to be used for application life cycle events, like initialization. It
 * is not expected that all requests flow through the provider to obtain the cache manager and cache
 * instances for request operations. Internally, this implementation is synchronized to avoid using
 * excess memory due to its infrequent usage.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@Component
@NullMarked
public final class CaffeineCachingProvider implements CachingProvider {

    private static final ClassLoader DEFAULT_CLASS_LOADER = new JCacheClassLoader(Thread.currentThread().getContextClassLoader());

    @GuardedBy("itself")
    final Map<ClassLoader, Map<URI, CacheManager>> cacheManagers;

    boolean isOsgiComponent;

    public CaffeineCachingProvider() {
        this.cacheManagers = new WeakHashMap<>(1);
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public URI getDefaultURI() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Properties getDefaultProperties() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, @Nullable Properties properties) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public void close(ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public void close(URI uri, ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private URI getManagerUri(@Nullable URI uri) {
        return (uri == null) ? getDefaultURI() : uri;
    }

    private ClassLoader getManagerClassLoader(@Nullable ClassLoader classLoader) {
        return (classLoader == null) ? getDefaultClassLoader() : classLoader;
    }

    /**
     * A {@link ClassLoader} that combines {@code Thread.currentThread().getContextClassLoader()}
     * and {@code getClass().getClassLoader()}.
     */
    static class JCacheClassLoader extends ClassLoader {

        public JCacheClassLoader(@Nullable ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public URL getResource(String name) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Nullable
        ClassLoader getClassClassLoader() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @Activate
    @SuppressWarnings("unused")
    private void activate() {
        isOsgiComponent = true;
    }
}
