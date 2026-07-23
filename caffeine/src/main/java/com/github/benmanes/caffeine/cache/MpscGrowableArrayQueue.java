/*
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
import static com.github.benmanes.caffeine.cache.Caffeine.requireArgument;
import static com.github.benmanes.caffeine.cache.Caffeine.requireState;
import static java.util.Objects.requireNonNull;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.AbstractQueue;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * An MPSC array queue which starts at <i>initialCapacity</i> and grows to <i>maxCapacity</i> in
 * linked chunks of the initial size. The queue grows only when the current buffer is full and
 * elements are not copied on resize, instead a link to the new buffer is stored in the old buffer
 * for the consumer to follow.<br>
 * <p>
 * This is a shaded copy of <code>MpscGrowableArrayQueue</code> provided by
 * <a href="https://github.com/JCTools/JCTools">JCTools</a> from version 2.0.
 *
 * @author nitsanw@yahoo.com (Nitsan Wakart)
 */
class MpscGrowableArrayQueue<E> extends MpscChunkedArrayQueue<E> {

    /**
     * @param initialCapacity the queue initial capacity. If chunk size is fixed this will be the
     *        chunk size. Must be 2 or more.
     * @param maxCapacity the maximum capacity will be rounded up to the closest power of 2 and will
     *        be the upper limit of number of elements in this queue. Must be 4 or more and round up
     *        to a larger power of 2 than initialCapacity.
     */
    MpscGrowableArrayQueue(int initialCapacity, int maxCapacity) {
        super(initialCapacity, maxCapacity);
    }

    @Override
    protected int getNextBufferSize(@Nullable E[] buffer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    protected long getCurrentBufferCapacity(long mask) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

@SuppressWarnings({ "MultiVariableDeclaration", "OvershadowingSubclassFields", "PMD.OneDeclarationPerLine", "unused" })
abstract class MpscChunkedArrayQueue<E> extends MpscChunkedArrayQueueColdProducerFields<E> {

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

    MpscChunkedArrayQueue(int initialCapacity, int maxCapacity) {
        super(initialCapacity, maxCapacity);
    }

    @Override
    protected long availableInQueue(long pIndex, long cIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int capacity() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

abstract class MpscChunkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueue<E> {

    protected final long maxQueueCapacity;

    MpscChunkedArrayQueueColdProducerFields(int initialCapacity, int maxCapacity) {
        super(initialCapacity);
        requireArgument(maxCapacity >= 4, "Max capacity must be 4 or more");
        requireArgument(ceilingPowerOfTwo(maxCapacity) >= ceilingPowerOfTwo(initialCapacity), "Initial capacity cannot exceed maximum capacity(both rounded up to a power of 2)");
        maxQueueCapacity = ((long) ceilingPowerOfTwo(maxCapacity)) << 1;
    }
}

@SuppressWarnings({ "MultiVariableDeclaration", "PMD.OneDeclarationPerLine", "unused" })
abstract class BaseMpscLinkedArrayQueuePad1<E> extends AbstractQueue<E> {

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

abstract class BaseMpscLinkedArrayQueueProducerFields<E> extends BaseMpscLinkedArrayQueuePad1<E> {

    protected long producerIndex;
}

@SuppressWarnings({ "MultiVariableDeclaration", "OvershadowingSubclassFields", "PMD.OneDeclarationPerLine", "unused" })
abstract class BaseMpscLinkedArrayQueuePad2<E> extends BaseMpscLinkedArrayQueueProducerFields<E> {

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

abstract class BaseMpscLinkedArrayQueueConsumerFields<E> extends BaseMpscLinkedArrayQueuePad2<E> {

    @Nullable
    protected E[] consumerBuffer;

    protected long consumerIndex;

    protected long consumerMask;

    BaseMpscLinkedArrayQueueConsumerFields(int initialCapacity) {
        requireArgument(initialCapacity >= 2, "Initial capacity must be 2 or more");
        int p2capacity = ceilingPowerOfTwo(initialCapacity);
        // leave lower bit of mask clear
        long mask = (p2capacity - 1L) << 1;
        // need extra element to point at next array
        @Nullable
        E[] buffer = allocate(p2capacity + 1);
        consumerBuffer = buffer;
        consumerMask = mask;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    E[] allocate(int capacity) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

@SuppressWarnings({ "MultiVariableDeclaration", "OvershadowingSubclassFields", "PMD.OneDeclarationPerLine", "unused" })
abstract class BaseMpscLinkedArrayQueuePad3<E> extends BaseMpscLinkedArrayQueueConsumerFields<E> {

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

    BaseMpscLinkedArrayQueuePad3(int initialCapacity) {
        super(initialCapacity);
    }
}

abstract class BaseMpscLinkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueuePad3<E> {

    @Nullable
    protected E[] producerBuffer;

    protected volatile long producerLimit;

    protected long producerMask;

    BaseMpscLinkedArrayQueueColdProducerFields(int initialCapacity) {
        super(initialCapacity);
        producerMask = consumerMask;
        producerBuffer = consumerBuffer;
    }
}

abstract class BaseMpscLinkedArrayQueue<E> extends BaseMpscLinkedArrayQueueColdProducerFields<E> {

    static final VarHandle P_INDEX = findVarHandle(BaseMpscLinkedArrayQueueProducerFields.class, "producerIndex", long.class);

    static final VarHandle C_INDEX = findVarHandle(BaseMpscLinkedArrayQueueConsumerFields.class, "consumerIndex", long.class);

    static final VarHandle P_LIMIT = findVarHandle(BaseMpscLinkedArrayQueueColdProducerFields.class, "producerLimit", long.class);

    static final VarHandle REF_ARRAY = MethodHandles.arrayElementVarHandle(Object[].class);

    // No post padding here, subclasses must add
    private static final Object JUMP = new Object();

    /**
     * @param initialCapacity the queue initial capacity. If chunk size is fixed this will be the
     *        chunk size. Must be 2 or more.
     */
    BaseMpscLinkedArrayQueue(int initialCapacity) {
        super(initialCapacity);
        // we know it's all empty to start with
        soProducerLimit(this, producerMask);
    }

    @Override
    public final Iterator<E> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "MissingDefault", "PMD.NonExhaustiveSwitch" })
    public boolean offer(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    int offerSlowPath(long mask, long pIndex, long producerLimit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * @return available elements in queue * 2
     */
    protected abstract long availableInQueue(long pIndex, long cIndex);

    @Override
    @SuppressWarnings({ "CastCanBeRemovedNarrowingVariableType", "PMD.ConfusingTernary", "unchecked" })
    @Nullable
    public E poll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings({ "CastCanBeRemovedNarrowingVariableType", "PMD.EmptyControlStatement", "StatementWithEmptyBody", "unchecked" })
    @Nullable
    public E peek() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private E[] getNextBuffer(@Nullable E[] buffer, long mask) {
        long nextArrayOffset = nextArrayOffset(mask);
        @SuppressWarnings("Varifier")
        @Nullable
        E[] nextBuffer = (@Nullable E[]) lvElement(buffer, nextArrayOffset);
        soElement(buffer, nextArrayOffset, null);
        return requireNonNull(nextBuffer);
    }

    private static long nextArrayOffset(long mask) {
        return modifiedCalcElementOffset(mask + 2, Long.MAX_VALUE);
    }

    private E newBufferPoll(@Nullable E[] nextBuffer, long index) {
        long offsetInNew = newBufferAndOffset(nextBuffer, index);
        // LoadLoad
        @Nullable
        E n = lvElement(nextBuffer, offsetInNew);
        requireNonNull(n, "new buffer must have at least one element");
        // StoreStore
        soElement(nextBuffer, offsetInNew, null);
        soConsumerIndex(this, index + 2);
        return n;
    }

    private E newBufferPeek(@Nullable E[] nextBuffer, long index) {
        long offsetInNew = newBufferAndOffset(nextBuffer, index);
        // LoadLoad
        @Nullable
        E n = lvElement(nextBuffer, offsetInNew);
        requireNonNull(n, "new buffer must have at least one element");
        return n;
    }

    private long newBufferAndOffset(@Nullable E[] nextBuffer, long index) {
        consumerBuffer = nextBuffer;
        consumerMask = (nextBuffer.length - 2L) << 1;
        return modifiedCalcElementOffset(index, consumerMask);
    }

    @Override
    public final int size() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public final boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private long lvProducerLimit() {
        return producerLimit;
    }

    public long currentProducerIndex() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long currentConsumerIndex() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public abstract int capacity();

    public boolean relaxedOffer(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "CastCanBeRemovedNarrowingVariableType", "unchecked" })
    @Nullable
    public E relaxedPoll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public E relaxedPeek() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private void resize(long oldMask, @Nullable E[] oldBuffer, long pIndex, E e) {
        int newBufferLength = getNextBufferSize(oldBuffer);
        @Nullable
        E[] newBuffer;
        try {
            newBuffer = allocate(newBufferLength);
        } catch (OutOfMemoryError error) {
            soProducerIndex(this, pIndex);
            throw error;
        }
        producerBuffer = newBuffer;
        int newMask = (newBufferLength - 2) << 1;
        producerMask = newMask;
        long offsetInOld = modifiedCalcElementOffset(pIndex, oldMask);
        long offsetInNew = modifiedCalcElementOffset(pIndex, newMask);
        // element in new array
        soElement(newBuffer, offsetInNew, e);
        // buffer linked
        soElement(oldBuffer, nextArrayOffset(oldMask), newBuffer);
        // ASSERT code
        long cIndex = lvConsumerIndex(this);
        long availableInQueue = availableInQueue(pIndex, cIndex);
        requireState(availableInQueue > 0);
        // Invalidate racing CASs
        // We never set the limit beyond the bounds of a buffer
        soProducerLimit(this, pIndex + Math.min(newMask, availableInQueue));
        // make resize visible to the other producers
        soProducerIndex(this, pIndex + 2);
        // INDEX visible before ELEMENT, consistent with consumer expectation
        // make resize visible to consumer
        soElement(oldBuffer, offsetInOld, JUMP);
    }

    /**
     * @return next buffer size(inclusive of next array pointer)
     */
    protected abstract int getNextBufferSize(@Nullable E[] buffer);

    /**
     * @return current buffer capacity for elements (excluding next pointer and jump entry) * 2
     */
    protected abstract long getCurrentBufferCapacity(long mask);

    @SuppressWarnings("PMD.LooseCoupling")
    static long lvProducerIndex(BaseMpscLinkedArrayQueue<?> self) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static long lvConsumerIndex(BaseMpscLinkedArrayQueue<?> self) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static void soProducerIndex(BaseMpscLinkedArrayQueue<?> self, long v) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static boolean casProducerIndex(BaseMpscLinkedArrayQueue<?> self, long expect, long newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static void soConsumerIndex(BaseMpscLinkedArrayQueue<?> self, long v) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static boolean casProducerLimit(BaseMpscLinkedArrayQueue<?> self, long expect, long newValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.LooseCoupling")
    static void soProducerLimit(BaseMpscLinkedArrayQueue<?> self, long v) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /*
   * A concurrent access enabling class used by circular array based queues this class exposes an
   * offset computation method along with differently memory fenced load/store methods into the
   * underlying array. The class is pre-padded and the array is padded on either side to help with
   * False sharing prevention. It is expected that subclasses handle post padding.
   * <p>
   * Offset calculation is separate from access to enable the reuse of a give compute offset.
   * <p>
   * Load/Store methods using a <i>buffer</i> parameter are provided to allow the prevention of
   * final field reload after a LoadLoad barrier.
   * <p>
   */
    static <E> void soElement(@Nullable E[] buffer, long offset, @Nullable E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    static <E> E lvElement(@Nullable E @Nullable [] buffer, long offset) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long modifiedCalcElementOffset(long index, long mask) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
