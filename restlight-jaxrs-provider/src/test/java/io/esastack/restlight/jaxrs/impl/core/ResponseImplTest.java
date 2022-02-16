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

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.restlight.server.util.DateUtils;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static jakarta.ws.rs.core.Link.REL;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ResponseImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new ResponseImpl(null));
        assertDoesNotThrow(() -> new ResponseImpl(mock(ResponseBuilderImpl.class)));
    }

    @Test
    void testOperateEntity() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        final Object entity1 = new Object();
        assertFalse(response.hasEntity());
        response.setEntity(entity1);
        assertSame(entity1, builder.entity());
        assertSame(entity1, response.getEntity());
        assertTrue(response.hasEntity());
        assertFalse(response.bufferEntity());

        final Annotation[] annotations = new Annotation[]{};
        final MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        final Object entity2 = new Object();
        response.setEntity(entity2, annotations, mediaType);
        assertSame(entity2, builder.entity());
        assertSame(annotations, builder.annotations());
        assertSame(annotations, response.getAnnotations());
        assertSame(mediaType, builder.headers().getFirst(HttpHeaderNames.CONTENT_TYPE));
        assertSame(entity2, response.getEntity());

        assertThrows(IllegalStateException.class, () -> response.readEntity(Object.class));
        assertThrows(IllegalStateException.class, () -> response.readEntity(GenericType.forInstance(entity1)));
        assertThrows(IllegalStateException.class, () -> response.readEntity(Object.class, new Annotation[0]));
        assertThrows(IllegalStateException.class, () -> response.readEntity(GenericType.forInstance(entity1),
                new Annotation[0]));
    }

    @Test
    void testOperateStatus() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        builder.status(301);
        assertEquals(301, builder.status().getStatusCode());
        assertEquals(301, response.getStatus());

        response.setStatus(Response.Status.ACCEPTED);
        assertSame(Response.Status.ACCEPTED, response.getStatusInfo());
    }

    @Test
    void testGetMediaType() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        assertNull(response.getMediaType());

        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE);
        assertSame(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        builder.headers().clear();
        builder.header(HttpHeaders.CONTENT_TYPE, io.esastack.commons.net.http.MediaType.TEXT_HTML);
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());

        builder.headers().clear();
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD);
        assertEquals(MediaType.WILDCARD_TYPE, response.getMediaType());
    }

    @Test
    void testGetLanguage() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        assertNull(response.getLanguage());

        builder.header(HttpHeaders.CONTENT_LANGUAGE, Locale.US);
        assertSame(Locale.US, response.getLanguage());

        builder.headers().clear();
        builder.header(HttpHeaders.CONTENT_LANGUAGE, Locale.FRANCE.toString());
        assertNotNull(response.getLanguage());
    }

    @Test
    void testGetLength() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        assertEquals(-1, response.getLength());

        builder.header(HttpHeaders.CONTENT_LENGTH, "100");
        assertEquals(100, response.getLength());

        builder.headers().clear();
        builder.header(HttpHeaders.CONTENT_LENGTH, "x00");
        assertEquals(-1, response.getLength());
    }

    @Test
    void testGetAllowedMethods() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertTrue(response.getAllowedMethods().isEmpty());

        builder.header(HttpHeaders.ALLOW, "get");
        builder.header(HttpHeaders.ALLOW, "post");
        builder.header(HttpHeaders.ALLOW, "put");

        Set<String> methods = response.getAllowedMethods();
        assertEquals(3, methods.size());
        assertTrue(methods.contains("get"));
        assertTrue(methods.contains("post"));
        assertTrue(methods.contains("put"));
    }

    @Test
    void testGetCookies() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertTrue(response.getCookies().isEmpty());

        builder.header(HttpHeaders.SET_COOKIE, "name1=value1");
        builder.header(HttpHeaders.SET_COOKIE, "name2=value2");
        builder.header(HttpHeaders.SET_COOKIE, "name3=value3");

        Map<String, NewCookie> cookies = response.getCookies();
        assertEquals(3, cookies.size());
        assertEquals("value1", cookies.get("name1").getValue());
        assertEquals("value2", cookies.get("name2").getValue());
        assertEquals("value3", cookies.get("name3").getValue());
    }

    @Test
    void testGetEntityTag() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertTrue(response.getCookies().isEmpty());

        builder.header(HttpHeaders.ETAG, "W/\"abc\"");
        assertTrue(response.getEntityTag().isWeak());
        assertEquals("abc", response.getEntityTag().getValue());
    }

    @Test
    void testGetDate() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertNull(response.getDate());

        final Date current = new Date();
        builder.header(HttpHeaders.DATE, current);
        assertSame(current, response.getDate());

        builder.headers().clear();
        final Date next = new Date();
        builder.header(HttpHeaders.DATE, DateUtils.formatByCache(next.getTime()));
        assertNotNull(response.getDate());
    }

    @Test
    void testGetLastModified() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertNull(response.getLastModified());

        final Date current = new Date();
        builder.header(HttpHeaders.LAST_MODIFIED, current);
        assertSame(current, response.getLastModified());

        builder.headers().clear();
        final Date next = new Date();
        builder.header(HttpHeaders.LAST_MODIFIED, DateUtils.formatByCache(next.getTime()));
        assertNotNull(response.getLastModified());
    }

    @Test
    void testGetLocation() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);

        assertNull(response.getLocation());

        final URI current = URI.create("/abc");
        builder.header(HttpHeaders.LOCATION, current);
        assertSame(current, response.getLocation());

        builder.headers().clear();
        builder.header(HttpHeaders.LOCATION, "/def");
        assertEquals("/def", response.getLocation().toString());
    }

    @Test
    void testOperateLinks() {
        final ResponseBuilderImpl builder1 = new ResponseBuilderImpl();
        final ResponseImpl response1 = new ResponseImpl(builder1);
        assertTrue(response1.getLinks().isEmpty());
        assertFalse(response1.hasLink("/abc"));
        assertNull(response1.getLink("/abc"));
        assertNull(response1.getLinkBuilder("/abc"));

        final ResponseBuilderImpl builder2 = new ResponseBuilderImpl();
        final ResponseImpl response2 = new ResponseImpl(builder2);

        final Link link = new LinkImpl(URI.create("/abc"), Collections.singletonMap(REL, "def"));
        builder2.header(HttpHeaders.LINK, link);
        builder2.header(HttpHeaders.LINK, "<abc0>; rel=\"preload\"");
        response2.getLinks();
        assertEquals(2, response2.getLinks().size());
        assertTrue(response2.hasLink("def"));
        assertTrue(response2.hasLink("preload"));
        assertNotNull(response2.getLink("def"));
        assertNotNull(response2.getLink("preload"));
        assertNotNull(response2.getLinkBuilder("def"));
        assertNotNull(response2.getLinkBuilder("preload"));
    }

    @Test
    void testOperateHeaders() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        assertSame(builder.headers(), response.getMetadata());
        assertTrue(response.getStringHeaders() instanceof DelegatingMultivaluedMap);

        builder.header("name", "value1");
        builder.header("name", "value2");
        builder.header("name", "value3");

        assertEquals("value1,value2,value3", response.getHeaderString("name"));
    }

}

