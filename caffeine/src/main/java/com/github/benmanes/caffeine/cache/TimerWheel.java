/*
 * Copyright 2017 Ben Manes. All Rights Reserved.
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

import static com.github.benmanes.caffeine.cache.Caffeine.ceilingPowerOfTwo;
import static java.util.Objects.requireNonNull;
import java.lang.ref.ReferenceQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * A hierarchical timer wheel to add, remove, and fire expiration events in amortized O(1) time. The
 * expiration events are deferred until the timer is advanced, which is performed as part of the
 * cache's maintenance cycle.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("GuardedBy")
final class TimerWheel<K, V> implements Iterable<Node<K, V>> {

    /*
   * A timer wheel [1] stores timer events in buckets on a circular buffer. A bucket represents a
   * coarse time span, e.g. one minute, and holds a doubly-linked list of events. The wheels are
   * structured in a hierarchy (seconds, minutes, hours, days) so that events scheduled in the
   * distant future are cascaded to lower buckets when the wheels rotate. This allows for events
   * to be added, removed, and expired in O(1) time, where expiration occurs for the entire bucket,
   * and the penalty of cascading is amortized by the rotations.
   *
   * [1] Hashed and Hierarchical Timing Wheels
   * http://www.cs.columbia.edu/~nahum/w6998/papers/ton97-timing-wheels.pdf
   */
    static final int[] BUCKETS = { 64, 64, 32, 4, 1 };

    static final long[] SPANS = { // 1.07s
    ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1)), // 1.14m
    ceilingPowerOfTwo(TimeUnit.MINUTES.toNanos(1)), // 1.22h
    ceilingPowerOfTwo(TimeUnit.HOURS.toNanos(1)), // 1.63d
    ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)), // 6.5d
    BUCKETS[3] * ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)), // 6.5d
    BUCKETS[3] * ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)) };

    static final long[] SHIFT = { Long.numberOfTrailingZeros(SPANS[0]), Long.numberOfTrailingZeros(SPANS[1]), Long.numberOfTrailingZeros(SPANS[2]), Long.numberOfTrailingZeros(SPANS[3]), Long.numberOfTrailingZeros(SPANS[4]) };

    final Node<K, V>[][] wheel;

    long nanos;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    TimerWheel() {
        wheel = new Node[BUCKETS.length][];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new Node[BUCKETS[i]];
            for (int j = 0; j < wheel[i].length; j++) {
                wheel[i][j] = new Sentinel<>();
            }
        }
    }

    public void advance(BoundedLocalCache<K, V> cache, long currentTimeNanos) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("Varifier")
    void expire(BoundedLocalCache<K, V> cache, int index, long previousTicks, long delta) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void schedule(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void reschedule(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void deschedule(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("Varifier")
    Node<K, V> findBucket(@Var long time) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void link(Node<K, V> sentinel, Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void unlink(Node<K, V> node) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "IntLongMath", "Varifier" })
    public long getExpirationDelay() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("Varifier")
    long peekAhead(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Iterator<Node<K, V>> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Iterator<Node<K, V>> descendingIterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * An iterator with rough ordering that can be specialized for either direction.
     */
    abstract class Traverser implements Iterator<Node<K, V>> {

        final long expectedNanos;

        @Nullable
        Node<K, V> current;

        @Nullable
        Node<K, V> next;

        Traverser() {
            expectedNanos = nanos;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Node<K, V> next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Nullable
        Node<K, V> computeNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Returns if the iteration has completed.
         */
        abstract boolean isDone();

        /**
         * Returns the sentinel at the current wheel and bucket position.
         */
        abstract Node<K, V> sentinel();

        /**
         * Returns the node's successor, or the bucket's sentinel if at the end.
         */
        abstract Node<K, V> traverse(Node<K, V> node);

        /**
         * Returns the sentinel for the wheel's next bucket, or null if the wheel is exhausted.
         */
        @Nullable
        abstract Node<K, V> goToNextBucket();

        /**
         * Returns the sentinel for the next wheel's bucket position, or null if no more wheels.
         */
        @Nullable
        abstract Node<K, V> goToNextWheel();
    }

    final class AscendingIterator extends Traverser {

        int wheelIndex;

        int steps;

        @Override
        boolean isDone() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        Node<K, V> sentinel() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        Node<K, V> traverse(Node<K, V> node) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        Node<K, V> goToNextBucket() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        Node<K, V> goToNextWheel() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        int bucketIndex() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    final class DescendingIterator extends Traverser {

        int wheelIndex;

        int steps;

        DescendingIterator() {
            wheelIndex = wheel.length - 1;
        }

        @Override
        boolean isDone() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        Node<K, V> sentinel() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        Node<K, V> goToNextBucket() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        Node<K, V> goToNextWheel() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        Node<K, V> traverse(Node<K, V> node) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        int bucketIndex() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A sentinel for the doubly-linked list in the bucket.
     */
    static final class Sentinel<K, V> extends Node<K, V> {

        Node<K, V> prev;

        Node<K, V> next;

        Sentinel() {
            prev = next = this;
        }

        @Override
        public Node<K, V> getPreviousInVariableOrder() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings({ "DataFlowIssue", "NullAway" })
        @Override
        public void setPreviousInVariableOrder(@Nullable Node<K, V> prev) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Node<K, V> getNextInVariableOrder() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @SuppressWarnings({ "DataFlowIssue", "NullAway" })
        @Override
        public void setNextInVariableOrder(@Nullable Node<K, V> next) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public K getKey() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Object getKeyReference() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Object getKeyReferenceOrNull() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public V getValue() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public Object getValueReference() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean isAlive() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean isRetired() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public boolean isDead() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void retire() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void die() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
