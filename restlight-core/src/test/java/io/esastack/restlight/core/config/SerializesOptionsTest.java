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
package io.esastack.restlight.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SerializesOptionsTest {

    @Test
    void testConfigure() {
        final SerializesOptions options = SerializesOptionsConfigure.newOpts()
                .request(null)
                .response(null)
                .configured();

        assertNull(options.getRequest());
        assertNull(options.getResponse());
    }

    @Test
    void testDefaultOpts() {
        final SerializesOptions options = SerializesOptionsConfigure.defaultOpts();

        assertNotNull(options.getRequest());
        assertNotNull(options.getResponse());
    }

}
