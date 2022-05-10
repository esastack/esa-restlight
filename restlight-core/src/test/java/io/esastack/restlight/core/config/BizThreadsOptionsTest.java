/*
 * Copyright 2022 OPPO ESA Stack Project
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BizThreadsOptionsTest {

    @Test
    void testConfigure() {
        final BizThreadsOptions options = BizThreadsOptionsConfigure.newOpts()
                .core(1)
                .max(2)
                .blockingQueueLength(3)
                .keepAliveTimeSeconds(4L)
                .configured();

        assertEquals(1, options.getCore());
        assertEquals(2, options.getMax());
        assertEquals(3, options.getBlockingQueueLength());
        assertEquals(4L, options.getKeepAliveTimeSeconds());
    }

    @Test
    void testDefaultOpts() {
        final BizThreadsOptions options = BizThreadsOptionsConfigure.defaultOpts();
        final BizThreadsOptions def = new BizThreadsOptions();
        assertEquals(def.getCore(), options.getCore());
        assertEquals(def.getMax(), options.getMax());
        assertEquals(def.getBlockingQueueLength(), options.getBlockingQueueLength());
        assertEquals(def.getKeepAliveTimeSeconds(), options.getKeepAliveTimeSeconds());
    }
}

