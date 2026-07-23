/*
 * Copyright 2015 Gilga Einziger. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.admission.tinycache;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;

/**
 * An implementation of TinySet's indexing method. A method to index a succinct hash table that is
 * only 2 bits from theoretical lower bound. This is only the indexing technique, and it helps
 * calculate offsets in array using two indexes. chainIndex - (set bit for non-empty chain/unset for
 * empty) isLastIndex (set bit for last in chain/empty bit for not last in chain). Both indexes are
 * assumed to be 64 bits, (longs) for efficiency and simplicity. The technique update the indexes
 * upon addition/removal.
 * <p>
 * Paper link:
 * http://www.cs.technion.ac.il/users/wwwb/cgi-bin/tr-get.cgi/2015/CS/CS-2015-03.pdf
 * Presentation:
 * http://www.cs.technion.ac.il/~gilga/UCLA_and_TCL.pptx
 *
 * @author gilga1983@gmail.com (Gil Einziger)
 */
@SuppressWarnings("JavadocLinkAsPlainText")
final class TinySetIndexing {

    // for performance - for functions that need to know both the start and the end of the chain.
    private int chainStart;

    private int chainEnd;

    public int getChainStart(HashedItem fpaux, long[] chainIndex, long[] isLastIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int rank(long index, int bitNum) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @CanIgnoreReturnValue
    public int getChain(HashedItem fpaux, long[] chainIndex, long[] isLastIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public int getChainAtOffset(HashedItem fpaux, long[] chainIndex, long[] isLastIndex, int offset) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean chainExist(long chainIndex, int chainId) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int addItem(HashedItem fpaux, long[] chainIndex, long[] lastIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("UnnecessaryParentheses")
    private static long extendZero(long isLastIndex, int offset) {
        long constantPartMask = (1L << offset) - 1;
        return (isLastIndex & constantPartMask) | ((isLastIndex << 1L) & (~(constantPartMask)) & (~(1L << offset)));
    }

    @SuppressWarnings("UnnecessaryParentheses")
    private static long shrinkOffset(long isLastIndex, int offset) {
        long conMask = ((1L << offset) - 1);
        return (isLastIndex & conMask) | (((~conMask) & isLastIndex) >>> 1);
    }

    public void removeItem(HashedItem fpaux, long[] chainIndex, long[] isLastIndex) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getChainStart() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void setChainStart(int chainStart) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getChainEnd() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void setChainEnd(int chainEnd) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
