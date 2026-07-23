/*
 * Copyright 2022 Ben Manes. All Rights Reserved.
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

import static java.util.Objects.requireNonNull;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Policy.CacheEntry;
import com.google.errorprone.annotations.Immutable;

/**
 * An immutable entry that includes a snapshot of the policy metadata at its time of creation.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@Immutable(containerOf = { "K", "V" })
class SnapshotEntry<K, V> implements CacheEntry<K, V> {

    private final long snapshot;

    private final V value;

    private final K key;

    SnapshotEntry(K key, V value, long snapshot) {
        this.snapshot = snapshot;
        this.key = requireNonNull(key);
        this.value = requireNonNull(value);
    }

    @Override
    public final K getKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final V getValue() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int weight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long expiresAt() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long refreshableAt() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final long snapshotAt() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final int hashCode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("StatementSwitchToExpressionSwitch")
    public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static class WeightedEntry<K, V> extends SnapshotEntry<K, V> {

        final int weight;

        WeightedEntry(K key, V value, long snapshot, int weight) {
            super(key, value, snapshot);
            this.weight = weight;
        }

        @Override
        public final int weight() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static class ExpirableEntry<K, V> extends SnapshotEntry<K, V> {

        final long expiresAt;

        ExpirableEntry(K key, V value, long snapshot, long expiresAt) {
            super(key, value, snapshot);
            this.expiresAt = expiresAt;
        }

        @Override
        public final long expiresAt() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static class ExpirableWeightedEntry<K, V> extends WeightedEntry<K, V> {

        final long expiresAt;

        ExpirableWeightedEntry(K key, V value, long snapshot, int weight, long expiresAt) {
            super(key, value, snapshot, weight);
            this.expiresAt = expiresAt;
        }

        @Override
        public final long expiresAt() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static class RefreshableExpirableEntry<K, V> extends ExpirableEntry<K, V> {

        final long refreshableAt;

        RefreshableExpirableEntry(K key, V value, long snapshot, long expiresAt, long refreshableAt) {
            super(key, value, snapshot, expiresAt);
            this.refreshableAt = refreshableAt;
        }

        @Override
        public final long refreshableAt() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class CompleteEntry<K, V> extends ExpirableWeightedEntry<K, V> {

        final long refreshableAt;

        CompleteEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
            super(key, value, snapshot, weight, expiresAt);
            this.refreshableAt = refreshableAt;
        }

        @Override
        public long refreshableAt() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
