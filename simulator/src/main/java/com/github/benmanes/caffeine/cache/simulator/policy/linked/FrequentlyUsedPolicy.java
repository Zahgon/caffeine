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

import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.admission.Admitter;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.KeyOnlyPolicy;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Least/Most Frequency Used in O(1) time as described in <a href="http://dhruvbird.com/lfu.pdf"> An
 * O(1) algorithm for implementing the LFU cache eviction scheme</a>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class FrequentlyUsedPolicy implements KeyOnlyPolicy {

    final PolicyStats policyStats;

    final Long2ObjectMap<Node> data;

    final EvictionPolicy policy;

    final FrequencyNode freq0;

    final Admitter admitter;

    final int maximumSize;

    public FrequentlyUsedPolicy(Admission admission, EvictionPolicy policy, Config config) {
        var settings = new BasicSettings(config);
        this.policyStats = new PolicyStats(admission.format(policy.label()));
        this.maximumSize = Math.toIntExact(settings.maximumSize());
        this.admitter = admission.from(config, policyStats);
        this.data = new Long2ObjectOpenHashMap<>();
        this.policy = requireNonNull(policy);
        this.freq0 = new FrequencyNode();
    }

    public static Set<Policy> policies(Config config, EvictionPolicy policy) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public PolicyStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void record(long key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Moves the entry to the next higher frequency list, creating it if necessary.
     */
    private void onHit(Node node) {
        policyStats.recordHit();
        int newCount = node.freq.count + 1;
        var next = requireNonNull(node.freq.next);
        FrequencyNode freqN = (next.count == newCount) ? next : new FrequencyNode(newCount, node.freq);
        node.remove();
        if (node.freq.isEmpty()) {
            node.freq.remove();
        }
        node.freq = freqN;
        node.append();
    }

    /**
     * Adds the entry, creating an initial frequency list of 1 if necessary, and evicts if needed.
     */
    private void onMiss(long key) {
        var next = requireNonNull(freq0.next);
        FrequencyNode freq1 = (next.count == 1) ? next : new FrequencyNode(1, freq0);
        var node = new Node(freq1, key);
        policyStats.recordMiss();
        data.put(key, node);
        node.append();
        evict(node);
    }

    /**
     * Evicts while the map exceeds the maximum capacity.
     */
    private void evict(Node candidate) {
        if (data.size() > maximumSize) {
            Node victim = nextVictim(candidate);
            boolean admit = admitter.admit(candidate.key, victim.key);
            if (admit) {
                evictEntry(victim);
            } else {
                evictEntry(candidate);
            }
            policyStats.recordEviction();
        }
    }

    Node nextVictim(Node candidate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Removes the entry.
     */
    private void evictEntry(Node node) {
        data.remove(node.key);
        node.remove();
        if (node.freq.isEmpty()) {
            node.freq.remove();
        }
    }

    public enum EvictionPolicy {

        LFU, MFU;

        public String label() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A frequency count and associated chain of cache entries.
     */
    static final class FrequencyNode {

        final int count;

        final Node nextNode;

        @Nullable
        FrequencyNode prev;

        @Nullable
        FrequencyNode next;

        public FrequencyNode() {
            nextNode = new Node(this);
            this.prev = this;
            this.next = this;
            this.count = 0;
        }

        public FrequencyNode(int count, FrequencyNode prev) {
            requireNonNull(prev.next);
            nextNode = new Node(this);
            this.prev = prev;
            this.next = prev.next;
            prev.next = this;
            next.prev = this;
            this.count = count;
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     * A cache entry on the frequency node's chain.
     */
    static final class Node {

        final long key;

        FrequencyNode freq;

        @Nullable
        Node prev;

        @Nullable
        Node next;

        public Node(FrequencyNode freq) {
            this.key = Long.MIN_VALUE;
            this.freq = freq;
            this.prev = this;
            this.next = this;
        }

        public Node(FrequencyNode freq, long key) {
            this.next = null;
            this.prev = null;
            this.freq = freq;
            this.key = key;
        }

        public void append() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
