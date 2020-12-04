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
package esa.restlight.starter.autoconfigure;

import java.io.Serializable;

public class WarmUpOptions implements Serializable {

    private static final long serialVersionUID = 363366172142568702L;

    /**
     * Is warm-up enable
     */
    private boolean enable;

    /**
     * delay mills time for starting server.
     */
    private long delay;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        if (delay <= 0) {
            throw new IllegalArgumentException("Warm-up delay must over than zero!");
        }
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "WarmUpOptions{" + "enable=" + enable +
                ", delay=" + delay +
                '}';
    }
}
