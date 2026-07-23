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
package com.github.benmanes.caffeine.jcache;

import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;

/**
 * A value with an expiration timestamp.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class Expirable<V> {

    private final V value;

    private volatile long expireTimeMillis;

    public Expirable(V value, long expireTimeMillis) {
        this.value = requireNonNull(value);
        this.expireTimeMillis = expireTimeMillis;
    }

    public V get() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long getExpireTimeMillis() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void setExpireTimeMillis(long expireTimeMillis) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean hasExpired(long currentTimeMillis) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isEternal() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
