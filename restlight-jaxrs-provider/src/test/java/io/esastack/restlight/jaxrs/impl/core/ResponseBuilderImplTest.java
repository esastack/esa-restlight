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
package io.esastack.restlight.jaxrs.impl.core;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseBuilderImplTest {

    @Test
    void testBasic() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();

        // illegal status
        assertThrows(IllegalArgumentException.class, () -> builder.status(0));
        assertThrows(IllegalArgumentException.class, () -> builder.status(600));

        builder.status(400);
        assertEquals(400, builder.status().getStatusCode());
        assertEquals(400, builder.build().getStatus());

        assertThrows(IllegalArgumentException.class, () -> builder.status(0, "abc"));
        assertThrows(IllegalArgumentException.class, () -> builder.status(600, "abc"));
        assertEquals(500, builder.status(500, "abc").build().getStatus());

        final Object obj1 = new Object();
        builder.entity(obj1);
        assertEquals(obj1, builder.entity());
        assertEquals(obj1, builder.build().getEntity());

        final Object obj2 = new Object();
        final Annotation[] annotations = new Annotation[] {};
        builder.entity(obj2, annotations);
        assertSame(obj2, builder.entity());
        assertSame(annotations, builder.annotations());
        assertSame(obj2, builder.build().getEntity());

        // test clone
        ResponseBuilderImpl cloned = (ResponseBuilderImpl) builder.status(301, "mn")
                .entity(obj1, annotations)
                .clone();
        assertEquals(301, cloned.status().getStatusCode());
        assertEquals("mn", cloned.reasonPhrase());
        assertSame(obj1, cloned.entity());
        assertSame(annotations, cloned.annotations());
    }

    @Test
    void testBuild() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.header("name", "value");
        builder.header("name1", "value1");
        final Response response = builder.build();
        assertEquals(204, response.getStatus());
        assertEquals(2, response.getHeaders().size());
        assertEquals("value", response.getHeaderString("name"));
        assertEquals("value1", response.getHeaderString("name1"));
        assertTrue(builder.headers().isEmpty());
    }

    @Test
    void testAllow() {
        final ResponseBuilderImpl builder1 = new ResponseBuilderImpl();
        builder1.allow("ABC", "MN", "ABC");
        final List<Object> methods1 = builder1.headers().get(HttpHeaders.ALLOW);
        assertEquals(2, methods1.size());
        assertTrue(methods1.contains("ABC"));
        assertTrue(methods1.contains("MN"));

        builder1.allow((String[]) null);
        assertNull(builder1.headers().get(HttpHeaders.ALLOW));

        final ResponseBuilderImpl builder2 = new ResponseBuilderImpl();
        final Set<String> allows = new HashSet<>();
        allows.add("XYZ");
        allows.add("AB");
        builder2.allow(allows);
        final List<Object> methods2 = builder2.headers().get(HttpHeaders.ALLOW);
        assertEquals(2, methods2.size());
        assertTrue(methods2.contains("XYZ"));
        assertTrue(methods2.contains("AB"));

        builder2.allow((String[]) null);
        assertNull(builder2.headers().get(HttpHeaders.ALLOW));
    }

    @Test
    void testCacheControl() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final CacheControl control = new CacheControl();
        builder.cacheControl(control);
        assertSame(control, builder.headers().getFirst(HttpHeaders.CACHE_CONTROL));

        builder.cacheControl(null);
        assertNull(builder.headers().getFirst(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    void testEncoding() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.encoding("ABC");
        assertEquals("ABC", builder.headers().getFirst(HttpHeaders.CONTENT_ENCODING));

        builder.encoding(null);
        assertNull(builder.headers().getFirst(HttpHeaders.CONTENT_ENCODING));
    }

    @Test
    void testHeader() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.header("name", "value1");
        builder.header("name", "value2");
        assertEquals(2, builder.headers().get("name").size());
        assertEquals("value1", builder.headers().get("name").get(0));
        assertEquals("value2", builder.headers().get("name").get(1));

        builder.header("name", null);
        assertNull(builder.headers().getFirst("name"));
    }

    @Test
    void testReplaceAll() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add("name", "value1");
        headers.add("name", "value2");
        headers.add("name1", "value1");
        headers.add("name1", "value2");

        builder.replaceAll(headers);
        assertEquals(2, builder.headers().get("name").size());
        assertEquals("value1", builder.headers().get("name").get(0));
        assertEquals("value2", builder.headers().get("name").get(1));
        assertEquals(2, builder.headers().get("name1").size());
        assertEquals("value1", builder.headers().get("name1").get(0));
        assertEquals("value2", builder.headers().get("name1").get(1));

        builder.replaceAll(null);
        assertTrue(builder.headers().isEmpty());
    }

    @Test
    void testLanguage() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.language("ABC");
        assertEquals("ABC", builder.headers().getFirst(HttpHeaders.CONTENT_LANGUAGE));
        builder.language((String) null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_LANGUAGE));

        final Locale locale = Locale.US;
        builder.language(locale);
        assertSame(locale, builder.headers().getFirst(HttpHeaders.CONTENT_LANGUAGE));
        builder.language((Locale) null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_LANGUAGE));
    }

    @Test
    void testType() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.type("ABC");
        assertEquals("ABC", builder.headers().getFirst(HttpHeaders.CONTENT_TYPE));
        builder.type((String) null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_TYPE));

        final MediaType type = MediaType.WILDCARD_TYPE;
        builder.type(type);
        assertSame(type, builder.headers().getFirst(HttpHeaders.CONTENT_TYPE));
        builder.type((MediaType) null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void testVariant() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final Variant variant = new Variant(MediaType.WILDCARD_TYPE, Locale.US, "MN");
        builder.variant(variant);
        assertSame(MediaType.WILDCARD_TYPE, builder.headers().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(Locale.US, builder.headers().getFirst(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals("MN", builder.headers().getFirst(HttpHeaders.CONTENT_ENCODING));

        builder.variant(null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_TYPE));
        assertNull(builder.headers().get(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(builder.headers().get(HttpHeaders.CONTENT_ENCODING));
    }

    @Test
    void testContentLocation() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final URI uri = URI.create("/abc/def");
        builder.contentLocation(uri);
        assertSame(uri, builder.headers().getFirst(HttpHeaders.CONTENT_LOCATION));

        builder.contentLocation(null);
        assertNull(builder.headers().get(HttpHeaders.CONTENT_LOCATION));
    }

    @Test
    void testCookie() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final NewCookie cookie1 = new NewCookie("name1", "value1");
        final NewCookie cookie2 = new NewCookie("name2", "value2");
        builder.cookie(cookie1, cookie2);
        assertEquals(2, builder.headers().get(HttpHeaders.SET_COOKIE).size());
        assertSame(cookie1, builder.headers().get(HttpHeaders.SET_COOKIE).get(0));
        assertSame(cookie2, builder.headers().get(HttpHeaders.SET_COOKIE).get(1));

        builder.cookie(null);
        assertNull(builder.headers().get(HttpHeaders.SET_COOKIE));
    }

    @Test
    void testExpires() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final Date date = new Date();
        builder.expires(date);
        assertSame(date, builder.headers().getFirst(HttpHeaders.EXPIRES));

        builder.expires(null);
        assertNull(builder.headers().get(HttpHeaders.EXPIRES));
    }

    @Test
    void testLastModified() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final Date date = new Date();
        builder.lastModified(date);
        assertSame(date, builder.headers().getFirst(HttpHeaders.LAST_MODIFIED));

        builder.lastModified(null);
        assertNull(builder.headers().get(HttpHeaders.LAST_MODIFIED));
    }

    @Test
    void testLocation() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final URI uri = URI.create("/abc/def");
        builder.location(uri);
        assertSame(uri, builder.headers().getFirst(HttpHeaders.LOCATION));

        builder.location(null);
        assertNull(builder.headers().get(HttpHeaders.LOCATION));
    }

    @Test
    void testTag() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final EntityTag tag = new EntityTag("mn");
        builder.tag(tag);
        assertSame(tag, builder.headers().getFirst(HttpHeaders.ETAG));
        builder.tag((EntityTag) null);
        assertNull(builder.headers().get(HttpHeaders.ETAG));

        builder.tag("xyz");
        assertEquals(new EntityTag("xyz"), builder.headers().getFirst(HttpHeaders.ETAG));
        builder.tag((String) null);
        assertNull(builder.headers().get(HttpHeaders.ETAG));
    }

    @Test
    void testVariants() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final Variant variant1 = new Variant(MediaType.WILDCARD_TYPE, "en", "utf-8");
        final Variant variant2 = new Variant(MediaType.APPLICATION_JSON_TYPE, "cn", "gbk");
        builder.variants(variant1, variant2);
        assertEquals(2, builder.headers().get(HttpHeaders.VARY).size());
        assertSame(variant1, builder.headers().get(HttpHeaders.VARY).get(0));
        assertSame(variant2, builder.headers().get(HttpHeaders.VARY).get(1));
        builder.variants((Variant[]) null);
        assertNull(builder.headers().get(HttpHeaders.VARY));

        final List<Variant> variants = new LinkedList<>();
        variants.add(variant1);
        variants.add(variant2);
        builder.variants(variants);
        assertEquals(2, builder.headers().get(HttpHeaders.VARY).size());
        assertSame(variant1, builder.headers().get(HttpHeaders.VARY).get(0));
        assertSame(variant2, builder.headers().get(HttpHeaders.VARY).get(1));
        builder.variants((Variant[]) null);
        assertNull(builder.headers().get(HttpHeaders.VARY));
    }

    @Test
    void testLinks() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final Link link1 = new LinkImpl(URI.create("/abc"), new HashMap<>());
        final Link link2 = new LinkImpl(URI.create("/def"), new HashMap<>());
        builder.links(link1, link2);
        assertEquals(2, builder.headers().get(HttpHeaders.LINK).size());
        assertSame(link1, builder.headers().get(HttpHeaders.LINK).get(0));
        assertSame(link2, builder.headers().get(HttpHeaders.LINK).get(1));
    }

    @Test
    void testLink() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.link(URI.create("/xyz"), "def");
        List<Object> links1 = builder.headers().get(HttpHeaders.LINK);
        assertEquals(1, links1.size());
        assertEquals("/xyz", ((Link) links1.get(0)).getUri().toString());
        assertEquals("def", ((Link) links1.get(0)).getParams().get("rel"));

        builder.link("/xyz0", "def0");
        List<Object> links2 = builder.headers().get(HttpHeaders.LINK);
        assertEquals(2, links2.size());
        assertEquals("/xyz", ((Link) links2.get(0)).getUri().toString());
        assertEquals("def", ((Link) links2.get(0)).getParams().get("rel"));
        assertEquals("/xyz0", ((Link) links2.get(1)).getUri().toString());
        assertEquals("def0", ((Link) links2.get(1)).getParams().get("rel"));
    }

}

