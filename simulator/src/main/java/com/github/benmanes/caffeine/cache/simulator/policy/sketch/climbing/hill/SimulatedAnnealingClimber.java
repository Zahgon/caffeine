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

import java.util.Random;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing.AbstractClimber;
import com.typesafe.config.Config;

/**
 * A simulated annealing hill climber.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class SimulatedAnnealingClimber extends AbstractClimber {

    private final double coolDownTolerance;

    private final double restartTolerance;

    private final double minTemperature;

    private final double coolDownRate;

    private final int initialStepSize;

    private final Random random;

    private boolean increaseWindow;

    private double temperature;

    private int stepSize;

    public SimulatedAnnealingClimber(Config config) {
        var settings = new SimulatedAnnealingSettings(config);
        int maximumSize = Math.toIntExact(settings.maximumSize());
        this.initialStepSize = (int) (settings.percentPivot() * maximumSize);
        this.sampleSize = (int) (settings.percentSample() * maximumSize);
        this.coolDownTolerance = 100 * settings.coolDownTolerance();
        this.restartTolerance = 100 * settings.restartTolerance();
        this.random = new Random(settings.randomSeed());
        this.minTemperature = settings.minTemperature();
        this.coolDownRate = settings.coolDownRate();
        restart();
    }

    private void restart() {
        stepSize = initialStepSize;
        temperature = 1.0;
    }

    @Override
    protected double adjust(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static final class SimulatedAnnealingSettings extends BasicSettings {

        static final String BASE_PATH = "hill-climber-window-tiny-lfu.simulated-annealing.";

        public SimulatedAnnealingSettings(Config config) {
            super(config);
        }

        public double percentPivot() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentSample() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double coolDownRate() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double minTemperature() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double restartTolerance() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double coolDownTolerance() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
