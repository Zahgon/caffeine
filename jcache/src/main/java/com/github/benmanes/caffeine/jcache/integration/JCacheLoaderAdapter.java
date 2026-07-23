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
package com.github.benmanes.caffeine.jcache.integration;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableMap;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.jcache.CacheProxy;
import com.github.benmanes.caffeine.jcache.Expirable;
import com.github.benmanes.caffeine.jcache.event.EventDispatcher;
import com.github.benmanes.caffeine.jcache.management.JCacheStatisticsMXBean;

/**
 * An adapter from a JCache cache loader to Caffeine's.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class JCacheLoaderAdapter<K, V> implements com.github.benmanes.caffeine.cache.CacheLoader<K, @Nullable Expirable<V>> {

    private static final Logger logger = System.getLogger(JCacheLoaderAdapter.class.getName());

    private final JCacheStatisticsMXBean statistics;

    private final EventDispatcher<K, V> dispatcher;

    private final CacheLoader<K, V> delegate;

    private final ExpiryPolicy expiry;

    private final Ticker ticker;

    @Nullable
    private CacheProxy<K, V> cache;

    public JCacheLoaderAdapter(CacheLoader<K, V> delegate, EventDispatcher<K, V> dispatcher, ExpiryPolicy expiry, Ticker ticker, JCacheStatisticsMXBean statistics) {
        this.dispatcher = requireNonNull(dispatcher);
        this.statistics = requireNonNull(statistics);
        this.delegate = requireNonNull(delegate);
        this.expiry = requireNonNull(expiry);
        this.ticker = requireNonNull(ticker);
    }

    public void setCache(CacheProxy<K, V> cache) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("ConstantValue")
    @Nullable
    public Expirable<V> load(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Map<K, Expirable<V>> loadAll(Set<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private long expireTimeMillis() {
        try {
            Duration duration = expiry.getExpiryForCreation();
            if (duration.isZero()) {
                return 0;
            } else if (duration.isEternal()) {
                return Long.MAX_VALUE;
            }
            long millis = TimeUnit.NANOSECONDS.toMillis(ticker.read());
            return duration.getAdjustedTime(millis);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Exception thrown by expiry policy", e);
            throw e;
        }
    }
}
