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

import org.jspecify.annotations.NullMarked;

/**
 * A time source that returns a time value representing the number of nanoseconds elapsed since some
 * fixed but arbitrary point in time.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@FunctionalInterface
public interface Ticker {

    /**
     * Returns the number of nanoseconds elapsed since this ticker's fixed point of reference.
     *
     * @return the number of nanoseconds elapsed since this ticker's fixed point of reference
     */
    long read();

    static Ticker systemTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Ticker disabledTicker() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

enum SystemTicker implements Ticker {

    INSTANCE;

    @Override
    public long read() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

enum DisabledTicker implements Ticker {

    INSTANCE;

    @Override
    public long read() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
