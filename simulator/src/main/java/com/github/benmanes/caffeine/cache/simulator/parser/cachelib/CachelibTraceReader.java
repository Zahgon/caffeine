/*
 * Copyright 2023 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.parser.cachelib;

import static com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic.WEIGHTED;
import java.util.Set;
import java.util.stream.Stream;
import com.github.benmanes.caffeine.cache.simulator.parser.TextTraceReader;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic;

/**
 * A reader for the trace files provided by the authors of cachelib. See
 * <a href="https://cachelib.org/docs/Cache_Library_User_Guides/Cachebench_FB_HW_eval">traces</a>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class CachelibTraceReader extends TextTraceReader {

    public CachelibTraceReader(String filePath) {
        super(filePath);
    }

    @Override
    public Set<Characteristic> characteristics() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public Stream<AccessEvent> events() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
