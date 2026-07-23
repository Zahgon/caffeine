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
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * The Window TinyLfu algorithm where the window and main spaces implement LRU. This simpler version
 * comes at the cost of not capturing recency as effectively as Segmented LRU does.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(name = "sketch.LruWindowTinyLfu")
public final class LruWindowTinyLfuPolicy implements KeyOnlyPolicy {

    private final Long2ObjectMap<Node> data;

    private final PolicyStats policyStats;

    private final Admitter admitter;

    private final Node headWindow;

    private final Node headMain;

    private final int maxWindow;

    private final int maxMain;

    private int sizeWindow;

    private int sizeMain;

    public LruWindowTinyLfuPolicy(double percentMain, LruWindowTinyLfuSettings settings) {
        this.policyStats = new PolicyStats(name() + " (%.0f%%)", 100 * (1.0d - percentMain));
        int maximumSize = Math.toIntExact(settings.maximumSize());
        this.admitter = Admission.TINYLFU.from(settings.config(), policyStats);
        this.maxMain = (int) (maximumSize * percentMain);
        this.data = new Long2ObjectOpenHashMap<>();
        this.maxWindow = maximumSize - maxMain;
        this.headWindow = new Node();
        this.headMain = new Node();
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
     * Evicts if the map exceeds the maximum capacity.
     */
    private void evict() {
        if (sizeWindow <= maxWindow) {
            return;
        }
        Node candidate = requireNonNull(headWindow.next);
        candidate.remove();
        sizeWindow--;
        candidate.appendToTail(headMain);
        candidate.status = Status.MAIN;
        sizeMain++;
        if (sizeMain > maxMain) {
            Node victim = requireNonNull(headMain.next);
            Node evict = admitter.admit(candidate.key, victim.key) ? victim : candidate;
            data.remove(evict.key);
            evict.remove();
            sizeMain--;
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

        /**
         * Creates a new sentinel node.
         */
        public Node() {
            this.key = Integer.MIN_VALUE;
            this.prev = this;
            this.next = this;
        }

        /**
         * Creates a new, unlinked node.
         */
        public Node(long key, Status status) {
            this.status = status;
            this.key = key;
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

    public static final class LruWindowTinyLfuSettings extends BasicSettings {

        public LruWindowTinyLfuSettings(Config config) {
            super(config);
        }

        public List<Double> percentMain() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
