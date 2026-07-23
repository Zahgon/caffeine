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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch.segment;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.admission.Admitter;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.KeyOnlyPolicy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.PolicySpec;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * The Window TinyLfu algorithm where the window space implements LRU and the main space implements
 * S4LRU.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(name = "sketch.S4WindowTinyLfu")
public final class S4WindowTinyLfuPolicy implements KeyOnlyPolicy {

    private final Long2ObjectMap<Node> data;

    private final PolicyStats policyStats;

    private final Admitter admitter;

    private final Node[] headMainQ;

    private final int maximumSize;

    private final int[] sizeMainQ;

    private final Node headWindow;

    private final int maxWindow;

    private final int maxMain;

    private final int levels;

    private int sizeWindow;

    @SuppressWarnings("Varifier")
    public S4WindowTinyLfuPolicy(double percentMain, S4WindowTinyLfuSettings settings) {
        this.policyStats = new PolicyStats(name() + " (%.0f%%)", 100 * (1.0d - percentMain));
        this.admitter = Admission.TINYLFU.from(settings.config(), policyStats);
        this.maximumSize = Math.toIntExact(settings.maximumSize());
        this.maxMain = (int) (maximumSize * percentMain);
        this.maxWindow = maximumSize - maxMain;
        this.data = new Long2ObjectOpenHashMap<>();
        this.headWindow = Node.sentinel(-1);
        this.levels = settings.levels();
        this.sizeMainQ = new int[levels];
        this.headMainQ = new Node[levels];
        Arrays.setAll(headMainQ, Node::sentinel);
    }

    public static Set<Policy> policies(Config config) {
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
     * Adds the entry to the admission window, evicting if necessary.
     */
    private void onMiss(long key) {
        var node = new Node(key, Status.WINDOW);
        node.appendToTail(headWindow);
        data.put(key, node);
        sizeWindow++;
        evict();
    }

    /**
     * Moves the entry to the MRU position in the admission window.
     */
    private void onWindowHit(Node node) {
        node.moveToTail(headWindow);
    }

    /**
     * Promotes the entry to the protected region's MRU position, demoting an entry if necessary.
     */
    private void onMainHit(Node node) {
        node.remove();
        sizeMainQ[node.level]--;
        if (node.level < (levels - 1)) {
            node.level++;
        }
        Node head = headMainQ[node.level];
        node.appendToTail(head);
        sizeMainQ[node.level]++;
        adjust();
    }

    private void adjust() {
        int maxPerLevel = maxMain / levels;
        for (int i = levels - 1; i > 0; i--) {
            if (sizeMainQ[i] > maxPerLevel) {
                Node demote = requireNonNull(headMainQ[i].next);
                demote.remove();
                sizeMainQ[i]--;
                demote.level = i - 1;
                sizeMainQ[demote.level]++;
                demote.appendToTail(headMainQ[demote.level]);
            }
        }
    }

    /**
     * Evicts if the map exceeds the maximum capacity.
     */
    private void evict() {
        if (sizeWindow <= maxWindow) {
            return;
        }
        Node candidate = requireNonNull(headWindow.next);
        candidate.remove();
        sizeWindow--;
        candidate.appendToTail(headMainQ[0]);
        candidate.status = Status.MAIN;
        sizeMainQ[0]++;
        if (data.size() > maximumSize) {
            Node victim = requireNonNull(headMainQ[0].next);
            Node evict = admitter.admit(candidate.key, victim.key) ? victim : candidate;
            data.remove(evict.key);
            evict.remove();
            sizeMainQ[0]--;
            policyStats.recordEviction();
        }
    }

    @Override
    public void finished() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    enum Status {

        WINDOW, MAIN
    }

    /**
     * A node on the double-linked list.
     */
    static final class Node {

        final long key;

        @Nullable
        Node prev;

        @Nullable
        Node next;

        @Nullable
        Status status;

        int level;

        /**
         * Creates a new, unlinked node.
         */
        public Node(long key, @Nullable Status status) {
            this.status = status;
            this.key = key;
        }

        static Node sentinel(int level) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void moveToTail(Node head) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void appendToTail(Node head) {
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

    public static final class S4WindowTinyLfuSettings extends BasicSettings {

        public S4WindowTinyLfuSettings(Config config) {
            super(config);
        }

        public int levels() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public List<Double> percentMain() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
