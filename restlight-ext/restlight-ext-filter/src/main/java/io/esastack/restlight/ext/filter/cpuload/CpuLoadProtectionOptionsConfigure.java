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

public final class CpuLoadProtectionOptionsConfigure {

    private double threshold = 80.0D;
    private double initialDiscardRate = 10.0D;
    private double maxDiscardRate = 80.0D;

    private CpuLoadProtectionOptionsConfigure() {
    }

    public static CpuLoadProtectionOptionsConfigure newOpts() {
        return new CpuLoadProtectionOptionsConfigure();
    }

    public static CpuLoadProtectionOptions defaultOpts() {
        return newOpts().configured();
    }

    public CpuLoadProtectionOptionsConfigure threshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public CpuLoadProtectionOptionsConfigure initialDiscardRate(double initialDiscardRate) {
        this.initialDiscardRate = initialDiscardRate;
        return this;
    }

    public CpuLoadProtectionOptionsConfigure maxDiscardRate(double maxDiscardRate) {
        this.maxDiscardRate = maxDiscardRate;
        return this;
    }

    public CpuLoadProtectionOptions configured() {
        final CpuLoadProtectionOptions options = new CpuLoadProtectionOptions();
        options.setInitialDiscardRate(initialDiscardRate);
        options.setMaxDiscardRate(maxDiscardRate);
        options.setThreshold(threshold);
        return options;
    }

}
