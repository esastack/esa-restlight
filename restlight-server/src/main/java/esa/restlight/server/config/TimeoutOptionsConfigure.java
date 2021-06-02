/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.restlight.server.config;

public class TimeoutOptionsConfigure {

    private long millisTime = -1L;
    private TimeoutOptions.Type type = TimeoutOptions.Type.QUEUED;

    private TimeoutOptionsConfigure() {
    }

    public static TimeoutOptionsConfigure newOpts() {
        return new TimeoutOptionsConfigure();
    }

    public static TimeoutOptions defaultOpts() {
        return newOpts().configured();
    }

    public TimeoutOptionsConfigure millisTime(long timeoutMillis) {
        this.millisTime = timeoutMillis;
        return this;
    }

    public TimeoutOptionsConfigure type(TimeoutOptions.Type timeoutType) {
        this.type = timeoutType;
        return this;
    }

    public TimeoutOptions configured() {
        TimeoutOptions timeoutOptions = new TimeoutOptions();
        timeoutOptions.setType(type);
        timeoutOptions.setMillisTime(millisTime);
        return timeoutOptions;
    }

}

