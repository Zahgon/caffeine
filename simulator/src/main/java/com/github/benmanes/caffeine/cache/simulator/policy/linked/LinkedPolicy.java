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
package com.github.benmanes.caffeine.cache.simulator.policy.linked;

import static com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic.WEIGHTED;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.admission.Admitter;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.PolicySpec;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * A cache that uses a linked list, in either insertion or access order, to implement simple
 * page replacement algorithms.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(characteristics = WEIGHTED)
public final class LinkedPolicy implements Policy {

    final Long2ObjectMap<Node> data;

    final PolicyStats policyStats;

    final EvictionPolicy policy;

    final Admitter admitter;

    final long maximumSize;

    final boolean weighted;

    final Node sentinel;

    long currentSize;

    public LinkedPolicy(Config config, Set<Characteristic> characteristics, Admission admission, EvictionPolicy policy) {
        this.policyStats = new PolicyStats(admission.format(policy.label()));
        this.admitter = admission.from(config, policyStats);
        this.weighted = characteristics.contains(WEIGHTED);
        var settings = new BasicSettings(config);
        this.data = new Long2ObjectOpenHashMap<>();
        this.maximumSize = settings.maximumSize();
        this.sentinel = new Node();
        this.policy = policy;
    }

    public static Set<Policy> policies(Config config, Set<Characteristic> characteristics, EvictionPolicy policy) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public PolicyStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void record(AccessEvent event) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Evicts while the map exceeds the maximum capacity.
     */
    private void evict(Node candidate) {
        if (currentSize > maximumSize) {
            while (currentSize > maximumSize) {
                if (candidate.weight > maximumSize) {
                    evictEntry(candidate);
                    continue;
                }
                Node victim = policy.findVictim(sentinel, policyStats);
                boolean admit = admitter.admit(candidate.key, victim.key);
                if (admit) {
                    evictEntry(victim);
                } else {
                    evictEntry(candidate);
                }
            }
        } else {
            policyStats.recordOperation();
        }
    }

    private void evictEntry(Node node) {
        policyStats.recordEviction();
        currentSize -= node.weight;
        data.remove(node.key);
        node.remove();
    }

    /**
     * The replacement policy.
     */
    public enum EvictionPolicy {

        /**
         * Evicts entries based on insertion order.
         */
        FIFO {

            @Override
            void onAccess(Node node, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            Node findVictim(Node sentinel, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
        ,
        /**
         * Evicts entries based on insertion order, but gives an entry a "second chance" if it has been
         * requested recently.
         */
        CLOCK {

            @Override
            void onAccess(Node node, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            Node findVictim(Node sentinel, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
        ,
        /**
         * Evicts entries based on how recently they are used, with the most recent evicted first.
         */
        MRU {

            @Override
            void onAccess(Node node, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            Node findVictim(Node sentinel, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
        ,
        /**
         * Evicts entries based on how recently they are used, with the least recent evicted first.
         */
        LRU {

            @Override
            void onAccess(Node node, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @Override
            Node findVictim(Node sentinel, PolicyStats policyStats) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
        ;

        public String label() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Performs any operations required by the policy after a node was successfully retrieved.
         */
        abstract void onAccess(Node node, PolicyStats policyStats);

        /**
         * Returns the victim entry to evict.
         */
        abstract Node findVictim(Node sentinel, PolicyStats policyStats);
    }

    /**
     * A node on the double-linked list.
     */
    static final class Node {

        final Node sentinel;

        @Nullable
        Node prev;

        @Nullable
        Node next;

        long key;

        int weight;

        boolean marked;

        /**
         * Creates a new sentinel node.
         */
        public Node() {
            this.key = Long.MIN_VALUE;
            this.sentinel = this;
            this.prev = this;
            this.next = this;
        }

        /**
         * Creates a new, unlinked node.
         */
        public Node(long key, int weight, Node sentinel) {
            this.sentinel = sentinel;
            this.weight = weight;
            this.key = key;
        }

        public void appendToTail() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void moveToTail() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
