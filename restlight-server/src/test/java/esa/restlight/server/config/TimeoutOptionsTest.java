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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TimeoutOptionsTest {

    @Test
    void testConfigure() {
        final TimeoutOptions options = TimeoutOptionsConfigure.newOpts()
                .timeMillis(100L)
                .type(TimeoutOptions.Type.TTFB)
                .configured();

        assertEquals(100L, options.getTimeMillis());
        assertSame(TimeoutOptions.Type.TTFB, options.getType());
    }

    @Test
    void testDefaultOpts() {
        final TimeoutOptions options = TimeoutOptionsConfigure.defaultOpts();
        assertEquals(-1L, options.getTimeMillis());
        assertSame(TimeoutOptions.Type.QUEUED, options.getType());
    }

}

