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
package com.github.benmanes.caffeine.jcache.management;

import org.jspecify.annotations.Nullable;
import static java.util.Locale.US;
import java.lang.management.ManagementFactory;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Jmx cache utilities.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class JmxRegistration {

    private JmxRegistration() {
    }

    public static void registerMxBean(Cache<?, ?> cache, Object mxbean, MBeanType type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static void unregisterMxBean(Cache<?, ?> cache, MBeanType type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static void register(MBeanServer server, ObjectName objectName, Object mbean) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static void unregister(MBeanServer server, ObjectName objectName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static ObjectName getObjectName(Cache<?, ?> cache, MBeanType type) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static ObjectName newObjectName(String name) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static String sanitize(@Nullable String name) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public enum MBeanType {

        CONFIGURATION, STATISTICS;

        private String formatted() {
            return Character.toUpperCase(name().charAt(0)) + name().toLowerCase(US).substring(1);
        }
    }
}
