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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing.hill;

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing.AbstractClimber;
import com.typesafe.config.Config;

/**
 * A naive, simple hill climber.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class SimpleClimber extends AbstractClimber {

    private final double restartThreshold;

    private final double initialStepSize;

    private final double sampleDecayRate;

    private final int initialSampleSize;

    private final double stepDecayRate;

    private final double tolerance;

    private boolean increaseWindow;

    private double stepSize;

    public SimpleClimber(Config config) {
        var settings = new SimpleClimberSettings(config);
        int maximumSize = Math.toIntExact(settings.maximumSize());
        this.initialSampleSize = (int) (settings.percentSample() * maximumSize);
        this.initialStepSize = settings.percentPivot() * maximumSize;
        this.restartThreshold = settings.restartThreshold();
        this.sampleDecayRate = settings.sampleDecayRate();
        this.stepDecayRate = settings.stepDecayRate();
        this.tolerance = 100d * settings.tolerance();
        this.sampleSize = initialSampleSize;
        this.stepSize = initialStepSize;
    }

    @Override
    protected double adjust(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    protected void resetSample(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static final class SimpleClimberSettings extends BasicSettings {

        static final String BASE_PATH = "hill-climber-window-tiny-lfu.simple.";

        public SimpleClimberSettings(Config config) {
            super(config);
        }

        public double percentPivot() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentSample() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double tolerance() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double stepDecayRate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double sampleDecayRate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double restartThreshold() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
