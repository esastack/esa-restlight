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
package io.esastack.restlight.ext.filter.connectionlimit;

public final class ConnectionLimitOptionsConfigure {

    private int maxPerSecond = 20000;

    private ConnectionLimitOptionsConfigure() {
    }

    public static ConnectionLimitOptionsConfigure newOpts() {
        return new ConnectionLimitOptionsConfigure();
    }

    public static ConnectionLimitOptions defaultOpts() {
        return newOpts().configured();
    }

    /**
     * @deprecated use {@link #maxPerSecond(int)}
     */
    @Deprecated
    public ConnectionLimitOptionsConfigure maxCreationPerSecond(int maxCreationPerSecond) {
        return maxPerSecond(maxCreationPerSecond);
    }

    public ConnectionLimitOptionsConfigure maxPerSecond(int maxPerSecond) {
        this.maxPerSecond = maxPerSecond;
        return this;
    }

    public ConnectionLimitOptions configured() {
        final ConnectionLimitOptions options = new ConnectionLimitOptions();
        options.setMaxPerSecond(maxPerSecond);
        return options;
    }
}
