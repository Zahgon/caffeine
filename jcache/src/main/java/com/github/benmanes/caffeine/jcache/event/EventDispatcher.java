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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import javax.cache.Cache;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.EventType;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * A dispatcher that publishes cache events to listeners for asynchronous execution.
 * <p>
 * A {@link CacheEntryListener} is required to receive events in the order of the actions being
 * performed on the associated key. This implementation supports this by using a dispatch queue for
 * each listener and key pair, and provides the following characteristics:
 * <ul>
 *   <li>A listener may be executed in parallel for events with different keys
 *   <li>A listener is executed sequentially for events with the same key. This creates a dependency
 *       relationship between events and waiting dependents do not consume threads.
 *   <li>A listener receives a single event per invocation; batch processing is not supported
 *   <li>Multiple listeners may be executed in parallel for the same event
 *   <li>Listeners process events at their own rate and do not explicitly block each other
 *   <li>Listeners share a pool of threads for event processing. A slow listener may limit the
 *       throughput if all threads are busy handling distinct events, causing the execution of other
 *       listeners to be delayed until the executor is able to process the work.
 * </ul>
 * <p>
 * Some listeners may be configured as <code>synchronous</code>, meaning that the publishing thread
 * should wait until the listener has processed the event. The calling thread should publish within
 * an atomic block that mutates the entry, and complete the operation by calling
 * {@link #awaitSynchronous()} or {@link #ignoreSynchronous()}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class EventDispatcher<K, V> {

    static final Logger logger = System.getLogger(EventDispatcher.class.getName());

    final ConcurrentMap<Registration<K, V>, ConcurrentMap<K, CompletableFuture<@Nullable Void>>> dispatchQueues;

    final ThreadLocal<List<CompletableFuture<@Nullable Void>>> pending;

    final Executor executor;

    public EventDispatcher(Executor executor) {
        this.pending = ThreadLocal.withInitial(ArrayList::new);
        this.dispatchQueues = new ConcurrentHashMap<>();
        this.executor = requireNonNull(executor);
    }

    public Set<Registration<K, V>> registrations() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.CloseResource")
    public void register(CacheEntryListenerConfiguration<K, V> configuration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void deregister(CacheEntryListenerConfiguration<K, V> configuration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishCreated(Cache<K, V> cache, K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishUpdated(Cache<K, V> cache, K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishRemoved(Cache<K, V> cache, K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishRemovedQuietly(Cache<K, V> cache, K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishExpired(Cache<K, V> cache, K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void publishExpiredQuietly(Cache<K, V> cache, K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void awaitSynchronous() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void ignoreSynchronous() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Broadcasts the event to the interested listener's dispatch queues.
     */
    @SuppressWarnings("FutureReturnValueIgnored")
    private void publish(Cache<K, V> cache, EventType eventType, K key, boolean hasOldValue, @Nullable V oldValue, @Nullable V newValue, boolean quiet) {
        if (dispatchQueues.isEmpty()) {
            return;
        }
        @Var
        JCacheEntryEvent<K, V> event = null;
        for (var entry : dispatchQueues.entrySet()) {
            var registration = entry.getKey();
            if (!registration.getCacheEntryListener().isCompatible(eventType)) {
                continue;
            }
            if (event == null) {
                event = new JCacheEntryEvent<>(cache, eventType, key, hasOldValue, oldValue, newValue);
            }
            if (!registration.getCacheEntryFilter().evaluate(event)) {
                continue;
            }
            JCacheEntryEvent<K, V> e = event;
            var dispatchQueue = entry.getValue();
            var future = dispatchQueue.compute(key, (k, queue) -> {
                Runnable action = () -> registration.getCacheEntryListener().dispatch(e);
                return (queue == null) ? CompletableFuture.runAsync(action, executor) : queue.thenRunAsync(action, executor);
            });
            future.whenComplete((result, error) -> {
                // optimistic check to avoid locking if not a match
                if (dispatchQueue.get(key) == future) {
                    dispatchQueue.remove(key, future);
                }
            });
            if (registration.isSynchronous() && !quiet) {
                pending.get().add(future);
            }
        }
    }
}
