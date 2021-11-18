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

import java.io.Serializable;

public class ConnectionLimitOptions implements Serializable {

    /**
     * Max http connection per second
     */
    private int maxPerSecond = 20000;

    public int getMaxPerSecond() {
        return maxPerSecond;
    }

    public void setMaxPerSecond(int maxPerSecond) {
        this.maxPerSecond = maxPerSecond;
    }

    /**
     * @deprecated use {@link #getMaxPerSecond()}
     */
    @Deprecated
    public int getMaxCreationPerSecond() {
        return getMaxPerSecond();
    }

    /**
     * @deprecated use {@link #setMaxPerSecond(int)}
     */
    @Deprecated
    public void setMaxCreationPerSecond(int maxCreationPerSecond) {
        setMaxPerSecond(maxCreationPerSecond);
    }
}
