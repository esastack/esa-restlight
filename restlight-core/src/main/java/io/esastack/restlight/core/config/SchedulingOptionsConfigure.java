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
package io.esastack.restlight.core.config;

import io.esastack.restlight.core.server.processor.schedule.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SchedulingOptionsConfigure {

    private String defaultScheduler = Schedulers.BIZ;

    private Map<String, TimeoutOptions> timeout = new LinkedHashMap<>(1);

    private SchedulingOptionsConfigure() {
    }

    public static SchedulingOptionsConfigure newOpts() {
        return new SchedulingOptionsConfigure();
    }

    public static SchedulingOptions defaultOpts() {
        return newOpts().configured();
    }

    public SchedulingOptionsConfigure defaultScheduler(String defaultScheduler) {
        this.defaultScheduler = defaultScheduler;
        return this;
    }

    public SchedulingOptionsConfigure timeout(Map<String, TimeoutOptions> timeout) {
        this.timeout = timeout;
        return this;
    }

    public SchedulingOptions configured() {
        SchedulingOptions schedulingOptions = new SchedulingOptions();
        schedulingOptions.setDefaultScheduler(defaultScheduler);
        schedulingOptions.setTimeout(timeout);
        return schedulingOptions;
    }
}
