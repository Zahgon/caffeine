/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;
import com.google.errorprone.annotations.Var;

/**
 * This class provides a skeletal implementation of the {@link LinkedDeque} interface to minimize
 * the effort required to implement this interface.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <E> the type of elements held in this collection
 */
abstract class AbstractLinkedDeque<E> extends AbstractCollection<E> implements LinkedDeque<E> {

    // This class provides a doubly-linked list that is optimized for the virtual machine. The first
    // and last elements are manipulated instead of a slightly more convenient sentinel element to
    // avoid the insertion of null checks with NullPointerException throws in the byte code. The links
    // to a removed element are cleared to help a generational garbage collector if the discarded
    // elements inhabit more than one generation.
    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null)
     */
    @Nullable
    E first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null)
     */
    @Nullable
    E last;

    /**
     * The number of times this deque has been <i>structurally modified</i>. Structural modifications
     * are those that change the size of the deque, or otherwise perturb it in such a fashion that
     * iterations in progress may yield incorrect results.
     */
    int modCount;

    void linkFirst(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void linkLast(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    E unlinkFirst() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    E unlinkLast() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void unlink(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void checkNotEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public abstract boolean contains(Object o);

    @Override
    public boolean isFirst(@Nullable E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isLast(@Nullable E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void moveToFront(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void moveToBack(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E peek() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E peekFirst() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E peekLast() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E getFirst() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E getLast() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E poll() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E pollFirst() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E pollLast() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public abstract boolean remove(Object o);

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public PeekingIterator<E> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public PeekingIterator<E> descendingIterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    abstract class AbstractLinkedIterator implements PeekingIterator<E> {

        @Nullable
        E previous;

        @Nullable
        E cursor;

        int expectedModCount;

        /**
         * Creates an iterator that can traverse the deque.
         *
         * @param start the initial element to begin traversal from
         */
        AbstractLinkedIterator(@Nullable E start) {
            expectedModCount = modCount;
            cursor = start;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        @Nullable
        public E peek() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public E next() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Retrieves the next element to traverse to or {@code null} if there are no more elements.
         */
        @Nullable
        abstract E computeNext();

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void remove() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        void checkForConcurrentModification() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
