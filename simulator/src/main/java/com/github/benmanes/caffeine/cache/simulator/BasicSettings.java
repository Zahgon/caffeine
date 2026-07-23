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
package com.github.benmanes.caffeine.cache.simulator;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.membership.FilterType;
import com.github.benmanes.caffeine.cache.simulator.parser.TraceFormat;
import com.github.benmanes.caffeine.cache.simulator.report.ReportFormat;
import com.google.common.base.CaseFormat;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

/**
 * The simulator's configuration. A policy can extend this class as a convenient way to extract
 * its own settings.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public class BasicSettings {

    private static final Pattern NUMERIC_SEPARATOR = Pattern.compile("[_,]");

    private final Config config;

    public BasicSettings(Config config) {
        this.config = requireNonNull(config);
    }

    public ActorSettings actor() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public ReportSettings report() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int randomSeed() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Set<String> policies() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Set<Admission> admission() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public MembershipSettings membership() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public TinyLfuSettings tinyLfu() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long maximumSize() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public TraceSettings trace() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Config config() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected int getFormattedInt(String path) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected long getFormattedLong(String path) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private <T extends Number> T parseFormattedNumber(String path, Function<String, T> getter, Function<String, @Nullable T> tryParse) {
        try {
            return getter.apply(path);
        } catch (ConfigException.Parse | ConfigException.WrongType e) {
            var matcher = NUMERIC_SEPARATOR.matcher(config().getString(path));
            @Nullable
            T value = tryParse.apply(matcher.replaceAll(""));
            if (value == null) {
                throw e;
            }
            return value;
        }
    }

    public final class ActorSettings {

        public int mailboxSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public int batchSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public final class ReportSettings {

        public ReportFormat format() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public String sortBy() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public boolean ascending() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public String output() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public final class MembershipSettings {

        public FilterType filter() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public long expectedInsertions() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double fpp() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public final class TinyLfuSettings {

        public String sketch() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public boolean conservative() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public JitterSettings jitter() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public CountMin4Settings countMin4() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public CountMin64Settings countMin64() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public final class JitterSettings {

            public boolean enabled() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public int threshold() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double probability() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class CountMin4Settings {

            public String reset() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double countersMultiplier() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public IncrementalSettings incremental() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public PeriodicSettings periodic() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public final class IncrementalSettings {

                public int interval() {
                    throw new UnsupportedOperationException("STUB: not implemented");
                }
            }

            public final class PeriodicSettings {

                public DoorkeeperSettings doorkeeper() {
                    throw new UnsupportedOperationException("STUB: not implemented");
                }
            }
        }

        public final class CountMin64Settings {

            public double eps() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double confidence() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class DoorkeeperSettings {

            public boolean enabled() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
    }

    public final class TraceSettings {

        public long skip() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public long limit() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public boolean isFiles() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public boolean isSynthetic() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public TraceFilesSettings traceFiles() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public SyntheticSettings synthetic() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public final class TraceFilesSettings {

        public List<String> paths() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public TraceFormat format() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public final class SyntheticSettings {

        public String distribution() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public int events() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public CounterSettings counter() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public RepeatSettings repeating() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public UniformSettings uniform() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public ExponentialSettings exponential() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public HotspotSettings hotspot() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public ZipfianSettings zipfian() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public final class CounterSettings {

            public int start() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class RepeatSettings {

            public int items() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class UniformSettings {

            public int lowerBound() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public int upperBound() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class ExponentialSettings {

            public double mean() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class HotspotSettings {

            public int lowerBound() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public int upperBound() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double hotsetFraction() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double hotOpnFraction() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }

        public final class ZipfianSettings {

            public int items() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }

            public double constant() {
                throw new UnsupportedOperationException("STUB: not implemented");
            }
        }
    }
}
