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
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.cache.Weigher;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.configuration.TypesafeConfigurator;
import com.github.benmanes.caffeine.jcache.event.EventDispatcher;
import com.github.benmanes.caffeine.jcache.event.JCacheEvictionListener;
import com.github.benmanes.caffeine.jcache.integration.JCacheLoaderAdapter;
import com.github.benmanes.caffeine.jcache.management.JCacheStatisticsMXBean;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;

/**
 * A factory for creating a cache from the configuration.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class CacheFactory {

    private CacheFactory() {
    }

    public static boolean isDefinedExternally(CacheManager cacheManager, String cacheName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    @Nullable
    public static <K, V> CacheProxy<K, V> tryToCreateFromExternalSettings(CacheManager cacheManager, String cacheName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static <K, V> CacheProxy<K, V> createCache(CacheManager cacheManager, String cacheName, Configuration<K, V> configuration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Returns the resolved configuration.
     */
    private static Config rootConfig(CacheManager cacheManager) {
        return requireNonNull(TypesafeConfigurator.configSource().get(cacheManager.getURI(), cacheManager.getClassLoader()));
    }

    /**
     * Copies the configuration and overlays it on top of the default settings.
     */
    private static <K, V> CaffeineConfiguration<K, V> resolveConfigurationFor(CacheManager cacheManager, Configuration<K, V> configuration) {
        if (configuration instanceof CaffeineConfiguration<?, ?>) {
            return new CaffeineConfiguration<>((CaffeineConfiguration<K, V>) configuration);
        }
        CaffeineConfiguration<K, V> template = TypesafeConfigurator.defaults(rootConfig(cacheManager));
        if (configuration instanceof CompleteConfiguration<?, ?>) {
            var complete = (CompleteConfiguration<K, V>) configuration;
            template.setReadThrough(complete.isReadThrough());
            template.setWriteThrough(complete.isWriteThrough());
            template.setManagementEnabled(complete.isManagementEnabled());
            template.setStatisticsEnabled(complete.isStatisticsEnabled());
            template.getCacheEntryListenerConfigurations().forEach(template::removeCacheEntryListenerConfiguration);
            complete.getCacheEntryListenerConfigurations().forEach(template::addCacheEntryListenerConfiguration);
            template.setCacheLoaderFactory(complete.getCacheLoaderFactory());
            template.setCacheWriterFactory(complete.getCacheWriterFactory());
            template.setExpiryPolicyFactory(complete.getExpiryPolicyFactory());
        }
        template.setTypes(configuration.getKeyType(), configuration.getValueType());
        template.setStoreByValue(configuration.isStoreByValue());
        return template;
    }

    /**
     * A one-shot builder for creating a cache instance.
     */
    private static final class Builder<K, V> {

        final Ticker ticker;

        final String cacheName;

        final Executor executor;

        final Scheduler scheduler;

        final CacheManager cacheManager;

        final ExpiryPolicy expiryPolicy;

        final EventDispatcher<K, V> dispatcher;

        final JCacheStatisticsMXBean statistics;

        final Caffeine<Object, Object> caffeine;

        final CaffeineConfiguration<K, V> config;

        Builder(CacheManager cacheManager, String cacheName, CaffeineConfiguration<K, V> config) {
            this.config = config;
            this.cacheName = cacheName;
            this.cacheManager = cacheManager;
            this.caffeine = Caffeine.newBuilder();
            this.statistics = new JCacheStatisticsMXBean();
            this.ticker = config.getTickerFactory().create();
            this.executor = config.getExecutorFactory().create();
            this.scheduler = config.getSchedulerFactory().create();
            this.expiryPolicy = config.getExpiryPolicyFactory().create();
            this.dispatcher = new EventDispatcher<>(executor);
            caffeine.ticker(ticker);
            caffeine.executor(executor);
            caffeine.scheduler(scheduler);
            config.getCacheEntryListenerConfigurations().forEach(dispatcher::register);
        }

        CacheProxy<K, V> build() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Determines if the cache should operate in read through mode.
         */
        private boolean isReadThrough() {
            return config.isReadThrough() && (config.getCacheLoaderFactory() != null);
        }

        /**
         * Creates a cache that does not read through on a cache miss.
         */
        private CacheProxy<K, V> newCacheProxy() {
            var cacheLoaderFactory = config.getCacheLoaderFactory();
            var cacheLoader = (cacheLoaderFactory == null) ? null : cacheLoaderFactory.create();
            return new CacheProxy<>(cacheName, executor, cacheManager, config, caffeine.build(), dispatcher, Optional.ofNullable(cacheLoader), expiryPolicy, ticker, statistics);
        }

        /**
         * Creates a cache that reads through on a cache miss.
         */
        private CacheProxy<K, V> newLoadingCacheProxy() {
            CacheLoader<K, V> cacheLoader = requireNonNull(config.getCacheLoaderFactory()).create();
            var adapter = new JCacheLoaderAdapter<>(cacheLoader, dispatcher, expiryPolicy, ticker, statistics);
            var cache = caffeine.build(adapter);
            var jcache = new LoadingCacheProxy<>(cacheName, executor, cacheManager, config, cache, dispatcher, cacheLoader, expiryPolicy, ticker, statistics);
            adapter.setCache(jcache);
            return jcache;
        }

        /**
         * Configures the maximum size and returns if set.
         */
        private boolean configureMaximumSize() {
            if (config.getMaximumSize().isPresent()) {
                caffeine.maximumSize(config.getMaximumSize().getAsLong());
            }
            return config.getMaximumSize().isPresent();
        }

        /**
         * Configures the maximum weight and returns if set.
         */
        private boolean configureMaximumWeight() {
            if (config.getMaximumWeight().isPresent()) {
                caffeine.maximumWeight(config.getMaximumWeight().getAsLong());
                Weigher<K, V> weigher = config.getWeigherFactory().map(Factory::create).orElseThrow(() -> new IllegalStateException("Weigher not configured"));
                caffeine.weigher((K key, Expirable<V> expirable) -> {
                    return weigher.weigh(key, expirable.get());
                });
            }
            return config.getMaximumWeight().isPresent();
        }

        /**
         * Configures write expiration and returns if set.
         */
        private boolean configureExpireAfterWrite() {
            if (config.getExpireAfterWrite().isEmpty()) {
                return false;
            }
            caffeine.expireAfterWrite(Duration.ofNanos(config.getExpireAfterWrite().getAsLong()));
            return true;
        }

        /**
         * Configures access expiration and returns if set.
         */
        private boolean configureExpireAfterAccess() {
            if (config.getExpireAfterAccess().isEmpty()) {
                return false;
            }
            caffeine.expireAfterAccess(Duration.ofNanos(config.getExpireAfterAccess().getAsLong()));
            return true;
        }

        /**
         * Configures the custom expiration and returns if set.
         */
        private boolean configureExpireVariably() {
            if (config.getExpiryFactory().isEmpty()) {
                return false;
            }
            caffeine.expireAfter(new ExpiryAdapter<>(config.getExpiryFactory().orElseThrow().create()));
            return true;
        }

        private boolean configureJCacheExpiry() {
            if (expiryPolicy instanceof EternalExpiryPolicy) {
                return false;
            }
            caffeine.expireAfter(new ExpirableToExpiry<>(ticker));
            return true;
        }

        private void configureRefreshAfterWrite() {
            if (config.getRefreshAfterWrite().isPresent()) {
                caffeine.refreshAfterWrite(Duration.ofNanos(config.getRefreshAfterWrite().getAsLong()));
            }
        }
    }

    private static final class ExpiryAdapter<K, V> implements Expiry<K, Expirable<V>> {

        private final Expiry<K, V> expiry;

        ExpiryAdapter(Expiry<K, V> expiry) {
            this.expiry = requireNonNull(expiry);
        }

        @Override
        public long expireAfterCreate(K key, Expirable<V> expirable, long currentTime) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterUpdate(K key, Expirable<V> expirable, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterRead(K key, Expirable<V> expirable, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    private static final class ExpirableToExpiry<K, V> implements Expiry<K, Expirable<V>> {

        private final Ticker ticker;

        ExpirableToExpiry(Ticker ticker) {
            this.ticker = requireNonNull(ticker);
        }

        @Override
        public long expireAfterCreate(K key, Expirable<V> expirable, long currentTime) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterUpdate(K key, Expirable<V> expirable, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long expireAfterRead(K key, Expirable<V> expirable, long currentTime, long currentDuration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private long toNanos(Expirable<V> expirable) {
            if (expirable.getExpireTimeMillis() == 0L) {
                return -1L;
            } else if (expirable.isEternal()) {
                return Long.MAX_VALUE;
            }
            return TimeUnit.MILLISECONDS.toNanos(expirable.getExpireTimeMillis()) - ticker.read();
        }
    }
}
