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

import esa.commons.Platforms;

public class BizThreadsOptionsConfigure {

    private int core =
            Math.min(Math.max(64, Platforms.cpuNum() << 2), 128);

    private int max =
            Math.min(Math.max(128, (Platforms.cpuNum() << 2) + (Platforms.cpuNum() << 1)), 256);

    private int blockingQueueLength = 512;

    private long keepAliveTimeSeconds = 180L;

    private BizThreadsOptionsConfigure() {
    }

    public static BizThreadsOptionsConfigure newOpts() {
        return new BizThreadsOptionsConfigure();
    }

    public static BizThreadsOptions defaultOpts() {
        return newOpts().configured();
    }

    public BizThreadsOptionsConfigure core(int core) {
        this.core = core;
        return this;
    }

    public BizThreadsOptionsConfigure max(int max) {
        this.max = max;
        return this;
    }

    public BizThreadsOptionsConfigure blockingQueueLength(int blockingQueueLength) {
        this.blockingQueueLength = blockingQueueLength;
        return this;
    }

    public BizThreadsOptionsConfigure keepAliveTimeSeconds(long keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        return this;
    }

    public BizThreadsOptions configured() {
        BizThreadsOptions bizThreadsOptions = new BizThreadsOptions();
        bizThreadsOptions.setCore(core);
        bizThreadsOptions.setMax(max);
        bizThreadsOptions.setBlockingQueueLength(blockingQueueLength);
        bizThreadsOptions.setKeepAliveTimeSeconds(keepAliveTimeSeconds);
        return bizThreadsOptions;
    }
}

