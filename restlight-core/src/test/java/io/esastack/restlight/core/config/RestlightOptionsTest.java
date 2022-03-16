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

import static org.junit.jupiter.api.Assertions.*;

class RestlightOptionsTest {

    @Test
    void testConfigure() {
        final RestlightOptions options = RestlightOptionsConfigure.newOpts()
                .contextPath("foo")
                .serialize(null)
                .ext(null)
                .configured();


        assertEquals("foo", options.getContextPath());
        assertNull(options.getSerialize());
        assertNull(options.getExt());
    }

    @Test
    void testDefaultOpts() {
        final RestlightOptions options = RestlightOptionsConfigure.defaultOpts();
        final RestlightOptions def = new RestlightOptions();

        assertEquals(def.getContextPath(), options.getContextPath());
        assertEquals(def.getExt(), options.getExt());
        assertNotNull(def.getSerialize());
    }

}
