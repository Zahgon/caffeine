/*
 * Copyright 2022 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.policy.product;

import static com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic.WEIGHTED;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static java.util.Locale.US;
import static java.util.stream.Collectors.toUnmodifiableSet;
import java.util.Map;
import java.util.Set;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.PolicySpec;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.CaseFormat;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Var;
import com.tangosol.net.cache.CacheStatistics;
import com.tangosol.net.cache.ConfigurableCacheMap.UnitCalculator;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.typesafe.config.Config;

/**
 * Coherence cache implementation.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("deprecation")
@PolicySpec(name = "product.Coherence", characteristics = WEIGHTED)
public final class CoherencePolicy implements Policy {

    private final Map<Long, AccessEvent> map;

    private final PolicyStats policyStats;

    private final CacheStatistics stats;

    @SuppressWarnings("unchecked")
    public CoherencePolicy(CoherenceSettings settings, Eviction policy) {
        policyStats = new PolicyStats(name() + " (%s)", policy);
        // auto scale units to integer range (from LocalScheme)
        @Var
        int factor = 1;
        @Var
        long maximum = settings.maximumSize();
        while (maximum >= Integer.MAX_VALUE) {
            maximum /= 1024;
            factor *= 1024;
        }
        var cache = new LocalCache();
        cache.setUnitFactor(factor);
        cache.setHighUnits((int) maximum);
        cache.setEvictionType(policy.type);
        cache.addMapListener(new CoherenceListener());
        cache.setUnitCalculator(new AccessEventCalculator());
        stats = cache.getCacheStatistics();
        map = cache;
    }

    public static Set<Policy> policies(Config config) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void record(AccessEvent event) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public PolicyStats stats() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void finished() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private final class CoherenceListener implements MapListener<Object, Object> {

        @Override
        public void entryInserted(MapEvent<Object, Object> event) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void entryUpdated(MapEvent<Object, Object> event) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public void entryDeleted(MapEvent<Object, Object> event) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    private static final class AccessEventCalculator implements UnitCalculator {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int calculateUnits(Object key, Object value) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public static final class CoherenceSettings extends BasicSettings {

        public CoherenceSettings(Config config) {
            super(config);
        }

        public ImmutableSet<Eviction> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @SuppressWarnings("deprecation")
    public enum Eviction {

        HYBRID(LocalCache.EVICTION_POLICY_HYBRID), LRU(LocalCache.EVICTION_POLICY_LRU), LFU(LocalCache.EVICTION_POLICY_LFU);

        final int type;

        Eviction(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
