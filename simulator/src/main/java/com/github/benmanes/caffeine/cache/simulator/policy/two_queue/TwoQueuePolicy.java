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
package com.github.benmanes.caffeine.cache.simulator.policy.two_queue;

import static java.util.Objects.requireNonNull;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.KeyOnlyPolicy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.PolicySpec;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * The 2Q algorithm. This algorithm uses a queue for items that are seen once (IN), a queue for
 * items seen multiple times (MAIN), and a non-resident queue for evicted items that are being
 * monitored (OUT). The maximum size of the IN and OUT queues must be tuned with the authors
 * recommending 20% and 50% of the maximum size, respectively.
 * <p>
 * This implementation is based on the pseudocode provided by the authors in their paper
 * <a href="https://www.vldb.org/conf/1994/P439.PDF">2Q: A Low Overhead High Performance Buffer
 * Management Replacement Algorithm</a>. For consistency with other policies, this version places
 * the next item to be removed at the head and most recently added at the tail of the queue.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(name = "two-queue.TwoQueue")
public final class TwoQueuePolicy implements KeyOnlyPolicy {

    static final Node UNLINKED = new Node();

    final Long2ObjectMap<Node> data;

    final PolicyStats policyStats;

    final int maximumSize;

    int sizeIn;

    final int maxIn;

    final Node headIn;

    int sizeOut;

    final int maxOut;

    final Node headOut;

    int sizeMain;

    final Node headMain;

    public TwoQueuePolicy(Config config) {
        var settings = new TwoQueueSettings(config);
        this.headIn = new Node();
        this.headOut = new Node();
        this.headMain = new Node();
        this.data = new Long2ObjectOpenHashMap<>();
        this.policyStats = new PolicyStats(name());
        this.maximumSize = Math.toIntExact(settings.maximumSize());
        this.maxIn = (int) (maximumSize * settings.percentIn());
        this.maxOut = (int) (maximumSize * settings.percentOut());
    }

    @Override
    public void record(long key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private void reclaimFor(Node node) {
        // if there are free page slots then
        //   put X into a free page slot
        // else if (size(Alin) > Kin)
        //   page out the tail of Alin, call it Y
        //   add identifier of Y to the head of Alout
        //   if (size(Alout) > Kout)
        //     remove identifier of Z from the tail of Alout
        //   end if
        //   put X into the reclaimed page slot
        // else
        //   page out the tail of Am, call it Y
        //   // do not put it on Alout; it hasn’t been accessed for a while
        //   put X into the reclaimed page slot
        // end if
        if ((sizeMain + sizeIn) < maximumSize) {
            data.put(node.key, node);
        } else if (sizeIn > maxIn) {
            // IN is full, move to OUT
            Node n = headIn.next;
            n.remove();
            sizeIn--;
            n.appendToTail(headOut);
            n.type = QueueType.OUT;
            sizeOut++;
            if (sizeOut > maxOut) {
                // OUT is full, drop oldest
                policyStats.recordEviction();
                Node victim = headOut.next;
                data.remove(victim.key);
                victim.remove();
                sizeOut--;
            }
            data.put(node.key, node);
        } else {
            // OUT has room, evict from MAIN
            policyStats.recordEviction();
            Node victim = headMain.next;
            data.remove(victim.key);
            victim.remove();
            sizeMain--;
            data.put(node.key, node);
        }
    }

    @Override
    public PolicyStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    enum QueueType {

        MAIN, IN, OUT
    }

    static final class Node {

        final long key;

        Node prev;

        Node next;

        @Nullable
        QueueType type;

        Node() {
            this.key = Long.MIN_VALUE;
            this.prev = this;
            this.next = this;
        }

        Node(long key) {
            this.key = key;
            this.prev = UNLINKED;
            this.next = UNLINKED;
        }

        public void appendToTail(Node head) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void moveToTail(Node head) {
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

    static final class TwoQueueSettings extends BasicSettings {

        public TwoQueueSettings(Config config) {
            super(config);
        }

        public double percentIn() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentOut() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
