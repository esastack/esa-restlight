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
package esa.restlight.ext.filter.cors;

import esa.restlight.core.method.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

class CorsOptionsTest {

    @Test
    void testConfigure() {
        final CorsOptions options = CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(new LinkedHashSet<>(Arrays.asList("a", "b")))
                .exposeHeaders(new LinkedHashSet<>(Arrays.asList("c", "d")))
                .allowCredentials(false)
                .allowMethods(new LinkedHashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.DELETE)))
                .allowHeaders(new LinkedHashSet<>(Arrays.asList("e", "f")))
                .maxAge(1L)
                .configured();

        assertFalse(options.isAnyOrigin());
        assertArrayEquals(new String[]{"a", "b"}, options.getOrigins().toArray());
        assertArrayEquals(new String[]{"c", "d"}, options.getExposeHeaders().toArray());
        assertFalse(options.isAllowCredentials());
        System.out.println(Arrays.toString(options.getAllowMethods().toArray()));
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET, HttpMethod.DELETE}, options.getAllowMethods().toArray());
        assertArrayEquals(new String[]{"e", "f"}, options.getAllowHeaders().toArray());
        assertEquals(1L, options.getMaxAge());
    }

    @Test
    void testDefaultOpts() {
        final CorsOptions options = CorsOptionsConfigure.defaultOpts();
        final CorsOptions def = new CorsOptions();

        assertEquals(def.isAnyOrigin(), options.isAnyOrigin());
        assertEquals(def.getOrigins(), options.getOrigins());
        assertEquals(def.getExposeHeaders(), options.getExposeHeaders());
        assertEquals(def.isAllowCredentials(), options.isAllowCredentials());
        assertEquals(def.getAllowMethods(), options.getAllowMethods());
        assertEquals(def.getAllowHeaders(), options.getAllowHeaders());
        assertEquals(def.getMaxAge(), options.getMaxAge());
    }

}
