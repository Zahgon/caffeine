/*
 * Copyright 2020 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.report;

import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.util.Comparator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats.Metric;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats.Metric.MetricType;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * A utility for performing common operations against a {@link Metric}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public record Metrics(Function<@Nullable Object, String> objectFormatter, LongFunction<String> longFormatter, DoubleFunction<String> percentFormatter, DoubleFunction<String> doubleFormatter) {

    public Metrics {
        requireNonNull(longFormatter);
        requireNonNull(objectFormatter);
        requireNonNull(doubleFormatter);
        requireNonNull(percentFormatter);
    }

    public String format(@Nullable Metric metric) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Comparator<PolicyStats> comparator(String header) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Metrics.Builder builder() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private record MetricComparator(Metrics metrics, String header) implements Comparator<PolicyStats>, Serializable {

        private MetricComparator {
            requireNonNull(metrics);
            requireNonNull(header);
        }

        @Override
        public int compare(PolicyStats p1, PolicyStats p2) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public static final class Builder {

        @Nullable
        private Function<@Nullable Object, String> objectFormatter;

        @Nullable
        private DoubleFunction<String> percentFormatter;

        @Nullable
        private DoubleFunction<String> doubleFormatter;

        @Nullable
        private LongFunction<String> longFormatter;

        @CanIgnoreReturnValue
        public Builder objectFormatter(Function<@Nullable Object, String> objectFormatter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @CanIgnoreReturnValue
        public Builder percentFormatter(DoubleFunction<String> percentFormatter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @CanIgnoreReturnValue
        public Builder doubleFormatter(DoubleFunction<String> doubleFormatter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @CanIgnoreReturnValue
        public Builder longFormatter(LongFunction<String> longFormatter) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Metrics build() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
