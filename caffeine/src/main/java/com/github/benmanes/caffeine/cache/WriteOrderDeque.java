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

import java.util.Deque;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.WriteOrderDeque.WriteOrder;

/**
 * A linked deque implementation used to represent a write-order queue.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <E> the type of elements held in this collection
 */
final class WriteOrderDeque<E extends WriteOrder<E>> extends AbstractLinkedDeque<E> {

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    // A fast-path containment check
    boolean contains(WriteOrder<?> e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    // A fast-path removal
    public boolean remove(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E getPrevious(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void setPrevious(E e, @Nullable E prev) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public E getNext(E e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void setNext(E e, @Nullable E next) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * An element that is linked on the {@link Deque}.
     */
    interface WriteOrder<T extends WriteOrder<T>> {

        /**
         * Retrieves the previous element or {@code null} if either the element is unlinked or the
         * first element on the deque.
         */
        @Nullable
        T getPreviousInWriteOrder();

        /**
         * Sets the previous element or {@code null} if there is no link.
         */
        void setPreviousInWriteOrder(@Nullable T prev);

        /**
         * Retrieves the next element or {@code null} if either the element is unlinked or the last
         * element on the deque.
         */
        @Nullable
        T getNextInWriteOrder();

        /**
         * Sets the next element or {@code null} if there is no link.
         */
        void setNextInWriteOrder(@Nullable T next);
    }
}
