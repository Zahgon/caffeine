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
package com.github.benmanes.caffeine.jcache.configuration;

import static java.util.Objects.requireNonNull;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.cache.Weigher;
import com.github.benmanes.caffeine.jcache.copy.Copier;
import com.github.benmanes.caffeine.jcache.copy.JavaSerializationCopier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * A JCache configuration with Caffeine specific settings.
 * <p>
 * The initial settings disable <code>store by value</code> so that entries are not copied when
 * crossing the {@link javax.cache.Cache} API boundary. If enabled and the {@link Copier} is not
 * explicitly set, then the {@link JavaSerializationCopier} will be used. This differs from
 * {@link MutableConfiguration} which enables <code>store by value</code> at construction.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
public final class CaffeineConfiguration<K, V> implements CompleteConfiguration<K, V> {

    private static final Factory<Scheduler> DISABLED_SCHEDULER = Scheduler::disabledScheduler;

    private static final Factory<Copier> JAVA_COPIER = JavaSerializationCopier::new;

    private static final Factory<Executor> COMMON_POOL = ForkJoinPool::commonPool;

    private static final Factory<Ticker> SYSTEM_TICKER = Ticker::systemTicker;

    private static final long serialVersionUID = 1L;

    private final MutableConfiguration<K, V> delegate;

    private final boolean readOnly;

    @Nullable
    private Factory<Weigher<K, V>> weigherFactory;

    @Nullable
    private Factory<Expiry<K, V>> expiryFactory;

    private Factory<Scheduler> schedulerFactory;

    private Factory<Executor> executorFactory;

    private Factory<Copier> copierFactory;

    private Factory<Ticker> tickerFactory;

    @Nullable
    private Long refreshAfterWriteNanos;

    @Nullable
    private Long expireAfterAccessNanos;

    @Nullable
    private Long expireAfterWriteNanos;

    @Nullable
    private Long maximumWeight;

    @Nullable
    private Long maximumSize;

    private boolean nativeStatistics;

    public CaffeineConfiguration() {
        delegate = new MutableConfiguration<>();
        delegate.setStoreByValue(false);
        schedulerFactory = DISABLED_SCHEDULER;
        tickerFactory = SYSTEM_TICKER;
        executorFactory = COMMON_POOL;
        copierFactory = JAVA_COPIER;
        readOnly = false;
    }

    /**
     * Returns a modifiable copy of the configuration.
     */
    public CaffeineConfiguration(CompleteConfiguration<K, V> configuration) {
        this(configuration, /* readOnly= */
        false);
    }

    private CaffeineConfiguration(CompleteConfiguration<K, V> configuration, boolean readOnly) {
        delegate = new MutableConfiguration<>(configuration);
        if (configuration instanceof CaffeineConfiguration<?, ?>) {
            var config = (CaffeineConfiguration<K, V>) configuration;
            refreshAfterWriteNanos = config.refreshAfterWriteNanos;
            expireAfterAccessNanos = config.expireAfterAccessNanos;
            expireAfterWriteNanos = config.expireAfterWriteNanos;
            nativeStatistics = config.nativeStatistics;
            schedulerFactory = config.schedulerFactory;
            executorFactory = config.executorFactory;
            expiryFactory = config.expiryFactory;
            copierFactory = config.copierFactory;
            tickerFactory = config.tickerFactory;
            weigherFactory = config.weigherFactory;
            maximumWeight = config.maximumWeight;
            maximumSize = config.maximumSize;
        } else {
            schedulerFactory = DISABLED_SCHEDULER;
            tickerFactory = SYSTEM_TICKER;
            executorFactory = COMMON_POOL;
            copierFactory = JAVA_COPIER;
        }
        this.readOnly = readOnly;
    }

    public CaffeineConfiguration<K, V> immutableCopy() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private void checkIfReadOnly() {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Class<K> getKeyType() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Class<V> getValueType() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setTypes(Class<K> keyType, Class<V> valueType) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> addCacheEntryListenerConfiguration(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> removeCacheEntryListenerConfiguration(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Factory<CacheLoader<K, V>> getCacheLoaderFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setCacheLoaderFactory(@Nullable Factory<? extends CacheLoader<K, V>> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    public CacheWriter<K, V> getCacheWriter() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean hasCacheWriter() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setCacheWriterFactory(@Nullable Factory<? extends CacheWriter<? super K, ? super V>> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Factory<ExpiryPolicy> getExpiryPolicyFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setExpiryPolicyFactory(@Nullable Factory<? extends ExpiryPolicy> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isReadThrough() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setReadThrough(boolean isReadThrough) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isWriteThrough() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setWriteThrough(boolean isWriteThrough) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isStoreByValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setStoreByValue(boolean isStoreByValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isNativeStatisticsEnabled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setNativeStatisticsEnabled(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isStatisticsEnabled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setStatisticsEnabled(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isManagementEnabled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setManagementEnabled(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Factory<Copier> getCopierFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setCopierFactory(Factory<Copier> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Factory<Scheduler> getSchedulerFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setSchedulerFactory(Factory<Scheduler> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Factory<Ticker> getTickerFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setTickerFactory(Factory<Ticker> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Factory<Executor> getExecutorFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public CaffeineConfiguration<K, V> setExecutorFactory(Factory<Executor> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public OptionalLong getRefreshAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CaffeineConfiguration<K, V> setRefreshAfterWrite(OptionalLong refreshAfterWriteNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public OptionalLong getExpireAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CaffeineConfiguration<K, V> setExpireAfterWrite(OptionalLong expireAfterWriteNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public OptionalLong getExpireAfterAccess() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CaffeineConfiguration<K, V> setExpireAfterAccess(OptionalLong expireAfterAccessNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Optional<Factory<Expiry<K, V>>> getExpiryFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "unchecked" })
    public CaffeineConfiguration<K, V> setExpiryFactory(Optional<Factory<? extends Expiry<K, V>>> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public OptionalLong getMaximumSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CaffeineConfiguration<K, V> setMaximumSize(OptionalLong maximumSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public OptionalLong getMaximumWeight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CaffeineConfiguration<K, V> setMaximumWeight(OptionalLong maximumWeight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Optional<Factory<Weigher<K, V>>> getWeigherFactory() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "unchecked" })
    public CaffeineConfiguration<K, V> setWeigherFactory(Optional<Factory<? extends Weigher<K, V>>> factory) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private static final class UnmodifiableIterable<E> implements Iterable<E> {

        private final Iterable<E> delegate;

        private UnmodifiableIterable(Iterable<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<E> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<E> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
