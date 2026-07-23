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
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.copy.Copier;
import com.github.benmanes.caffeine.jcache.event.EventDispatcher;
import com.github.benmanes.caffeine.jcache.event.Registration;
import com.github.benmanes.caffeine.jcache.integration.DisabledCacheWriter;
import com.github.benmanes.caffeine.jcache.management.JCacheMXBean;
import com.github.benmanes.caffeine.jcache.management.JCacheStatisticsMXBean;
import com.github.benmanes.caffeine.jcache.management.JmxRegistration;
import com.github.benmanes.caffeine.jcache.management.JmxRegistration.MBeanType;
import com.github.benmanes.caffeine.jcache.processor.EntryProcessorEntry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;

/**
 * An implementation of JSR-107 {@link Cache} backed by a Caffeine cache.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CacheProxy<K, V> implements Cache<K, V> {

    private static final Logger logger = System.getLogger(CacheProxy.class.getName());

    protected final com.github.benmanes.caffeine.cache.Cache<K, @Nullable Expirable<V>> cache;

    protected final Optional<CacheLoader<K, V>> cacheLoader;

    protected final Set<CompletableFuture<?>> inFlight;

    protected final JCacheStatisticsMXBean statistics;

    protected final EventDispatcher<K, V> dispatcher;

    protected final Executor executor;

    protected final Ticker ticker;

    private final CaffeineConfiguration<K, V> configuration;

    private final CacheManager cacheManager;

    private final CacheWriter<K, V> writer;

    private final JCacheMXBean cacheMxBean;

    private final ExpiryPolicy expiry;

    private final Copier copier;

    private final String name;

    private volatile boolean closed;

    @SuppressWarnings({ "PMD.ExcessiveParameterList", "this-escape", "TooManyParameters" })
    public CacheProxy(String name, Executor executor, CacheManager cacheManager, CaffeineConfiguration<K, V> configuration, com.github.benmanes.caffeine.cache.Cache<K, @Nullable Expirable<V>> cache, EventDispatcher<K, V> dispatcher, Optional<CacheLoader<K, V>> cacheLoader, ExpiryPolicy expiry, Ticker ticker, JCacheStatisticsMXBean statistics) {
        this.writer = requireNonNullElse(configuration.getCacheWriter(), DisabledCacheWriter.get());
        this.configuration = requireNonNull(configuration);
        this.cacheManager = requireNonNull(cacheManager);
        this.cacheLoader = requireNonNull(cacheLoader);
        this.dispatcher = requireNonNull(dispatcher);
        this.statistics = requireNonNull(statistics);
        this.executor = requireNonNull(executor);
        this.expiry = requireNonNull(expiry);
        this.ticker = requireNonNull(ticker);
        this.cache = requireNonNull(cache);
        this.name = requireNonNull(name);
        copier = configuration.isStoreByValue() ? configuration.getCopierFactory().create() : Copier.identity();
        cacheMxBean = new JCacheMXBean(this);
        inFlight = ConcurrentHashMap.newKeySet();
    }

    @Override
    public boolean containsKey(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V get(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected Map<K, Expirable<V>> getAndFilterExpiredEntries(Set<? extends K> keys, boolean updateAccessTime) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "CollectionUndefinedEquality", "FutureReturnValueIgnored" })
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, @Nullable CompletionListener completionListener) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Performs the bulk load where the existing entries are replaced.
     */
    private void loadAllAndReplaceExisting(Set<? extends K> keys) {
        Map<K, V> loaded = cacheLoader.orElseThrow().loadAll(keys);
        for (var entry : loaded.entrySet()) {
            putNoCopyOrAwait(entry.getKey(), entry.getValue(), /* publishToWriter= */
            false);
        }
    }

    /**
     * Performs the bulk load where the existing entries are retained.
     */
    @SuppressWarnings("ConstantValue")
    private void loadAllAndKeepExisting(Set<? extends K> keys) {
        List<K> keysToLoad = keys.stream().filter(key -> !cache.asMap().containsKey(key)).collect(toUnmodifiableList());
        Map<K, V> result = cacheLoader.orElseThrow().loadAll(keysToLoad);
        for (var entry : result.entrySet()) {
            if ((entry.getKey() != null) && (entry.getValue() != null)) {
                putIfAbsentNoAwait(entry.getKey(), entry.getValue(), /* publishToWriter= */
                false);
            }
        }
    }

    @Override
    public void put(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getAndPut(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    protected PutResult<V> putNoCopyOrAwait(K key, V value, boolean publishToWriter) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Associates the specified value with the specified key in the cache if there is no existing
     * mapping.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param publishToWriter if the writer should be notified
     * @return if the mapping was successful
     */
    @CanIgnoreReturnValue
    private boolean putIfAbsentNoAwait(K key, V value, boolean publishToWriter) {
        boolean[] absent = { false };
        cache.asMap().compute(copyOf(key), (K k, @Var Expirable<V> expirable) -> {
            if ((expirable != null) && !expirable.isEternal() && expirable.hasExpired(currentTimeMillis())) {
                dispatcher.publishExpired(this, key, expirable.get());
                statistics.recordEvictions(1L);
                expirable = null;
            }
            if (expirable != null) {
                return expirable;
            }
            if (publishToWriter) {
                publishToCacheWriter(writer::write, () -> new EntryProxy<>(key, value));
            }
            absent[0] = true;
            V copy = copyOf(value);
            long expireTimeMillis = getWriteExpireTimeMillis(/* created= */
            true);
            if (expireTimeMillis == 0) {
                // The TCK asserts that a create is not published in
                // CacheExpiryTest.expire_whenCreated_CreatedExpiryPolicy()
                dispatcher.publishExpired(this, key, copy);
                return null;
            } else {
                dispatcher.publishCreated(this, key, copy);
                return new Expirable<>(copy, expireTimeMillis);
            }
        });
        return absent[0];
    }

    @Override
    public boolean remove(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Removes the mapping from the cache without store-by-value copying nor waiting for synchronous
     * listeners to complete.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the old value
     */
    @Nullable
    private V removeNoCopyOrAwait(K key) {
        @SuppressWarnings("unchecked")
        var removed = (V[]) new Object[1];
        cache.asMap().computeIfPresent(key, (k, expirable) -> {
            if (!expirable.isEternal() && expirable.hasExpired(currentTimeMillis())) {
                dispatcher.publishExpired(this, key, expirable.get());
                statistics.recordEvictions(1L);
            } else {
                dispatcher.publishRemoved(this, key, expirable.get());
                removed[0] = expirable.get();
            }
            return null;
        });
        return removed[0];
    }

    @Override
    @CanIgnoreReturnValue
    public boolean remove(K key, V oldValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getAndRemove(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean replace(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getAndReplace(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Replaces the entry for the specified key only if it is currently mapped to some value. The
     * entry is not store-by-value copied nor does the method wait for synchronous listeners to
     * complete.
     *
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the old value
     */
    @Nullable
    private V replaceNoCopyOrAwait(K key, V value) {
        requireNonNull(value);
        V copy = copyOf(value);
        @SuppressWarnings("unchecked")
        var replaced = (V[]) new Object[1];
        cache.asMap().computeIfPresent(key, (k, expirable) -> {
            if (!expirable.isEternal() && expirable.hasExpired(currentTimeMillis())) {
                dispatcher.publishExpired(this, key, expirable.get());
                statistics.recordEvictions(1L);
                return null;
            }
            publishToCacheWriter(writer::write, () -> new EntryProxy<>(key, value));
            @Var
            long expireTimeMillis = getWriteExpireTimeMillis(/* created= */
            false);
            if (expireTimeMillis == Long.MIN_VALUE) {
                expireTimeMillis = expirable.getExpireTimeMillis();
            }
            dispatcher.publishUpdated(this, key, expirable.get(), copy);
            replaced[0] = expirable.get();
            return new Expirable<>(copy, expireTimeMillis);
        });
        return replaced[0];
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("fallthrough")
    @Nullable
    Expirable<V> postProcess(@Nullable Expirable<V> expirable, EntryProcessorEntry<K, V> entry, @Var long currentTimeMillis) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @Nullable
    private Throwable shutdownExecutor() {
        if (executor instanceof ExecutorService) {
            @SuppressWarnings("PMD.CloseResource")
            var es = (ExecutorService) executor;
            es.shutdown();
        }
        @Var
        Throwable thrown = null;
        try {
            CompletableFuture.allOf(inFlight.toArray(CompletableFuture[]::new)).get(10, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            thrown = e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            thrown = e;
        }
        inFlight.clear();
        return thrown;
    }

    /**
     * Attempts to close the resource. If an error occurs and an outermost exception is set, then adds
     * the error to the suppression list.
     *
     * @param o the resource to close if Closeable
     * @param outer the outermost error, or null if unset
     * @return the outermost error, or null if unset and successful
     */
    @Nullable
    private static Throwable tryClose(@Nullable Object o, @Nullable Throwable outer) {
        if (o instanceof AutoCloseable) {
            try {
                ((AutoCloseable) o).close();
            } catch (Throwable t) {
                if (outer == null) {
                    return t;
                }
                outer.addSuppressed(t);
            }
        }
        return outer;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Iterator<Cache.Entry<K, V>> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void enableManagement(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void enableStatistics(boolean enabled) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Performs the action with the cache writer if write-through is enabled.
     */
    private <T> void publishToCacheWriter(Consumer<T> action, Supplier<T> data) {
        if (!configuration.isWriteThrough()) {
            return;
        }
        try {
            action.accept(data.get());
        } catch (CacheWriterException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CacheWriterException("Exception in CacheWriter", e);
        }
    }

    protected final void requireNotClosed() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "DataFlowIssue", "NullAway" })
    protected final <T> T copyOf(@Nullable T object) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "DataFlowIssue", "NullAway" })
    protected final V copyValue(@Nullable Expirable<V> expirable) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("CollectorMutability")
    protected final Map<K, V> copyMap(Map<K, Expirable<V>> map) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected final long currentTimeMillis() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected static long nanosToMillis(long nanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected final void setAccessExpireTime(K key, Expirable<?> expirable, @Var long currentTimeMillis) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected final long getWriteExpireTimeMillis(boolean created) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * An iterator to safely expose the cache entries.
     */
    final class EntryIterator implements Iterator<Cache.Entry<K, V>> {

        // NullAway does not yet understand the @NonNull annotation in the return type of asMap.
        @SuppressWarnings("NullAway")
        final Iterator<Map.Entry<K, Expirable<V>>> delegate = cache.asMap().entrySet().iterator();

        Map.@Nullable Entry<K, Expirable<V>> current;

        Map.@Nullable Entry<K, Expirable<V>> cursor;

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Cache.Entry<K, V> next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    protected static final class PutResult<V> {

        @Nullable
        V oldValue;

        boolean written;
    }

    protected enum NullCompletionListener implements CompletionListener {

        INSTANCE;

        @Override
        public void onCompletion() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void onException(Exception e) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
