/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.ext.filter.cpuload;

import java.io.Serializable;

public class CpuLoadProtectionOptions implements Serializable {

    private static final long serialVersionUID = 3523217122381975325L;

    private double threshold = 80.0D;
    private double initialDiscardRate = 10.0D;
    private double maxDiscardRate = 80.0D;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getInitialDiscardRate() {
        return initialDiscardRate;
    }

    public void setInitialDiscardRate(double initialDiscardRate) {
        this.initialDiscardRate = initialDiscardRate;
    }

    public double getMaxDiscardRate() {
        return maxDiscardRate;
    }

    public void setMaxDiscardRate(double maxDiscardRate) {
        this.maxDiscardRate = maxDiscardRate;
    }
}
