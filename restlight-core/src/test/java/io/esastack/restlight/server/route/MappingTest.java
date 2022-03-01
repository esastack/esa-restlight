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
package io.esastack.restlight.server.route;

import io.esastack.commons.net.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingTest {

    @Test
    void testEmptyMapping() {
        final Mapping mapping = Mapping.mapping();
        assertNull(mapping.name());
        assertNotNull(mapping.path());
        assertEquals(0, mapping.path().length);
        assertNotNull(mapping.method());
        assertEquals(0, mapping.method().length);
        assertNotNull(mapping.params());
        assertEquals(0, mapping.params().length);
        assertNotNull(mapping.headers());
        assertEquals(0, mapping.headers().length);
        assertNotNull(mapping.consumes());
        assertEquals(0, mapping.consumes().length);
        assertNotNull(mapping.produces());
        assertEquals(0, mapping.produces().length);
    }

    @Test
    void testMappingWithPath() {
        final Mapping mapping = Mapping.mapping("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
    }

    @Test
    void testMappingWithPathAndMethod() {
        final Mapping mapping = Mapping.mapping("/foo", HttpMethod.POST);
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping.method());
    }

    @Test
    void testMappingWithGet() {
        final Mapping mapping = Mapping.get();
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.method());
    }

    @Test
    void testMappingWithGetAndPath() {
        final Mapping mapping = Mapping.get("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.method());
    }

    @Test
    void testMappingWithPost() {
        final Mapping mapping = Mapping.post();
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping.method());
    }

    @Test
    void testMappingWithPostAndPath() {
        final Mapping mapping = Mapping.post("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping.method());
    }

    @Test
    void testMappingWithPut() {
        final Mapping mapping = Mapping.put();
        assertArrayEquals(new HttpMethod[]{HttpMethod.PUT}, mapping.method());
    }

    @Test
    void testMappingWithPutAndPath() {
        final Mapping mapping = Mapping.put("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PUT}, mapping.method());
    }

    @Test
    void testMappingWithDelete() {
        final Mapping mapping = Mapping.delete();
        assertArrayEquals(new HttpMethod[]{HttpMethod.DELETE}, mapping.method());
    }

    @Test
    void testMappingWithDeleteAndPath() {
        final Mapping mapping = Mapping.delete("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.DELETE}, mapping.method());
    }

    @Test
    void testMappingWithPatch() {
        final Mapping mapping = Mapping.patch();
        assertArrayEquals(new HttpMethod[]{HttpMethod.PATCH}, mapping.method());
    }

    @Test
    void testMappingWithPatchAndPath() {
        final Mapping mapping = Mapping.patch("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PATCH}, mapping.method());
    }

    @Test
    void testMappingWithHead() {
        final Mapping mapping = Mapping.head();
        assertArrayEquals(new HttpMethod[]{HttpMethod.HEAD}, mapping.method());
    }

    @Test
    void testMappingWithHeadAndPath() {
        final Mapping mapping = Mapping.head("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.HEAD}, mapping.method());
    }

    @Test
    void testMappingWithOptions() {
        final Mapping mapping = Mapping.options();
        assertArrayEquals(new HttpMethod[]{HttpMethod.OPTIONS}, mapping.method());
    }

    @Test
    void testMappingWithOptionsAndPath() {
        final Mapping mapping = Mapping.options("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.OPTIONS}, mapping.method());
    }

    @Test
    void testMappingWithTrace() {
        final Mapping mapping = Mapping.trace();
        assertArrayEquals(new HttpMethod[]{HttpMethod.TRACE}, mapping.method());
    }

    @Test
    void testMappingWithTraceAndPath() {
        final Mapping mapping = Mapping.trace("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.TRACE}, mapping.method());
    }

    @Test
    void testMappingWithConnect() {
        final Mapping mapping = Mapping.connect();
        assertArrayEquals(new HttpMethod[]{HttpMethod.CONNECT}, mapping.method());
    }

    @Test
    void testMappingWithConnectAndPath() {
        final Mapping mapping = Mapping.connect("/foo");
        assertArrayEquals(new String[]{"/foo"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.CONNECT}, mapping.method());
    }

}
