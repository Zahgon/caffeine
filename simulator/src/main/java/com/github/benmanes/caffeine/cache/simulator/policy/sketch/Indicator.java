/*
 * Copyright 2018 Ohad Eytan. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch;

import java.util.Arrays;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import com.clearspring.analytics.stream.StreamSummary;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.countmin4.PeriodicResetCountMin4;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * An indicator for the recency vs. frequency bias.
 *
 * @author ohadey@gmail.com (Ohad Eytan)
 */
public final class Indicator {

    private final PeriodicResetCountMin4 sketch;

    private final EstSkew estSkew;

    private final Hinter hinter;

    private final int k;

    private long sample;

    public Indicator(Config config) {
        var settings = new IndicatorSettings(config);
        this.sketch = new PeriodicResetCountMin4(ConfigFactory.parseString("maximum-size = 5000").withFallback(config));
        this.estSkew = new EstSkew(settings.ssSize());
        this.hinter = new Hinter();
        this.k = settings.k();
    }

    public void record(long key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void reset() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long getSample() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unused")
    public int[] getFreqs() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double getSkew() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double getHint() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double getIndicator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static final class Hinter {

        final int[] freq = new int[16];

        int sum;

        int count;

        public void increment(int i) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void reset() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double getAverage() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class EstSkew {

        final int ssSize;

        StreamSummary<Long> stream;

        public EstSkew(int ssSize) {
            this.stream = new StreamSummary<>(ssSize);
            this.ssSize = ssSize;
        }

        public void record(long key) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public void reset() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public IntStream getTopK(int k) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double estSkew(int k) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    static final class IndicatorSettings extends BasicSettings {

        public IndicatorSettings(Config config) {
            super(config);
        }

        public int k() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public int ssSize() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
