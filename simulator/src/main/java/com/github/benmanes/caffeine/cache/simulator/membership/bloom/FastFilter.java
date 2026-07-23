/*
 * Copyright 2019 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.membership.bloom;

import static com.google.common.base.Preconditions.checkState;
import org.fastfilter.Filter;
import org.fastfilter.FilterType;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.membership.Membership;
import com.google.common.base.CaseFormat;
import com.typesafe.config.Config;

/**
 * An adapter to <a href="https://github.com/FastFilter/fastfilter_java">FastFilter</a>
 * implementations.
 *
 * @author ben@withvector.com (Ben Manes)
 */
public final class FastFilter implements Membership {

    private final FilterType filterType;

    private final int bitsPerKey;

    private final long[] keys;

    private Filter filter;

    public FastFilter(Config config) {
        var settings = new FastFilterSettings(config);
        keys = new long[(int) settings.membership().expectedInsertions()];
        filterType = settings.filterType();
        bitsPerKey = settings.bitsPerKey();
        reset();
    }

    @Override
    public boolean mightContain(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean put(long e) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private void reset() {
        filter = filterType.construct(keys, bitsPerKey);
        checkState(filter.supportsAdd(), "Filter must support additions");
    }

    static final class FastFilterSettings extends BasicSettings {

        public FastFilterSettings(Config config) {
            super(config);
        }

        public FilterType filterType() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public int bitsPerKey() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
