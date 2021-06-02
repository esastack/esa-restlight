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

public class FailFastOptionsConfigure {

    private long timeoutMillis = -1L;
    private FailFastOptions.TimeoutType timeoutType = FailFastOptions.TimeoutType.QUEUED;

    private FailFastOptionsConfigure() {
    }

    public static FailFastOptionsConfigure newOpts() {
        return new FailFastOptionsConfigure();
    }

    public static FailFastOptions defaultOpts() {
        return newOpts().configured();
    }

    public FailFastOptionsConfigure timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public FailFastOptionsConfigure timeoutType(FailFastOptions.TimeoutType timeoutType) {
        this.timeoutType = timeoutType;
        return this;
    }

    public FailFastOptions configured() {
        FailFastOptions failFastOptions = new FailFastOptions();
        failFastOptions.setTimeoutType(timeoutType);
        failFastOptions.setTimeoutMillis(timeoutMillis);
        return failFastOptions;
    }

}

