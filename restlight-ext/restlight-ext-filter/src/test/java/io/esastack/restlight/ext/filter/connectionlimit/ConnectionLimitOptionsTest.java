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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionLimitOptionsTest {

    @Test
    void testConfigure() {
        final ConnectionLimitOptions options = ConnectionLimitOptionsConfigure.newOpts()
                .maxPerSecond(2)
                .configured();
        assertEquals(2, options.getMaxPerSecond());
        assertEquals(2, options.getMaxPerSecond());
    }

    @Test
    void testDefaultOpts() {
        final ConnectionLimitOptions options = ConnectionLimitOptionsConfigure.defaultOpts();
        final ConnectionLimitOptions def = new ConnectionLimitOptions();

        assertEquals(def.getMaxPerSecond(), options.getMaxPerSecond());
    }

}
