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
package io.esastack.restlight.core.config;

import java.io.Serializable;

public class TimeoutOptions implements Serializable {

    private static final long serialVersionUID = 5509354808610167580L;

    public enum Type {
        TTFB,

        QUEUED
    }

    private long timeMillis = -1L;
    private Type type = Type.QUEUED;

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeoutOptions{");
        sb.append("timeMillis=").append(timeMillis);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}

