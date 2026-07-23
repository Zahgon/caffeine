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

import static com.google.common.base.Preconditions.checkState;
import static com.hazelcast.config.MaxSizePolicy.ENTRY_COUNT;
import static java.util.Locale.US;
import static java.util.stream.Collectors.toUnmodifiableSet;
import java.util.EnumSet;
import java.util.Set;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.PolicySpec;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.CaseFormat;
import com.google.common.base.Enums;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.ManagedContext;
import com.hazelcast.internal.nearcache.NearCache;
import com.hazelcast.internal.nearcache.impl.DefaultNearCache;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.partition.PartitioningStrategy;
import com.typesafe.config.Config;

/**
 * Hazelcast cache implementation.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@PolicySpec(name = "product.Hazelcast")
public final class HazelcastPolicy implements Policy {

    private final NearCache<Long, Boolean> cache;

    private final PolicyStats policyStats;

    private final int maximumSize;

    public HazelcastPolicy(HazelcastSettings settings, EvictionPolicy policy) {
        policyStats = new PolicyStats(name() + " (%s)", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, policy.name()));
        maximumSize = Math.toIntExact(settings.maximumSize());
        var config = new NearCacheConfig().setSerializeKeys(false).setInMemoryFormat(InMemoryFormat.OBJECT).setEvictionConfig(new EvictionConfig().setMaxSizePolicy(ENTRY_COUNT).setEvictionPolicy(policy).setSize(maximumSize));
        cache = new DefaultNearCache<>("simulation", config, DummySerializationService.INSTANCE, /* scheduler= */
        null, getClass().getClassLoader(), /* properties= */
        null);
        cache.initialize();
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

    public static final class HazelcastSettings extends BasicSettings {

        public HazelcastSettings(Config config) {
            super(config);
        }

        public Set<EvictionPolicy> policy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    @SuppressWarnings({ "rawtypes", "TypeParameterUnusedInFormals", "unchecked" })
    enum DummySerializationService implements SerializationService {

        INSTANCE;

        @Override
        public <B extends Data> B toData(Object obj) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <B extends Data> B toDataWithSchema(Object obj) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <B extends Data> B toData(Object obj, PartitioningStrategy strategy) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <T> T toObject(Object data) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <T> T toObject(Object data, Class klazz) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public ManagedContext getManagedContext() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public <B extends Data> B trimSchema(Data data) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
