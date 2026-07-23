/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing.gradient;

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.sketch.climbing.AbstractClimber;
import com.typesafe.config.Config;

/**
 * Adaptive Moment Estimation (Adam) optimizer. Adam is an improvement on stochastic gradient
 * descent with momentum, that incorporates adaptive learning rates. The authors describe it in
 * <a href="https://arxiv.org/abs/1412.6980">Adam: A Method for Stochastic Optimization</a>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class Adam extends AbstractClimber {

    private final int stepSize;

    private final double beta1;

    private final double beta2;

    private final double epsilon;

    private int t;

    private double moment;

    private double velocity;

    public Adam(Config config) {
        var settings = new AdamSettings(config);
        int maximumSize = Math.toIntExact(settings.maximumSize());
        sampleSize = (int) (settings.percentSample() * maximumSize);
        stepSize = (int) (settings.percentPivot() * maximumSize);
        epsilon = settings.epsilon();
        beta1 = settings.beta1();
        beta2 = settings.beta2();
        t = 1;
    }

    @Override
    protected void resetSample(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    protected double adjust(double hitRate) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static final class AdamSettings extends BasicSettings {

        static final String BASE_PATH = "hill-climber-window-tiny-lfu.adam.";

        public AdamSettings(Config config) {
            super(config);
        }

        public double percentPivot() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double percentSample() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double beta1() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double beta2() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public double epsilon() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
