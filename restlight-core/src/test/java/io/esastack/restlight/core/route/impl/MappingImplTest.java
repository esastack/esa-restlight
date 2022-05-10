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
package io.esastack.restlight.core.route.impl;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.route.Mapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingImplTest {

    @Test
    void testArgument() {
        final MappingImpl mapping = new MappingImpl("name",
                new String[]{"/path"},
                new HttpMethod[]{HttpMethod.GET},
                new String[]{"a=1"},
                new String[]{"b=1"},
                new String[]{"a/1"},
                new String[]{"b/1"});
        assertEquals("name", mapping.name());
        assertArrayEquals(new String[]{"/path"}, mapping.path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.method());
        assertArrayEquals(new String[]{"a=1"}, mapping.params());
        assertArrayEquals(new String[]{"b=1"}, mapping.headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.produces());
    }

    @Test
    void testEmpty() {
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
    void testValue() {
        final Mapping mapping = Mapping.mapping()
                .name("name")
                .path("/path")
                .method("get")
                .params("p=1")
                .headers("h=1")
                .consumes("text/plain")
                .produces("text/html");
        assertEquals("name", mapping.name());
        assertNotNull(mapping.path());
        assertEquals(1, mapping.path().length);
        assertEquals("/path", mapping.path()[0]);

        assertNotNull(mapping.method());
        assertEquals(HttpMethod.GET, mapping.method()[0]);
        assertNotNull(mapping.params());
        assertEquals("p=1", mapping.params()[0]);
        assertNotNull(mapping.headers());
        assertEquals("h=1", mapping.headers()[0]);
        assertNotNull(mapping.consumes());
        assertEquals("text/plain", mapping.consumes()[0]);
        assertNotNull(mapping.produces());
        assertEquals("text/html", mapping.produces()[0]);
    }

    @Test
    void testIllegalConsumes() {
        assertThrows(IllegalArgumentException.class, () -> Mapping.mapping()
                .consumes("abc/?"));
    }

    @Test
    void testIllegalProduces() {
        assertThrows(IllegalArgumentException.class, () -> Mapping.mapping()
                .produces("abc/?"));
    }

    @Test
    void testPath() {
        final Mapping mapping = Mapping.mapping().path("/a").path("b");
        assertEquals(1, mapping.path().length);
        assertEquals("/b", mapping.path()[0]);

        final Mapping mapping1 = Mapping.mapping().path("/a", "/b").path("b");
        assertEquals(1, mapping1.path().length);
        assertEquals("/b", mapping1.path()[0]);

        final Mapping mapping2 = Mapping.mapping().path("/a", "/b").path("c", "d");
        assertEquals(2, mapping2.path().length);
        assertEquals("/c", mapping2.path()[0]);
        assertEquals("/d", mapping2.path()[1]);
    }

    @Test
    void testMethod() {
        final Mapping mapping = Mapping.mapping()
                .method("get")
                .method(HttpMethod.POST);
        assertEquals(1, mapping.method().length);
        assertEquals(HttpMethod.POST, mapping.method()[0]);

        final Mapping mapping1 = Mapping.mapping()
                .method(HttpMethod.GET, HttpMethod.POST)
                .method("post");
        assertEquals(1, mapping1.method().length);
        assertEquals(HttpMethod.POST, mapping1.method()[0]);

        final Mapping mapping2 = Mapping.mapping()
                .method(HttpMethod.GET, HttpMethod.POST)
                .method(HttpMethod.DELETE, HttpMethod.PUT);
        assertEquals(2, mapping2.method().length);
        assertEquals(HttpMethod.DELETE, mapping2.method()[0]);
        assertEquals(HttpMethod.PUT, mapping2.method()[1]);
    }

    @Test
    void testParams() {
        final Mapping mapping = Mapping.mapping()
                .params("a=1");

        assertEquals(1, mapping.params().length);
        assertEquals("a=1", mapping.params()[0]);

        final Mapping mapping1 = Mapping.mapping()
                .hasParam("a", "1");
        assertEquals(1, mapping1.params().length);
        assertEquals("a=1", mapping1.params()[0]);

        final Mapping mapping2 = Mapping.mapping()
                .hasParam("a");
        assertEquals(1, mapping2.params().length);
        assertEquals("a", mapping2.params()[0]);

        final Mapping mapping3 = Mapping.mapping()
                .noneParam("a", "1");
        assertEquals(1, mapping3.params().length);
        assertEquals("a!=1", mapping3.params()[0]);

        final Mapping mapping4 = Mapping.mapping()
                .noneParam("a");
        assertEquals(1, mapping4.params().length);
        assertEquals("!a", mapping4.params()[0]);

        final Mapping mapping5 = Mapping.mapping()
                .hasParam("a")
                .hasParam("b", "1")
                .noneParam("c")
                .noneParam("d", "1")
                .params("e=1", "f!=2");
        assertEquals(6, mapping5.params().length);
        assertEquals("a", mapping5.params()[0]);
        assertEquals("b=1", mapping5.params()[1]);
        assertEquals("!c", mapping5.params()[2]);
        assertEquals("d!=1", mapping5.params()[3]);
        assertEquals("e=1", mapping5.params()[4]);
        assertEquals("f!=2", mapping5.params()[5]);
    }

    @Test
    void testHeaders() {
        final Mapping mapping = Mapping.mapping()
                .headers("a=1");

        assertEquals(1, mapping.headers().length);
        assertEquals("a=1", mapping.headers()[0]);

        final Mapping mapping1 = Mapping.mapping()
                .hasHeader("a", "1");
        assertEquals(1, mapping1.headers().length);
        assertEquals("a=1", mapping1.headers()[0]);

        final Mapping mapping2 = Mapping.mapping()
                .hasHeader("a");
        assertEquals(1, mapping2.headers().length);
        assertEquals("a", mapping2.headers()[0]);

        final Mapping mapping3 = Mapping.mapping()
                .noneHeader("a", "1");
        assertEquals(1, mapping3.headers().length);
        assertEquals("a!=1", mapping3.headers()[0]);

        final Mapping mapping4 = Mapping.mapping()
                .noneHeader("a");
        assertEquals(1, mapping4.headers().length);
        assertEquals("!a", mapping4.headers()[0]);

        final Mapping mapping5 = Mapping.mapping()
                .hasHeader("a")
                .hasHeader("b", "1")
                .noneHeader("c")
                .noneHeader("d", "1")
                .headers("e=1", "f!=2");
        assertEquals(6, mapping5.headers().length);
        assertEquals("a", mapping5.headers()[0]);
        assertEquals("b=1", mapping5.headers()[1]);
        assertEquals("!c", mapping5.headers()[2]);
        assertEquals("d!=1", mapping5.headers()[3]);
        assertEquals("e=1", mapping5.headers()[4]);
        assertEquals("f!=2", mapping5.headers()[5]);
    }

    @Test
    void testConsumes() {
        final Mapping mapping = Mapping.mapping()
                .consumes("application/1");

        assertEquals(1, mapping.consumes().length);
        assertEquals("application/1", mapping.consumes()[0]);

        final Mapping mapping1 = Mapping.mapping()
                .consumes("application/1")
                .consumes(MediaType.TEXT_PLAIN);

        assertEquals(2, mapping1.consumes().length);
        assertEquals("application/1", mapping1.consumes()[0]);
        assertEquals(MediaType.TEXT_PLAIN.value(), mapping1.consumes()[1]);
    }

    @Test
    void testProduces() {
        final Mapping mapping = Mapping.mapping()
                .produces("application/1");

        assertEquals(1, mapping.produces().length);
        assertEquals("application/1", mapping.produces()[0]);

        final Mapping mapping1 = Mapping.mapping()
                .produces("application/1")
                .produces(MediaType.TEXT_PLAIN);

        assertEquals(2, mapping1.produces().length);
        assertEquals("application/1", mapping1.produces()[0]);
        assertEquals(MediaType.TEXT_PLAIN.value(), mapping1.produces()[1]);
    }

    @Test
    void testComposite() {
        final Mapping mapping = Mapping.mapping().get("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping.path().length);
        assertEquals("/path", mapping.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.GET, mapping.method()[0]);


        final Mapping mapping1 = Mapping.mapping().post("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping1.path().length);
        assertEquals("/path", mapping1.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.POST, mapping1.method()[0]);

        final Mapping mapping2 = Mapping.mapping().delete("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping2.path().length);
        assertEquals("/path", mapping2.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.DELETE, mapping2.method()[0]);

        final Mapping mapping3 = Mapping.mapping().put("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping3.path().length);
        assertEquals("/path", mapping3.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.PUT, mapping3.method()[0]);

        final Mapping mapping4 = Mapping.mapping().patch("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping4.path().length);
        assertEquals("/path", mapping4.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.PATCH, mapping4.method()[0]);

        final Mapping mapping5 = Mapping.mapping().options("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping5.path().length);
        assertEquals("/path", mapping5.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.OPTIONS, mapping5.method()[0]);

        final Mapping mapping6 = Mapping.mapping().trace("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping6.path().length);
        assertEquals("/path", mapping6.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.TRACE, mapping6.method()[0]);

        final Mapping mapping7 = Mapping.mapping().connect("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping7.path().length);
        assertEquals("/path", mapping7.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.CONNECT, mapping7.method()[0]);

        final Mapping mapping8 = Mapping.mapping().head("path");
        assertNotNull(mapping.path());
        assertEquals(1, mapping8.path().length);
        assertEquals("/path", mapping8.path()[0]);
        assertNotNull(mapping.method());
        assertEquals(HttpMethod.HEAD, mapping8.method()[0]);
    }

    @Test
    void testToString() {
        assertEquals("", Mapping.mapping().toString());
        assertEquals("name=foo", Mapping.mapping().name("foo").toString());
        assertEquals("method=[GET]", Mapping.mapping().method("GET").toString());
        assertEquals("path=[/foo]", Mapping.mapping().path("/foo").toString());
        assertEquals("params=[a=1]", Mapping.mapping().hasParam("a", "1").toString());
        assertEquals("headers=[a=1]", Mapping.mapping().hasHeader("a", "1").toString());
        assertEquals("consumes=[text/plain]", Mapping.mapping().consumes(MediaType.TEXT_PLAIN).toString());
        assertEquals("produces=[text/plain]", Mapping.mapping().produces(MediaType.TEXT_PLAIN).toString());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(Mapping.mapping(), Mapping.mapping());
        assertEquals(Mapping.get("/foo"), Mapping.get("/foo"));
        final Mapping m1 = Mapping.get("/foo")
                .name("foo")
                .hasParam("a", "1")
                .hasHeader("a", "1")
                .consumes(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)
                .produces(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON);
        final Mapping m2 = Mapping.get("/foo")
                .name("foo")
                .hasParam("a", "1")
                .hasHeader("a", "1")
                .consumes(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)
                .produces(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

}
