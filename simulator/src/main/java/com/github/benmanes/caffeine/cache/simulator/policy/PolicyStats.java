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
package com.github.benmanes.caffeine.cache.simulator.policy;

import static com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic.WEIGHTED;
import static com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats.Metric.MetricType.NUMBER;
import static com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats.Metric.MetricType.OBJECT;
import static com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats.Metric.MetricType.PERCENT;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.Characteristic;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Statistics gathered by a policy execution. A policy can extend this class as a convenient way to
 * add custom metrics.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("unused")
public class PolicyStats {

    private final Map<String, Metric> metrics;

    private final Stopwatch stopwatch;

    private final String name;

    private long hitCount;

    private long missCount;

    private long hitsWeight;

    private long missesWeight;

    private double hitPenalty;

    private double missPenalty;

    private long evictionCount;

    private long admittedCount;

    private long rejectedCount;

    private long operationCount;

    private double percentAdaption;

    @SuppressFBWarnings("FORMAT_STRING_MANIPULATION")
    @SuppressWarnings({ "AnnotateFormatMethod", "this-escape" })
    public PolicyStats(String format, Object... args) {
        this.stopwatch = Stopwatch.createUnstarted();
        this.name = String.format(US, format, args);
        this.metrics = new LinkedHashMap<>();
        addMetric(new Metric.Builder().name("Policy").value(this::name).type(OBJECT).required(true));
        addMetric(new Metric.Builder().name("Hit Rate").value(this::hitRate).type(PERCENT).required(true));
        addMetric(new Metric.Builder().name("Miss Rate").value(this::missRate).type(PERCENT).required(true));
        addMetric(new Metric.Builder().name("Hits").value(this::hitCount).type(NUMBER).required(true));
        addMetric(new Metric.Builder().name("Misses").value(this::missCount).type(NUMBER).required(true));
        addMetric(new Metric.Builder().name("Requests").value(this::requestCount).type(NUMBER).required(true));
        addMetric(new Metric.Builder().name("Evictions").value(this::evictionCount).type(NUMBER).required(true));
        addPercentMetric("Admit rate", () -> (admittedCount + rejectedCount) == 0 ? 0 : admissionRate());
        addMetric(new Metric.Builder().name("Requests Weight").value(this::requestsWeight).type(NUMBER).characteristic(WEIGHTED));
        addMetric(new Metric.Builder().name("Weighted Hit Rate").value(this::weightedHitRate).characteristic(WEIGHTED).type(PERCENT));
        addMetric(new Metric.Builder().name("Weighted Miss Rate").value(this::weightedMissRate).type(PERCENT).characteristic(WEIGHTED));
        addPercentMetric("Adaption", this::percentAdaption);
        addMetric("Average Miss Penalty", this::averageMissPenalty);
        addMetric("Average Penalty", this::averagePenalty);
        addMetric("Steps", this::operationCount);
        addMetric("Time", this::stopwatch);
    }

    public void addMetric(Metric.Builder metricBuilder) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addMetric(String name, Supplier<?> supplier) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addMetric(String name, LongSupplier supplier) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addMetric(String name, DoubleSupplier supplier) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addPercentMetric(String name, DoubleSupplier supplier) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Map<String, Metric> metrics() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Stopwatch stopwatch() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String name() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordOperation() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long operationCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addOperations(long operations) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordHit() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long hitCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addHits(long hits) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordWeightedHit(int weight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long hitsWeight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordHitPenalty(double penalty) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double hitPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordMiss() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long missCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addMisses(long misses) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordWeightedMiss(int weight) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long missesWeight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordMissPenalty(double penalty) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double missPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long evictionCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordEviction() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void addEvictions(long evictions) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long requestCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long requestsWeight() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long admissionCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordAdmission() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long rejectionCount() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void recordRejection() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double totalPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double percentAdaption() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void setPercentAdaption(double percentAdaption) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double hitRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double weightedHitRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double missRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double weightedMissRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double admissionRate() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double complexity() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double averagePenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double averageHitPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double averageMissPenalty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public record Metric(String name, Object value, MetricType type, ImmutableSet<Characteristic> characteristics, boolean required) {

        public enum MetricType {

            NUMBER, PERCENT, OBJECT
        }

        public Metric {
            requireNonNull(type);
            requireNonNull(name);
            requireNonNull(value);
            requireNonNull(characteristics);
        }

        public static final class Builder {

            private final Set<Characteristic> characteristics;

            @Nullable
            private MetricType type;

            @Nullable
            private Object value;

            @Nullable
            private String name;

            private boolean required;

            public Builder() {
                characteristics = EnumSet.noneOf(Characteristic.class);
            }

            @CanIgnoreReturnValue
            public Builder characteristic(Characteristic characteristic) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder name(String name) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder value(Object value) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder value(Supplier<?> value) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder value(LongSupplier value) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder value(DoubleSupplier value) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder type(MetricType type) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            @CanIgnoreReturnValue
            public Builder required(boolean required) {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public Metric build() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
    }
}
