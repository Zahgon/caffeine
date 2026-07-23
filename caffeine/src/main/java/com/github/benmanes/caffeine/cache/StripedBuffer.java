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
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package com.github.benmanes.caffeine.cache;

import static com.github.benmanes.caffeine.cache.Caffeine.ceilingPowerOfTwo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * A base class providing the mechanics for supporting dynamic striping of bounded buffers. This
 * implementation is an adaption of the numeric 64-bit <i>java.util.concurrent.atomic.Striped64</i>
 * class, which is used by atomic counters. The approach was modified to lazily grow an array of
 * buffers in order to minimize memory usage for caches that are not heavily contended on.
 *
 * @author dl@cs.oswego.edu (Doug Lea)
 * @author ben.manes@gmail.com (Ben Manes)
 */
abstract class StripedBuffer<E> implements Buffer<E> {

    /*
   * This class maintains a lazily-initialized table of atomically updated buffers. The table size
   * is a power of two. Indexing uses masked per-thread hash codes. Nearly all declarations in this
   * class are package-private, accessed directly by subclasses.
   *
   * Table entries are of class Buffer and should be padded to reduce cache contention. Padding is
   * overkill for most atomics because they are usually irregularly scattered in memory and thus
   * don't interfere much with each other. But atomic objects residing in arrays will tend to be
   * placed adjacent to each other, and so will most often share cache lines (with a huge negative
   * performance impact) without this precaution.
   *
   * In part because Buffers are relatively large, we avoid creating them until they are needed.
   * When there is no contention, all updates are made to a single buffer. Upon contention (a failed
   * CAS inserting into the buffer), the table is expanded to size 2. The table size is doubled upon
   * further contention until reaching the nearest power of two greater than or equal to the number
   * of CPUS. Table slots remain empty (null) until they are needed.
   *
   * A single spinlock ("tableBusy") is used for initializing and resizing the table, as well as
   * populating slots with new Buffers. There is no need for a blocking lock; when the lock is not
   * available, threads try other slots. During these retries, there is increased contention and
   * reduced locality, which is still better than alternatives.
   *
   * Contention and/or table collisions are indicated by failed CASes when performing an update
   * operation. Upon a collision, if the table size is less than the capacity, it is doubled in size
   * unless some other thread holds the lock. If a hashed slot is empty, and lock is available, a
   * new Buffer is created. Otherwise, if the slot exists, a CAS is tried. The thread id serves as
   * the base for per-thread hash codes. Retries proceed by "incremental hashing", using the top
   * half of the seed to increment the bottom half which is used as a probe to try to find a free
   * slot.
   *
   * The table size is capped because, when there are more threads than CPUs, supposing that each
   * thread were bound to a CPU, there would exist a perfect hash function mapping threads to slots
   * that eliminates collisions. When we reach capacity, we search for this mapping by varying the
   * hash codes of colliding threads. Because search is random, and collisions only become known via
   * CAS failures, convergence can be slow, and because threads are typically not bound to CPUs
   * forever, may not occur at all. However, despite these limitations, observed contention rates
   * are typically low in these cases.
   *
   * It is possible for a Buffer to become unused when threads that once hashed to it terminate, as
   * well as in the case where doubling the table causes no thread to hash to it under expanded
   * mask. We do not try to detect or remove buffers, under the assumption that for long-running
   * instances, observed contention levels will recur, so the buffers will eventually be needed
   * again; and for short-lived ones, it does not matter.
   */
    static final VarHandle TABLE_BUSY = findVarHandle(StripedBuffer.class, "tableBusy", int.class);

    /**
     * Number of CPUS.
     */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The bound on the table size.
     */
    static final int MAXIMUM_TABLE_SIZE = 4 * ceilingPowerOfTwo(NCPU);

    /**
     * The maximum number of attempts when trying to expand the table.
     */
    static final int ATTEMPTS = 3;

    /**
     * Table of buffers. When non-null, size is a power of 2.
     */
    volatile Buffer<E> @Nullable [] table;

    /**
     * Spinlock (locked via CAS) used when resizing and/or creating Buffers.
     */
    volatile int tableBusy;

    final boolean casTableBusy() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Creates a new buffer instance after resizing to accommodate a producer.
     *
     * @param e the producer's element
     * @return a newly created buffer populated with a single element
     */
    protected abstract Buffer<E> create(E e);

    @Override
    @SuppressWarnings("Varifier")
    public int offer(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    final int expandOrRetry(E e, @Var int h, int increment, @Var boolean wasUncontended) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void drainTo(Consumer<E> consumer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long reads() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public long writes() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long mix64(@Var long z) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
