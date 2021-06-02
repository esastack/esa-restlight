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

import java.io.Serializable;

public class FailFastOptions implements Serializable {

    private static final long serialVersionUID = 5509354808610167580L;

    public enum TimeoutType {
        TTFB,

        QUEUED
    }

    private long timeoutMillis = -1L;
    private TimeoutType timeoutType = TimeoutType.QUEUED;

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public TimeoutType getTimeoutType() {
        return timeoutType;
    }

    public void setTimeoutType(TimeoutType timeoutType) {
        this.timeoutType = timeoutType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FailFastOptions{");
        sb.append("timeoutMillis=").append(timeoutMillis);
        sb.append(", timeoutType='").append(timeoutType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

