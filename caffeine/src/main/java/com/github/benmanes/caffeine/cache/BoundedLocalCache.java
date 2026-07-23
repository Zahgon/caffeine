/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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

import static com.github.benmanes.caffeine.cache.Async.ASYNC_EXPIRY;
import static com.github.benmanes.caffeine.cache.Caffeine.calculateHashMapCapacity;
import static com.github.benmanes.caffeine.cache.Caffeine.ceilingPowerOfTwo;
import static com.github.benmanes.caffeine.cache.Caffeine.requireArgument;
import static com.github.benmanes.caffeine.cache.Caffeine.toNanosSaturated;
import static com.github.benmanes.caffeine.cache.LocalLoadingCache.newBulkMappingFunction;
import static com.github.benmanes.caffeine.cache.LocalLoadingCache.newMappingFunction;
import static com.github.benmanes.caffeine.cache.Node.PROBATION;
import static com.github.benmanes.caffeine.cache.Node.PROTECTED;
import static com.github.benmanes.caffeine.cache.Node.WINDOW;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Async.AsyncExpiry;
import com.github.benmanes.caffeine.cache.LinkedDeque.PeekingIterator;
import com.github.benmanes.caffeine.cache.Policy.CacheEntry;
import com.github.benmanes.caffeine.cache.References.InternalReference;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.annotations.concurrent.GuardedBy;

/**
 * An in-memory cache implementation that supports full concurrency of retrievals, a high expected
 * concurrency for updates, and multiple ways to bound the cache.
 * <p>
 * This class is abstract and code generated subclasses provide the complete implementation for a
 * particular configuration. This is to ensure that only the fields and execution paths necessary
 * for a given configuration are used.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
@SuppressWarnings({ "RedundantSuppression", "ResultOfMethodCallIgnored", "serial", "unused" })
abstract class BoundedLocalCache<K, V> extends BLCHeader.DrainStatusRef implements LocalCache<K, V> {

    /*
   * This class performs a best-effort bounding of a ConcurrentHashMap using a page-replacement
   * algorithm to determine which entries to evict when the capacity is exceeded.
   *
   * Concurrency:
   * ------------
   * The page replacement algorithms are kept eventually consistent with the map. An update to the
   * map and recording of reads may not be immediately reflected in the policy's data structures.
   * These structures are guarded by a lock, and operations are applied in batches to avoid lock
   * contention. The penalty of applying the batches is spread across threads, so that the amortized
   * cost is slightly higher than performing just the ConcurrentHashMap operation [1].
   *
   * A memento of the reads and writes that were performed on the map is recorded in buffers. These
   * buffers are drained at the first opportunity after a write or when a read buffer is full. The
   * reads are offered to a buffer that will reject additions if contended on or if it is full. Due
   * to the concurrent nature of the read and write operations, a strict policy ordering is not
   * possible, but it may be observably strict when single-threaded. The buffers are drained
   * asynchronously to minimize the request latency and uses a state machine to determine when to
   * schedule this work on an executor.
   *
   * Due to a lack of a strict ordering guarantee, a task can be executed out-of-order, such as a
   * removal followed by its addition. The state of the entry is encoded using the key field to
   * avoid additional memory usage. An entry is "alive" if it is in both the hash table and the page
   * replacement policy. It is "retired" if it is not in the hash table and is pending removal from
   * the page replacement policy. Finally, an entry transitions to the "dead" state when it is
   * neither in the hash table nor the page replacement policy. Both the retired and dead states are
   * represented by a sentinel key that should not be used for map operations.
   *
   * Eviction:
   * ---------
   * Maximum size is implemented using the Window TinyLfu policy [2] due to its high hit rate, O(1)
   * time complexity, and small footprint. A new entry starts in the admission window and remains
   * there as long as it has high temporal locality (recency). Eventually an entry will slip from
   * the window into the main space. If the main space is already full, then a historic frequency
   * filter determines whether to evict the newly admitted entry or the victim entry chosen by the
   * eviction policy. This process ensures that the entries in the window were very recently used,
   * while entries in the main space are accessed very frequently and remain moderately recent. The
   * windowing allows the policy to have a high hit rate when entries exhibit a bursty access
   * pattern, while the filter ensures that popular items are retained. The admission window uses
   * LRU and the main space uses Segmented LRU.
   *
   * The optimal size of the window vs. main spaces is workload dependent [3]. A large admission
   * window is favored by recency-biased workloads, while a small one favors frequency-biased
   * workloads. When the window is too small, then recent arrivals are prematurely evicted, but when
   * it is too large, then they pollute the cache and force the eviction of more popular entries.
   * The optimal configuration is dynamically determined by using hill climbing to walk the hit rate
   * curve. This is achieved by sampling the hit rate and adjusting the window size in the direction
   * that is improving (making positive or negative steps). At each interval, the step size is
   * decreased until the hit rate climber converges at the optimal setting. The process is restarted
   * when the hit rate changes over a threshold, indicating that the workload altered, and a new
   * setting may be required.
   *
   * The historic usage is retained in a compact popularity sketch, which uses hashing to
   * probabilistically estimate an item's frequency. This exposes a flaw where an adversary could
   * use hash flooding [4] to artificially raise the frequency of the main space's victim and cause
   * all candidates to be rejected. In the worst case, by exploiting hash collisions, an attacker
   * could cause the cache to never hit and hold only worthless items, resulting in a
   * denial-of-service attack against the underlying resource. This is mitigated by introducing
   * jitter, allowing candidates that are at least moderately popular to have a small, random chance
   * of being admitted. This causes the victim to be evicted, but in a way that marginally impacts
   * the hit rate.
   *
   * Expiration:
   * -----------
   * Expiration is implemented in O(1) time complexity. The time-to-idle policy uses an access-order
   * queue, the time-to-live policy uses a write-order queue, and variable expiration uses a
   * hierarchical timer wheel [5]. The queuing policies allow for peeking at the oldest entry to
   * determine if it has expired. If it has not, then the younger entries must not have expired
   * either. If a maximum size is set, then expiration will share the queues, minimizing the
   * per-entry footprint. The timer wheel based policy uses hashing and cascading in a manner that
   * amortizes the penalty of sorting to achieve a similar algorithmic cost.
   *
   * The expiration updates are applied in a best effort fashion. The reordering of variable or
   * access-order expiration may be discarded by the read buffer if it is full or contended.
   * Similarly, recording the touch for expiration to extend its lifetime may be ignored for an
   * entry if the last update was within a short time window. This is done to avoid overwhelming the
   * write buffer and to avoid false sharing on reads due to modifying the access time. The
   * expiration scan compensates by moving a stale-positioned head to the back of the queue when its
   * timestamp is fresher than the tail, so subsequent passes can resume from the next-oldest entry.
   *
   * [1] BP-Wrapper: A Framework Making Any Replacement Algorithms (Almost) Lock Contention Free
   * https://web.njit.edu/~dingxn/papers/BP-Wrapper.pdf
   * [2] TinyLFU: A Highly Efficient Cache Admission Policy
   * https://dl.acm.org/citation.cfm?id=3149371
   * [3] Adaptive Software Cache Management
   * https://dl.acm.org/citation.cfm?id=3274816
   * [4] Denial of Service via Algorithmic Complexity Attack
   * https://www.usenix.org/legacy/events/sec03/tech/full_papers/crosby/crosby.pdf
   * [5] Hashed and Hierarchical Timing Wheels
   * http://www.cs.columbia.edu/~nahum/w6998/papers/ton97-timing-wheels.pdf
   */
    static final Logger logger = System.getLogger(BoundedLocalCache.class.getName());

    /**
     * The number of CPUs
     */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The initial capacity of the write buffer.
     */
    static final int WRITE_BUFFER_MIN = 4;

    /**
     * The maximum capacity of the write buffer.
     */
    static final int WRITE_BUFFER_MAX = 128 * ceilingPowerOfTwo(NCPU);

    /**
     * The number of attempts to insert into the write buffer before yielding.
     */
    static final int WRITE_BUFFER_RETRIES = 100;

    /**
     * The maximum weighted capacity of the map.
     */
    static final long MAXIMUM_CAPACITY = Long.MAX_VALUE - Integer.MAX_VALUE;

    /**
     * The initial percent of the maximum weighted capacity dedicated to the main space.
     */
    static final double PERCENT_MAIN = 0.99d;

    /**
     * The percent of the maximum weighted capacity dedicated to the main's protected space.
     */
    static final double PERCENT_MAIN_PROTECTED = 0.80d;

    /**
     * The difference in hit rates that restarts the climber.
     */
    static final double HILL_CLIMBER_RESTART_THRESHOLD = 0.05d;

    /**
     * The percent of the total size to adapt the window by.
     */
    static final double HILL_CLIMBER_STEP_PERCENT = 0.0625d;

    /**
     * The rate to decrease the step size to adapt by.
     */
    static final double HILL_CLIMBER_STEP_DECAY_RATE = 0.98d;

    /**
     * The minimum popularity for allowing randomized admission.
     */
    static final int ADMIT_HASHDOS_THRESHOLD = 6;

    /**
     * The maximum number of entries that can be transferred between queues.
     */
    static final int QUEUE_TRANSFER_THRESHOLD = 1_000;

    /**
     * The maximum time window between touches for expiration updates.
     */
    static final long EXPIRE_TOLERANCE = TimeUnit.SECONDS.toNanos(1);

    /**
     * The maximum duration before an entry expires.
     */
    // 150 years
    static final long MAXIMUM_EXPIRY = (Long.MAX_VALUE >> 1);

    /**
     * The duration to wait on the eviction lock before warning of a possible misuse.
     */
    static final long WARN_AFTER_LOCK_WAIT_NANOS = TimeUnit.SECONDS.toNanos(30);

    /**
     * The number of retries before computing to validate the entry's integrity; pow2 modulus.
     */
    static final int MAX_PUT_SPIN_WAIT_ATTEMPTS = 1024 - 1;

    /**
     * The handle for the in-flight refresh operations.
     */
    static final VarHandle REFRESHES = findVarHandle(BoundedLocalCache.class, "refreshes", ConcurrentMap.class);

    @Nullable
    final RemovalListener<K, V> evictionListener;

    @Nullable
    final AsyncCacheLoader<K, V> cacheLoader;

    final MpscGrowableArrayQueue<Runnable> writeBuffer;

    final ConcurrentHashMap<Object, Node<K, V>> data;

    final PerformCleanupTask drainBuffersTask;

    final Consumer<Node<K, V>> accessPolicy;

    final Buffer<Node<K, V>> readBuffer;

    final NodeFactory<K, V> nodeFactory;

    final ReentrantLock evictionLock;

    final Weigher<K, V> weigher;

    final Executor executor;

    final boolean isWeighted;

    final boolean isAsync;

    @Nullable
    Set<K> keySet;

    @Nullable
    Collection<V> values;

    @Nullable
    Set<Entry<K, V>> entrySet;

    @Nullable
    volatile ConcurrentMap<Object, CompletableFuture<?>> refreshes;

    /**
     * Creates an instance based on the builder's configuration.
     */
    @SuppressWarnings("GuardedBy")
    protected BoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<K, V> cacheLoader, boolean isAsync) {
        this.isAsync = isAsync;
        this.cacheLoader = cacheLoader;
        executor = builder.getExecutor();
        isWeighted = builder.isWeighted();
        evictionLock = new ReentrantLock();
        weigher = builder.getWeigher(isAsync);
        drainBuffersTask = new PerformCleanupTask(this);
        nodeFactory = NodeFactory.newFactory(builder, isAsync);
        evictionListener = builder.getEvictionListener(isAsync);
        data = new ConcurrentHashMap<>(builder.getInitialCapacity());
        readBuffer = evicts() || collectKeys() || collectValues() || expiresAfterAccess() ? new BoundedBuffer<>() : Buffer.disabled();
        accessPolicy = (evicts() || expiresAfterAccess()) ? this::onAccess : e -> {
        };
        writeBuffer = new MpscGrowableArrayQueue<>(WRITE_BUFFER_MIN, WRITE_BUFFER_MAX);
        if (evicts()) {
            setMaximumSize(builder.getMaximum());
        }
    }

    void requireIsAlive(Object key, Node<?, ?> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void logIfAlive(Node<?, ?> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    String brokenEqualityMessage(Object key, Node<?, ?> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static RuntimeException toUncheckedException(Throwable t) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Shared --------------- */
    @Override
    public boolean isAsync() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    final boolean isComputingAsync(@Nullable V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected WriteOrderDeque<Node<K, V>> writeOrderDeque() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final Executor executor() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public ConcurrentMap<Object, CompletableFuture<?>> refreshes() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("RedundantCollectionOperation")
    void discardRefresh(Object keyReference) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Object referenceKey(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isPendingEviction(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Stats Support --------------- */
    @Override
    public boolean isRecordingStats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public StatsCounter statsCounter() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Ticker statsTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Removal Listener Support --------------- */
    @Nullable
    protected RemovalListener<K, V> removalListener() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Eviction Listener Support --------------- */
    void notifyEviction(@Nullable K key, @Nullable V value, RemovalCause cause) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Reference Support --------------- */
    @Override
    public boolean collectKeys() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean collectValues() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "DataFlowIssue", "NullAway" })
    protected ReferenceQueue<K> keyReferenceQueue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "DataFlowIssue", "NullAway" })
    protected ReferenceQueue<V> valueReferenceQueue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Expiration Support --------------- */
    @Nullable
    protected Pacer pacer() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean expiresVariable() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean expiresAfterAccess() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long expiresAfterAccessNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected void setExpiresAfterAccessNanos(long expireAfterAccessNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean expiresAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long expiresAfterWriteNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected void setExpiresAfterWriteNanos(long expireAfterWriteNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean refreshAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long refreshAfterWriteNanos() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected void setRefreshAfterWriteNanos(long refreshAfterWriteNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "DataFlowIssue", "NullAway" })
    public Expiry<K, V> expiry() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Ticker expirationTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected TimerWheel<K, V> timerWheel() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Eviction Support --------------- */
    protected boolean evicts() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean isWeighted() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected FrequencySketch frequencySketch() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean fastpath() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long maximum() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long maximumAcquire() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long windowMaximum() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long mainProtectedMaximum() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setMaximum(long maximum) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setWindowMaximum(long maximum) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setMainProtectedMaximum(long maximum) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long weightedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long weightedSizeAcquire() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long windowWeightedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long mainProtectedWeightedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setWeightedSize(long weightedSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setWindowWeightedSize(long weightedSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setMainProtectedWeightedSize(long weightedSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long hitsInSample() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long missesInSample() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected double stepSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected double previousSampleHitRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long adjustment() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setHitsInSample(long hitCount) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setMissesInSample(long missCount) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setStepSize(double stepSize) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setPreviousSampleHitRate(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    protected void setAdjustment(long amount) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @SuppressWarnings({ "ConstantValue", "Varifier" })
    void setMaximumSize(long maximum) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void evictEntries() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @Nullable
    Node<K, V> evictFromWindow() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void evictFromMain(@Var @Nullable Node<K, V> candidate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    boolean admit(Object candidateKeyRef, Object victimKeyRef) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void expireEntries() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void expireAfterAccessEntries(long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void expireAfterAccessEntries(long now, AccessOrderDeque<Node<K, V>> accessOrderDeque) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void expireAfterWriteEntries(long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void expireVariableEntries(long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    long getExpirationDelay(long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("ShortCircuitBoolean")
    boolean hasExpired(Node<K, V> node, long now, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @SuppressWarnings({ "GuardedByChecker", "SynchronizationOnLocalVariableOrMethodParameter" })
    boolean evictEntry(Node<K, V> node, RemovalCause cause, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @SuppressWarnings("UnnecessaryReturnStatement")
    void climb() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void determineAdjustment() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void increaseWindow() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void decreaseWindow() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void demoteFromMainProtected() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    V afterRead(Node<K, V> node, long now, boolean recordHit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean skipReadBuffer() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @Nullable
    V refreshIfNeeded(Node<K, V> node, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long expireAfterCreate(K key, V value, @Nullable Expiry<? super K, ? super V> expiry, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long expireAfterUpdate(Node<K, V> node, K key, V value, @Nullable Expiry<? super K, ? super V> expiry, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    long expireAfterRead(Node<K, V> node, K key, V value, Expiry<K, V> expiry, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void tryExpireAfterRead(Node<K, V> node, K key, V value, Expiry<K, V> expiry, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void setVariableTime(Node<K, V> node, long expirationTime) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void setWriteTime(Node<K, V> node, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void setAccessTime(Node<K, V> node, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    boolean exceedsWriteTimeTolerance(Node<K, V> node, long varTime, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void afterWrite(Runnable task) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void lock() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void scheduleAfterWrite() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void scheduleDrainBuffers() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void performCleanUp(@Nullable Runnable task) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    void rescheduleCleanUpIfIncomplete() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void maintenance(@Nullable Runnable task) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void drainKeyReferences() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void drainValueReferences() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void drainReadBuffer() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void onAccess(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void reorderProbation(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static <K, V> void reorder(LinkedDeque<Node<K, V>> deque, Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    void drainWriteBuffer() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    void makeDead(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Adds the node to the page replacement policy.
     */
    final class AddTask implements Runnable {

        final Node<K, V> node;

        final int weight;

        AddTask(Node<K, V> node, int weight) {
            this.weight = weight;
            this.node = node;
        }

        @Override
        @GuardedBy("evictionLock")
        public void run() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * Removes a node from the page replacement policy.
     */
    final class RemovalTask implements Runnable {

        final Node<K, V> node;

        RemovalTask(Node<K, V> node) {
            this.node = node;
        }

        @Override
        @GuardedBy("evictionLock")
        public void run() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * Updates the weighted size.
     */
    final class UpdateTask implements Runnable {

        final int weightDifference;

        final Node<K, V> node;

        public UpdateTask(Node<K, V> node, int weightDifference) {
            this.weightDifference = weightDifference;
            this.node = node;
        }

        @Override
        @GuardedBy("evictionLock")
        public void run() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /* --------------- Concurrent Map Support --------------- */
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long estimatedSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @GuardedBy("evictionLock")
    @SuppressWarnings({ "GuardedByChecker", "SynchronizationOnLocalVariableOrMethodParameter" })
    void removeNode(Node<K, V> node, long now) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V get(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getIfPresent(Object key, boolean recordStats) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V getIfPresentQuietly(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    public K getKey(K key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    V put(K key, V value, Expiry<K, V> expiry, boolean onlyIfAbsent) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Nullable
    public V remove(Object key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public boolean remove(Object key, @Nullable Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Nullable
    public V replace(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V computeIfAbsent(K key, @Var Function<? super K, ? extends @Nullable V> mappingFunction, boolean recordStats, boolean recordLoad) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Nullable
    V doComputeIfAbsent(K key, Object keyRef, Function<? super K, ? extends @Nullable V> mappingFunction, ComputeContext<K, V> ctx, boolean recordStats) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V compute(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, boolean recordLoad, boolean recordLoadFailure, boolean @Nullable [] preserveTimestamps) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "StatementWithEmptyBody", "SynchronizationOnLocalVariableOrMethodParameter" })
    @Nullable
    V remap(K key, Object keyRef, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, ComputeContext<K, V> ctx, boolean computeIfAbsent) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
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

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("GuardedByChecker")
    <T> T evictionOrder(boolean hottest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("GuardedByChecker")
    <T> T expireAfterAccessOrder(boolean oldest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    <T> T snapshot(Iterable<Node<K, V>> iterable, Function<@Nullable V, @Nullable V> transformer, Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Nullable
    CacheEntry<K, V> nodeToCacheEntry(Node<K, V> node, Function<@Nullable V, @Nullable V> transformer, int weight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Mutable context for passing state between a lambda and the caller.
     */
    static final class EvictContext<V> {

        @Nullable
        RemovalCause cause;

        @Nullable
        V value;

        boolean resurrect;

        boolean removed;

        int oldWeight;
    }

    /**
     * Mutable context for passing state between a lambda and the caller.
     */
    static final class RemoveContext<K, V> {

        @Nullable
        K oldKey;

        @Nullable
        V oldValue;

        @Nullable
        Node<K, V> node;

        @Nullable
        RemovalCause cause;

        int oldWeight;
    }

    /**
     * Mutable context for passing state between a lambda and the caller.
     */
    static final class ReplaceContext<K, V> {

        @Nullable
        K nodeKey;

        @Nullable
        V oldValue;

        long now;

        int oldWeight;

        boolean exceedsTolerance;
    }

    /**
     * Mutable context for passing state between a lambda and the caller.
     */
    static final class ComputeContext<K, V> {

        @Nullable
        K nodeKey;

        @Nullable
        V oldValue;

        @Nullable
        V newValue;

        @Nullable
        Node<K, V> removed;

        @Nullable
        RemovalCause cause;

        @Nullable
        Throwable exception;

        boolean @Nullable [] preserveTimestamps;

        long now;

        int oldWeight;

        int newWeight;

        boolean exceedsTolerance;

        ComputeContext(long now) {
            this.now = now;
        }
    }

    /**
     * A function that produces an unmodifiable map up to the limit in stream order.
     */
    static final class SizeLimiter<K, V> implements Function<Stream<CacheEntry<K, V>>, Map<K, V>> {

        private final int expectedSize;

        private final long limit;

        SizeLimiter(int expectedSize, long limit) {
            requireArgument(limit >= 0);
            this.expectedSize = expectedSize;
            this.limit = limit;
        }

        @Override
        public Map<K, V> apply(Stream<CacheEntry<K, V>> stream) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A function that produces an unmodifiable map up to the weighted limit in stream order.
     */
    static final class WeightLimiter<K, V> implements Function<Stream<CacheEntry<K, V>>, Map<K, V>> {

        private final long weightLimit;

        private long weightedSize;

        WeightLimiter(long weightLimit) {
            requireArgument(weightLimit >= 0);
            this.weightLimit = weightLimit;
        }

        @Override
        public Map<K, V> apply(Stream<CacheEntry<K, V>> stream) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the keys.
     */
    static final class KeySetView<K, V> extends AbstractSet<K> {

        final BoundedLocalCache<K, V> cache;

        KeySetView(BoundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super K> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<K> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<K> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the key iterator.
     */
    static final class KeyIterator<K, V> implements Iterator<K> {

        final EntryIterator<K, V> iterator;

        KeyIterator(BoundedLocalCache<K, V> cache) {
            this.iterator = new EntryIterator<>(cache);
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public K next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the key spliterator.
     */
    static final class KeySpliterator<K, V> implements Spliterator<K> {

        final Spliterator<Node<K, V>> spliterator;

        final BoundedLocalCache<K, V> cache;

        KeySpliterator(BoundedLocalCache<K, V> cache) {
            this(cache, cache.data.values().spliterator());
        }

        KeySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
            this.spliterator = requireNonNull(spliterator);
            this.cache = requireNonNull(cache);
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Spliterator<K> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the values.
     */
    static final class ValuesView<K, V> extends AbstractCollection<V> {

        final BoundedLocalCache<K, V> cache;

        ValuesView(BoundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean remove(@Nullable Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<V> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<V> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the value iterator.
     */
    static final class ValueIterator<K, V> implements Iterator<V> {

        final EntryIterator<K, V> iterator;

        ValueIterator(BoundedLocalCache<K, V> cache) {
            this.iterator = new EntryIterator<>(cache);
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public V next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the value spliterator.
     */
    static final class ValueSpliterator<K, V> implements Spliterator<V> {

        final Spliterator<Node<K, V>> spliterator;

        final BoundedLocalCache<K, V> cache;

        ValueSpliterator(BoundedLocalCache<K, V> cache) {
            this(cache, cache.data.values().spliterator());
        }

        ValueSpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
            this.spliterator = requireNonNull(spliterator);
            this.cache = requireNonNull(cache);
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Spliterator<V> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entries.
     */
    static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {

        final BoundedLocalCache<K, V> cache;

        EntrySetView(BoundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entry iterator.
     */
    static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {

        final BoundedLocalCache<K, V> cache;

        final Iterator<Node<K, V>> iterator;

        @Nullable
        K key;

        @Nullable
        V value;

        @Nullable
        K removalKey;

        @Nullable
        Node<K, V> next;

        EntryIterator(BoundedLocalCache<K, V> cache) {
            this.iterator = cache.data.values().iterator();
            this.cache = cache;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        void advance() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        K nextKey() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        V nextValue() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Entry<K, V> next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * An adapter to safely externalize the entry spliterator.
     */
    static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {

        final Spliterator<Node<K, V>> spliterator;

        final BoundedLocalCache<K, V> cache;

        EntrySpliterator(BoundedLocalCache<K, V> cache) {
            this(cache, cache.data.values().spliterator());
        }

        EntrySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
            this.spliterator = requireNonNull(spliterator);
            this.cache = requireNonNull(cache);
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Spliterator<Entry<K, V>> trySplit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public long estimateSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int characteristics() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A reusable task that performs the maintenance work; used to avoid wrapping by ForkJoinPool.
     */
    static final class PerformCleanupTask extends ForkJoinTask<@Nullable Void> implements Runnable {

        private static final long serialVersionUID = 1L;

        final WeakReference<BoundedLocalCache<?, ?>> reference;

        PerformCleanupTask(BoundedLocalCache<?, ?> cache) {
            reference = new WeakReference<>(cache);
        }

        @Override
        public boolean exec() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * This method cannot be ignored due to being final, so a hostile user supplied Executor could
         * forcibly complete the task and halt future executions. There are easier ways to intentionally
         * harm a system, so this is assumed to not happen in practice.
         */
        // public final void quietlyComplete() {}
        @Override
        public void complete(@Nullable Void value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void setRawResult(@Nullable Void value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Void getRawResult() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void completeExceptionally(@Nullable Throwable t) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static <K, V> SerializationProxy<K, V> makeSerializationProxy(BoundedLocalCache<?, ?> cache) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /* --------------- Manual Cache --------------- */
    static class BoundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {

        private static final long serialVersionUID = 1;

        final BoundedLocalCache<K, V> cache;

        @Nullable
        Policy<K, V> policy;

        BoundedLocalManualCache(Caffeine<K, V> builder) {
            this(builder, null);
        }

        BoundedLocalManualCache(Caffeine<K, V> builder, @Nullable CacheLoader<? super K, V> loader) {
            cache = LocalCacheFactory.newBoundedLocalCache(builder, loader, /* isAsync= */
            false);
        }

        @Override
        public final BoundedLocalCache<K, V> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public final Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        private Object writeReplace() {
            return makeSerializationProxy(cache);
        }
    }

    @SuppressWarnings({ "NullableOptional", "OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType" })
    static final class BoundedPolicy<K, V> implements Policy<K, V> {

        final Function<@Nullable V, @Nullable V> transformer;

        final BoundedLocalCache<K, V> cache;

        final boolean isWeighted;

        @Nullable
        Optional<Eviction<K, V>> eviction;

        @Nullable
        Optional<FixedRefresh<K, V>> refreshes;

        @Nullable
        Optional<FixedExpiration<K, V>> afterWrite;

        @Nullable
        Optional<FixedExpiration<K, V>> afterAccess;

        @Nullable
        Optional<VarExpiration<K, V>> variable;

        BoundedPolicy(BoundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer, boolean isWeighted) {
            this.transformer = transformer;
            this.isWeighted = isWeighted;
            this.cache = cache;
        }

        @Override
        public boolean isRecordingStats() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public V getIfPresentQuietly(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("GuardedByChecker")
        @Override
        @Nullable
        public CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings("Java9CollectionFactory")
        @Override
        public Map<K, CompletableFuture<V>> refreshes() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<Eviction<K, V>> eviction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedExpiration<K, V>> expireAfterAccess() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedExpiration<K, V>> expireAfterWrite() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<VarExpiration<K, V>> expireVariably() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Optional<FixedRefresh<K, V>> refreshAfterWrite() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        final class BoundedEviction implements Eviction<K, V> {

            @Override
            public boolean isWeighted() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public OptionalInt weightOf(K key) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public OptionalLong weightedSize() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public long getMaximum() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public void setMaximum(long maximum) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> coldest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> coldestWeighted(long weightLimit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T coldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> hottest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> hottestWeighted(long weightLimit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T hottest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        @SuppressWarnings("PreferJavaTimeOverload")
        final class BoundedExpireAfterAccess implements FixedExpiration<K, V> {

            @Override
            public OptionalLong ageOf(K key, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public long getExpiresAfter(TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public void setExpiresAfter(long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> oldest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T oldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> youngest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T youngest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        @SuppressWarnings("PreferJavaTimeOverload")
        final class BoundedExpireAfterWrite implements FixedExpiration<K, V> {

            @Override
            public OptionalLong ageOf(K key, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public long getExpiresAfter(TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public void setExpiresAfter(long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> oldest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @SuppressWarnings("GuardedByChecker")
            @Override
            public <T> T oldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> youngest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @SuppressWarnings("GuardedByChecker")
            @Override
            public <T> T youngest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        @SuppressWarnings("PreferJavaTimeOverload")
        final class BoundedVarExpiration implements VarExpiration<K, V> {

            @Override
            public OptionalLong getExpiresAfter(K key, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public void setExpiresAfter(K key, long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @Nullable
            public V put(K key, V value, long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @Nullable
            public V putIfAbsent(K key, V value, long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Nullable
            V putSync(K key, V value, long duration, TimeUnit unit, boolean onlyIfAbsent) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @SuppressWarnings("unchecked")
            @Nullable
            V putIfAbsentAsync(K key, V value, long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @SuppressWarnings("unchecked")
            @Nullable
            V putAsync(K key, V value, long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            @Nullable
            public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Duration duration) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Nullable
            V computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Expiry<? super K, ? super V> expiry) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> oldest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T oldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public Map<K, V> youngest(int limit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public <T> T youngest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        static final class FixedExpireAfterWrite<K, V> implements Expiry<K, V> {

            final long duration;

            final TimeUnit unit;

            FixedExpireAfterWrite(long duration, TimeUnit unit) {
                this.duration = duration;
                this.unit = unit;
            }

            @Override
            public long expireAfterCreate(K key, V value, long currentTime) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            @Override
            public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        @SuppressWarnings("PreferJavaTimeOverload")
        final class BoundedRefreshAfterWrite implements FixedRefresh<K, V> {

            @Override
            public OptionalLong ageOf(K key, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public long getRefreshesAfter(TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            public void setRefreshesAfter(long duration, TimeUnit unit) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
    }

    /* --------------- Loading Cache --------------- */
    static final class BoundedLocalLoadingCache<K, V> extends BoundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {

        private static final long serialVersionUID = 1;

        final Function<K, @Nullable V> mappingFunction;

        @Nullable
        final Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

        BoundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> loader) {
            super(builder, loader);
            requireNonNull(loader);
            mappingFunction = newMappingFunction(loader);
            bulkMappingFunction = newBulkMappingFunction(loader);
        }

        @Override
        @SuppressWarnings({ "DataFlowIssue", "NullAway" })
        public AsyncCacheLoader<? super K, V> cacheLoader() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Function<K, @Nullable V> mappingFunction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        private Object writeReplace() {
            return makeSerializationProxy(cache);
        }
    }

    /* --------------- Async Cache --------------- */
    static final class BoundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {

        private static final long serialVersionUID = 1;

        final BoundedLocalCache<K, CompletableFuture<V>> cache;

        final boolean isWeighted;

        @Nullable
        ConcurrentMap<K, CompletableFuture<V>> mapView;

        @Nullable
        CacheView<K, V> cacheView;

        @Nullable
        Policy<K, V> policy;

        @SuppressWarnings("unchecked")
        BoundedLocalAsyncCache(Caffeine<K, V> builder) {
            cache = (BoundedLocalCache<K, CompletableFuture<V>>) LocalCacheFactory.newBoundedLocalCache(builder, /* cacheLoader= */
            null, /* isAsync= */
            true);
            isWeighted = builder.isWeighted();
        }

        @Override
        public BoundedLocalCache<K, CompletableFuture<V>> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ConcurrentMap<K, CompletableFuture<V>> asMap() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Cache<K, V> synchronous() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        private Object writeReplace() {
            return makeSerializationProxy(cache);
        }
    }

    /* --------------- Async Loading Cache --------------- */
    static final class BoundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        final BoundedLocalCache<K, CompletableFuture<V>> cache;

        final boolean isWeighted;

        @Nullable
        ConcurrentMap<K, CompletableFuture<V>> mapView;

        @Nullable
        Policy<K, V> policy;

        @SuppressWarnings("unchecked")
        BoundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
            super(loader);
            isWeighted = builder.isWeighted();
            cache = (BoundedLocalCache<K, CompletableFuture<V>>) LocalCacheFactory.newBoundedLocalCache(builder, loader, /* isAsync= */
            true);
        }

        @Override
        public BoundedLocalCache<K, CompletableFuture<V>> cache() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ConcurrentMap<K, CompletableFuture<V>> asMap() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Policy<K, V> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        private Object writeReplace() {
            return makeSerializationProxy(cache);
        }
    }
}

/**
 * The namespace for field padding through inheritance.
 */
@SuppressWarnings({ "IdentifierName", "MultiVariableDeclaration" })
final class BLCHeader {

    private BLCHeader() {
    }

    @SuppressWarnings("unused")
    static class PadDrainStatus {

        byte p000, p001, p002, p003, p004, p005, p006, p007;

        byte p008, p009, p010, p011, p012, p013, p014, p015;

        byte p016, p017, p018, p019, p020, p021, p022, p023;

        byte p024, p025, p026, p027, p028, p029, p030, p031;

        byte p032, p033, p034, p035, p036, p037, p038, p039;

        byte p040, p041, p042, p043, p044, p045, p046, p047;

        byte p048, p049, p050, p051, p052, p053, p054, p055;

        byte p056, p057, p058, p059, p060, p061, p062, p063;

        byte p064, p065, p066, p067, p068, p069, p070, p071;

        byte p072, p073, p074, p075, p076, p077, p078, p079;

        byte p080, p081, p082, p083, p084, p085, p086, p087;

        byte p088, p089, p090, p091, p092, p093, p094, p095;

        byte p096, p097, p098, p099, p100, p101, p102, p103;

        byte p104, p105, p106, p107, p108, p109, p110, p111;

        byte p112, p113, p114, p115, p116, p117, p118, p119;
    }

    /**
     * Enforces a memory layout to avoid false sharing by padding the drain status.
     */
    abstract static class DrainStatusRef extends PadDrainStatus {

        static final VarHandle DRAIN_STATUS = findVarHandle(DrainStatusRef.class, "drainStatus", int.class);

        /**
         * A drain is not taking place.
         */
        static final int IDLE = 0;

        /**
         * A drain is required due to a pending write modification.
         */
        static final int REQUIRED = 1;

        /**
         * A drain is in progress and will transition to idle.
         */
        static final int PROCESSING_TO_IDLE = 2;

        /**
         * A drain is in progress and will transition to required.
         */
        static final int PROCESSING_TO_REQUIRED = 3;

        /**
         * The draining status of the buffers.
         */
        volatile int drainStatus = IDLE;

        @SuppressWarnings("StatementSwitchToExpressionSwitch")
        boolean shouldDrainBuffers(boolean delayable) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        int drainStatusOpaque() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        int drainStatusAcquire() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        void setDrainStatusOpaque(int drainStatus) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        void setDrainStatusRelease(int drainStatus) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        boolean casDrainStatus(int expect, int update) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
