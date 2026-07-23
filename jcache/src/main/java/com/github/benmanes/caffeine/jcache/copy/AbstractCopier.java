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
package com.github.benmanes.caffeine.jcache.copy;

import static java.util.Objects.requireNonNull;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * A skeleton implementation where subclasses provide the serialization strategy. Serialization is
 * not performed if the type is a known immutable, an array of known immutable types, or specially
 * handled by a known cloning strategy.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@SuppressWarnings({ "ImmutableMemberCollection", "JavaUtilDate" })
public abstract class AbstractCopier<A> implements Copier {

    private static final Map<Class<?>, Function<Object, Object>> JAVA_DEEP_COPY = Map.of(Date.class, o -> ((Date) o).clone(), GregorianCalendar.class, o -> ((GregorianCalendar) o).clone());

    private static final Set<Class<?>> JAVA_IMMUTABLE = Set.of(Boolean.class, Byte.class, Character.class, Double.class, Float.class, Short.class, Integer.class, Long.class, BigInteger.class, BigDecimal.class, String.class, Class.class, UUID.class, URL.class, URI.class, Pattern.class, Inet4Address.class, Inet6Address.class, InetSocketAddress.class, LocalDate.class, LocalTime.class, LocalDateTime.class, Instant.class, Duration.class);

    private final Set<Class<?>> immutableClasses;

    private final Map<Class<?>, Function<Object, Object>> deepCopyStrategies;

    protected AbstractCopier() {
        this(javaImmutableClasses(), javaDeepCopyStrategies());
    }

    protected AbstractCopier(Set<Class<?>> immutableClasses, Map<Class<?>, Function<Object, Object>> deepCopyStrategies) {
        this.immutableClasses = requireNonNull(immutableClasses);
        this.deepCopyStrategies = requireNonNull(deepCopyStrategies);
    }

    public static Set<Class<?>> javaImmutableClasses() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Map<Class<?>, Function<Object, Object>> javaDeepCopyStrategies() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public <T> T copy(T object, ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean isImmutable(Class<?> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected boolean canDeeplyCopy(Class<?> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * @return if the class represents an array of immutable values.
     */
    private boolean isArrayOfImmutableTypes(Class<?> clazz) {
        if (!clazz.isArray()) {
            return false;
        }
        Class<?> component = clazz.getComponentType();
        return component.isPrimitive() || isImmutable(component);
    }

    /**
     * @return a shallow copy of the array.
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static <T> T arrayCopy(T object) {
        int length = Array.getLength(object);
        @SuppressWarnings("unchecked")
        var copy = (T) Array.newInstance(object.getClass().getComponentType(), length);
        System.arraycopy(object, 0, copy, 0, length);
        return copy;
    }

    protected <T> T roundtrip(T object, ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Serializes the object.
     *
     * @param object the object to serialize
     * @return the serialized bytes
     */
    protected abstract A serialize(Object object);

    /**
     * Deserializes the data using the provided classloader.
     *
     * @param data the serialized bytes
     * @param classLoader the classloader to create the instance with
     * @return the deserialized object
     */
    protected abstract Object deserialize(A data, ClassLoader classLoader);
}
