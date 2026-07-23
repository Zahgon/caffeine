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
package com.github.benmanes.caffeine.jcache.configuration;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.cache.CacheManager;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.jcache.expiry.JCacheExpiryPolicy;
import com.google.errorprone.annotations.Var;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import jakarta.inject.Inject;

/**
 * Static utility methods pertaining to externalized {@link CaffeineConfiguration} entries using the
 * Typesafe Config library.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
public final class TypesafeConfigurator {

    static final Logger logger = System.getLogger(TypesafeConfigurator.class.getName());

    static final AtomicReference<ConfigSource> configSource = new AtomicReference<>(TypesafeConfigurator::resolveConfig);

    static final AtomicReference<FactoryCreator> factoryCreator = new AtomicReference<>(FactoryBuilder::factoryOf);

    private TypesafeConfigurator() {
    }

    public static Set<String> cacheNames(Config config) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static <K, V> CaffeineConfiguration<K, V> defaults(Config config) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static <K, V> Optional<CaffeineConfiguration<K, V>> from(Config config, String cacheName) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Inject
    @SuppressWarnings({ "deprecation", "UnnecessarilyVisible" })
    public static void setFactoryCreator(FactoryCreator factoryCreator) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static FactoryCreator factoryCreator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static void setConfigSource(Supplier<Config> configSource) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static void setConfigSource(ConfigSource configSource) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static ConfigSource configSource() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Returns the configuration by applying the default strategy.
     */
    private static Config resolveConfig(URI uri, ClassLoader classloader) {
        requireNonNull(uri);
        requireNonNull(classloader);
        var options = ConfigParseOptions.defaults().setClassLoader(classloader).setAllowMissing(false);
        if ((uri.getScheme() != null) && uri.getScheme().equalsIgnoreCase("file")) {
            return ConfigFactory.defaultOverrides(classloader).withFallback(ConfigFactory.parseFile(new File(uri), options)).withFallback(ConfigFactory.defaultReferenceUnresolved(classloader));
        } else if ((uri.getScheme() != null) && uri.getScheme().equalsIgnoreCase("jar")) {
            try {
                return ConfigFactory.defaultOverrides(classloader).withFallback(ConfigFactory.parseURL(uri.toURL(), options)).withFallback(ConfigFactory.defaultReferenceUnresolved(classloader));
            } catch (MalformedURLException e) {
                throw new ConfigException.BadPath(uri.toString(), "Failed to load cache configuration", e);
            }
        } else if (isResource(uri)) {
            return ConfigFactory.defaultOverrides(classloader).withFallback(ConfigFactory.parseResources(uri.getSchemeSpecificPart(), options)).withFallback(ConfigFactory.defaultReferenceUnresolved(classloader));
        }
        return ConfigFactory.load(classloader);
    }

    /**
     * Returns if the uri is a file or classpath resource.
     */
    private static boolean isResource(URI uri) {
        if ((uri.getScheme() != null) && !uri.getScheme().equalsIgnoreCase("classpath")) {
            return false;
        }
        var path = uri.getSchemeSpecificPart();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            var extension = path.substring(dotIndex + 1);
            for (var format : ConfigSyntax.values()) {
                if (format.toString().equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A one-shot builder for creating a configuration instance.
     */
    static final class Configurator<K, V> {

        final CaffeineConfiguration<K, V> configuration;

        final Config customized;

        final Config merged;

        final Config root;

        Configurator(Config config, String cacheName) {
            this.root = requireNonNull(config);
            this.configuration = new CaffeineConfiguration<>();
            this.customized = root.getConfig("caffeine.jcache." + requireNonNull(cacheName));
            this.merged = customized.withFallback(root.getConfig("caffeine.jcache.default"));
        }

        CaffeineConfiguration<K, V> configure() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Adds the key and value class types.
         */
        private void addKeyValueTypes() {
            try {
                @SuppressWarnings("unchecked")
                var keyType = (Class<K>) Class.forName(merged.getString("key-type"));
                @SuppressWarnings("unchecked")
                var valueType = (Class<V>) Class.forName(merged.getString("value-type"));
                configuration.setTypes(keyType, valueType);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        /**
         * Adds the store-by-value settings.
         */
        private void addStoreByValue() {
            configuration.setStoreByValue(merged.getBoolean("store-by-value.enabled"));
            if (isSet("store-by-value.strategy")) {
                configuration.setCopierFactory(factoryCreator().factoryOf(merged.getString("store-by-value.strategy")));
            }
        }

        public void addExecutor() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void addScheduler() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Adds the entry listeners settings.
         */
        private void addListeners() {
            for (String path : merged.getStringList("listeners")) {
                Config listener = root.getConfig(path);
                Factory<? extends CacheEntryListener<? super K, ? super V>> listenerFactory = factoryCreator().factoryOf(listener.getString("class"));
                @Var
                Factory<? extends CacheEntryEventFilter<? super K, ? super V>> filterFactory = null;
                if (listener.hasPath("filter")) {
                    filterFactory = factoryCreator().factoryOf(listener.getString("filter"));
                }
                boolean oldValueRequired = listener.getBoolean("old-value-required");
                boolean synchronous = listener.getBoolean("synchronous");
                configuration.addCacheEntryListenerConfiguration(new MutableCacheEntryListenerConfiguration<>(listenerFactory, filterFactory, oldValueRequired, synchronous));
            }
        }

        /**
         * Adds the read through settings.
         */
        private void addReadThrough() {
            configuration.setReadThrough(merged.getBoolean("read-through.enabled"));
            if (isSet("read-through.loader")) {
                configuration.setCacheLoaderFactory(factoryCreator().factoryOf(merged.getString("read-through.loader")));
            }
        }

        /**
         * Adds the write-through settings.
         */
        private void addWriteThrough() {
            configuration.setWriteThrough(merged.getBoolean("write-through.enabled"));
            if (isSet("write-through.writer")) {
                configuration.setCacheWriterFactory(factoryCreator().factoryOf(merged.getString("write-through.writer")));
            }
        }

        /**
         * Adds the monitoring settings.
         */
        private void addMonitoring() {
            configuration.setNativeStatisticsEnabled(merged.getBoolean("monitoring.native-statistics"));
            configuration.setStatisticsEnabled(merged.getBoolean("monitoring.statistics"));
            configuration.setManagementEnabled(merged.getBoolean("monitoring.management"));
        }

        public void addLazyExpiration() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Returns the duration for the expiration time.
         */
        @Nullable
        private Duration getDurationFor(String path) {
            if (!isSet(path)) {
                return null;
            }
            if (merged.getString(path).equalsIgnoreCase("eternal")) {
                return Duration.ETERNAL;
            }
            long millis = merged.getDuration(path, MILLISECONDS);
            return new Duration(MILLISECONDS, millis);
        }

        public void addEagerExpiration() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void addRefresh() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        /**
         * Adds the maximum size and weight bounding settings.
         */
        private void addMaximum() {
            if (isSet("policy.maximum.size")) {
                configuration.setMaximumSize(OptionalLong.of(merged.getLong("policy.maximum.size")));
            }
            if (isSet("policy.maximum.weight")) {
                configuration.setMaximumWeight(OptionalLong.of(merged.getLong("policy.maximum.weight")));
            }
            if (isSet("policy.maximum.weigher")) {
                configuration.setWeigherFactory(Optional.of(FactoryBuilder.factoryOf(merged.getString("policy.maximum.weigher"))));
            }
        }

        /**
         * Returns if the value is present (not unset by the cache configuration).
         */
        private boolean isSet(String path) {
            if (!merged.hasPath(path)) {
                return false;
            } else if (customized.hasPathOrNull(path)) {
                return !customized.getIsNull(path);
            }
            return true;
        }
    }
}
