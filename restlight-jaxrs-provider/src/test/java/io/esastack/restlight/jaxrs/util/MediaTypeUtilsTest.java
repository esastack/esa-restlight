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
package io.esastack.restlight.jaxrs.util;

import io.esastack.commons.net.http.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MediaTypeUtilsTest {

    @Test
    void testConvert() {
        assertNull(MediaTypeUtils.convert((jakarta.ws.rs.core.MediaType) null));
        assertNull(MediaTypeUtils.convert((MediaType) null));

        assertEquals(MediaType.ALL, MediaTypeUtils.convert(jakarta.ws.rs.core.MediaType.WILDCARD_TYPE));
        assertEquals(jakarta.ws.rs.core.MediaType.WILDCARD_TYPE, MediaTypeUtils.convert(MediaType.ALL));

        assertEquals(MediaType.APPLICATION_JSON,
                MediaTypeUtils.convert(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE));
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE,
                MediaTypeUtils.convert(MediaType.APPLICATION_JSON));
    }
}

