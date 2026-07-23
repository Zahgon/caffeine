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
package com.github.benmanes.caffeine.jcache.expiry;

import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.util.Objects;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import org.jspecify.annotations.Nullable;

/**
 * A customized expiration policy.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class JCacheExpiryPolicy implements ExpiryPolicy, Serializable {

    private static final long serialVersionUID = 1L;

    private final Duration creation;

    @Nullable
    private final Duration update;

    @Nullable
    private final Duration access;

    public JCacheExpiryPolicy(Duration creation, @Nullable Duration update, @Nullable Duration access) {
        this.creation = requireNonNull(creation);
        this.update = update;
        this.access = access;
    }

    @Override
    public Duration getExpiryForCreation() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Duration getExpiryForUpdate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Duration getExpiryForAccess() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
