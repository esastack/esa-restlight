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
package io.esastack.restlight.server.config;

import esa.commons.Platforms;

import java.io.Serializable;

public class BizThreadsOptions implements Serializable {

    private static final long serialVersionUID = 7719795134485906225L;

    /**
     * core biz thread count, cpu * 4 (must between 32 and 128)
     */
    private int core =
            Math.min(Math.max(64, Platforms.cpuNum() << 2), 128);

    /**
     * maximum biz thread count, cpu * 6 (must between 128 and 256)
     */
    private int max =
            Math.min(Math.max(128, (Platforms.cpuNum() << 2) + (Platforms.cpuNum() << 1)), 256);

    /**
     * maximum waiting queue length
     */
    private int blockingQueueLength = 512;

    /**
     * thread pool keepAlive time(default to 180s)
     */
    private long keepAliveTimeSeconds = 180L;

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getBlockingQueueLength() {
        return blockingQueueLength;
    }

    public void setBlockingQueueLength(int blockingQueueLength) {
        this.blockingQueueLength = blockingQueueLength;
    }

    public long getKeepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizThreadsOptions{");
        sb.append("core=").append(core);
        sb.append(", max=").append(max);
        sb.append(", blockingQueueLength=").append(blockingQueueLength);
        sb.append(", keepAliveTimeSeconds=").append(keepAliveTimeSeconds);
        sb.append('}');
        return sb.toString();
    }
}

