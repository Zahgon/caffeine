/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
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

import static com.github.benmanes.caffeine.cache.Caffeine.requireArgument;
import static com.github.benmanes.caffeine.cache.Caffeine.requireState;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.Caffeine.Strength;

/**
 * A specification of a {@link Caffeine} builder configuration.
 * <p>
 * {@code CaffeineSpec} supports parsing configuration from a string, which makes it especially
 * useful for command-line configuration of a {@code Caffeine} builder.
 * <p>
 * The string syntax is a series of comma-separated keys or key-value pairs, each corresponding to a
 * {@code Caffeine} builder method.
 * <ul>
 *   <li>{@code initialCapacity=[integer]}: sets {@link Caffeine#initialCapacity}.
 *   <li>{@code maximumSize=[long]}: sets {@link Caffeine#maximumSize}.
 *   <li>{@code maximumWeight=[long]}: sets {@link Caffeine#maximumWeight}.
 *   <li>{@code expireAfterAccess=[duration]}: sets {@link Caffeine#expireAfterAccess}.
 *   <li>{@code expireAfterWrite=[duration]}: sets {@link Caffeine#expireAfterWrite}.
 *   <li>{@code refreshAfterWrite=[duration]}: sets {@link Caffeine#refreshAfterWrite}.
 *   <li>{@code weakKeys}: sets {@link Caffeine#weakKeys}.
 *   <li>{@code weakValues}: sets {@link Caffeine#weakValues}.
 *   <li>{@code softValues}: sets {@link Caffeine#softValues}.
 *   <li>{@code recordStats}: sets {@link Caffeine#recordStats}.
 * </ul>
 * <p>
 * Durations are represented as either an ISO-8601 string using {@link Duration#parse(CharSequence)}
 * or by an integer followed by one of "d", "h", "m", or "s", representing days, hours, minutes, or
 * seconds, respectively. There is currently no short syntax to request durations in milliseconds,
 * microseconds, or nanoseconds.
 * <p>
 * Whitespace before and after commas and equal signs is ignored. Keys may not be repeated; it is
 * also illegal to use the following pairs of keys in a single value:
 * <ul>
 *   <li>{@code maximumSize} and {@code maximumWeight}
 *   <li>{@code weakValues} and {@code softValues}
 * </ul>
 * <p>
 * {@code CaffeineSpec} does not support configuring {@code Caffeine} methods with non-value
 * parameters. These must be configured in code.
 * <p>
 * A new {@code Caffeine} builder can be instantiated from a {@code CaffeineSpec} using
 * {@link Caffeine#from(CaffeineSpec)} or {@link Caffeine#from(String)}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
public final class CaffeineSpec {

    static final String SPLIT_OPTIONS = ",";

    static final String SPLIT_KEY_VALUE = "=";

    final String specification;

    boolean recordStats;

    @Nullable
    Long maximumSize;

    @Nullable
    Long maximumWeight;

    @Nullable
    Strength keyStrength;

    @Nullable
    Strength valueStrength;

    @Nullable
    Integer initialCapacity;

    @Nullable
    Duration expireAfterWrite;

    @Nullable
    Duration expireAfterAccess;

    @Nullable
    Duration refreshAfterWrite;

    private CaffeineSpec(String specification) {
        this.specification = requireNonNull(specification);
        @SuppressWarnings("StringSplitter")
        var options = specification.split(SPLIT_OPTIONS);
        for (String option : options) {
            parseOption(option.strip());
        }
    }

    Caffeine<Object, Object> toBuilder() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static CaffeineSpec parse(String specification) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void parseOption(String option) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void configure(String option, String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void initialCapacity(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void maximumSize(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void maximumWeight(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void weakKeys(@Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void valueStrength(String key, @Nullable String value, Strength strength) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void expireAfterAccess(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void expireAfterWrite(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void refreshAfterWrite(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    void recordStats(@Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static int parseInt(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static long parseLong(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static String normalizeNumericLiteral(String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Duration parseDuration(String key, @Nullable String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Duration parseIsoDuration(String key, String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Duration parseSimpleDuration(String key, String value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings({ "ConstantValue", "StatementSwitchToExpressionSwitch" })
    static TimeUnit parseTimeUnit(String key, String value) {
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

    public String toParsableString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
