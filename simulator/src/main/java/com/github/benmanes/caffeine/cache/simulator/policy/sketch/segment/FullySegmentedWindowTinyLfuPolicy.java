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
import com.github.benmanes.caffeine.cache.simulator.policy.linked.SegmentedLruPolicy;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * The Window TinyLfu algorithm where the window and main spaces implement
 * {@link SegmentedLruPolicy}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(name = "sketch.FullySegmentedWindowTinyLfu")
public final class FullySegmentedWindowTinyLfuPolicy implements KeyOnlyPolicy {

    private final Long2ObjectMap<Node> data;

    private final PolicyStats policyStats;

    private final Admitter admitter;

    private final int maximumSize;

    private final Node headWindowProbation;

    private final Node headWindowProtected;

    private final Node headMainProbation;

    private final Node headMainProtected;

    private final int maxWindow;

    private final int maxWindowProtected;

    private final int maxMainProtected;

    private int sizeWindow;

    private int sizeWindowProtected;

    private int sizeMainProtected;

    @SuppressWarnings("Varifier")
    public FullySegmentedWindowTinyLfuPolicy(double percentMain, FullySegmentedWindowTinyLfuSettings settings) {
        this.policyStats = new PolicyStats(name() + " (%.0f%%)", 100 * (1.0d - percentMain));
        this.maximumSize = Math.toIntExact(settings.maximumSize());
        int maxMain = (int) (maximumSize * percentMain);
        this.maxWindow = maximumSize - maxMain;
        this.maxMainProtected = (int) (maxMain * settings.percentMainProtected());
        this.maxWindowProtected = (int) (maxWindow * settings.percentWindowProtected());
        this.admitter = Admission.TINYLFU.from(settings.config(), policyStats);
        this.data = new Long2ObjectOpenHashMap<>();
        this.headWindowProbation = new Node();
        this.headWindowProtected = new Node();
        this.headMainProbation = new Node();
        this.headMainProtected = new Node();
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
        var node = new Node(key, Status.WINDOW_PROBATION);
        node.appendToTail(headWindowProbation);
        data.put(key, node);
        sizeWindow++;
        evict();
    }

    /**
     * Promotes the entry to the protected region's MRU position, demoting an entry if necessary.
     */
    private void onWindowProbationHit(Node node) {
        node.remove();
        node.status = Status.WINDOW_PROTECTED;
        node.appendToTail(headWindowProtected);
        sizeWindowProtected++;
        if (sizeWindowProtected > maxWindowProtected) {
            Node demote = requireNonNull(headWindowProtected.next);
            demote.remove();
            demote.status = Status.WINDOW_PROBATION;
            demote.appendToTail(headWindowProbation);
            sizeWindowProtected--;
        }
    }

    /**
     * Moves the entry to the MRU position in the admission window.
     */
    private void onWindowProtectedHit(Node node) {
        node.moveToTail(headWindowProtected);
    }

    /**
     * Promotes the entry to the protected region's MRU position, demoting an entry if necessary.
     */
    private void onMainProbationHit(Node node) {
        node.remove();
        node.status = Status.MAIN_PROTECTED;
        node.appendToTail(headMainProtected);
        sizeMainProtected++;
        if (sizeMainProtected > maxMainProtected) {
            Node demote = requireNonNull(headMainProtected.next);
            demote.remove();
            demote.status = Status.MAIN_PROBATION;
            demote.appendToTail(headMainProbation);
            sizeMainProtected--;
        }
    }

    /**
     * Moves the entry to the MRU position if it falls outside of the fast-path threshold.
     */
    private void onMainProtectedHit(Node node) {
        node.moveToTail(headMainProtected);
    }

    /**
     * Evicts from the admission window into the probation space. If the size exceeds the maximum,
     * then the admission candidate and probation's victim are evaluated and one is evicted.
     */
    private void evict() {
        if (sizeWindow <= maxWindow) {
            return;
        }
        Node candidate = requireNonNull(headWindowProbation.next);
        sizeWindow--;
        candidate.remove();
        candidate.status = Status.MAIN_PROBATION;
        candidate.appendToTail(headMainProbation);
        if (data.size() > maximumSize) {
            Node victim = requireNonNull(headMainProbation.next);
            Node evict = admitter.admit(candidate.key, victim.key) ? victim : candidate;
            data.remove(evict.key);
            evict.remove();
            policyStats.recordEviction();
        }
    }

    @Override
    public void finished() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    enum Status {

        WINDOW_PROBATION, WINDOW_PROTECTED, MAIN_PROBATION, MAIN_PROTECTED
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

    public static final class FullySegmentedWindowTinyLfuSettings extends BasicSettings {

        public FullySegmentedWindowTinyLfuSettings(Config config) {
            super(config);
        }

        public List<Double> percentMain() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentMainProtected() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentWindowProtected() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
