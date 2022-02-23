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
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        response.setStatus(302);
        assertSame(Response.Status.FOUND, response.getStatusInfo());
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
        assertNull(response1.getLink(null));

        final ResponseBuilderImpl builder2 = new ResponseBuilderImpl();
        final ResponseImpl response2 = new ResponseImpl(builder2);

        final Link link = new LinkImpl(URI.create("/abc"), Collections.singletonMap(REL, "def"));
        builder2.header(HttpHeaders.LINK, link);
        builder2.header(HttpHeaders.LINK, "<abc0>; rel=\"preload\"");
        builder2.header(HttpHeaders.LINK, new Object());
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
        assertTrue(response.getStringHeaders() instanceof ResponseImpl.StringHeadersMultivaluedMap);

        builder.header("name", "value1");
        builder.header("name", "value2");
        builder.header("name", "value3");

        assertEquals("value1,value2,value3", response.getHeaderString("name"));
    }

    @Test
    void testClose() {
        final ResponseBuilderImpl builder = new ResponseBuilderImpl();
        final ResponseImpl response = new ResponseImpl(builder);
        response.close();
        assertThrows(IllegalStateException.class, () -> response.setEntity(new Object()));
    }

    @Test
    void testStringHeadersMultivaluedMap() {
        assertThrows(NullPointerException.class,
                () -> new ResponseImpl.StringHeadersMultivaluedMap(null));

        final MultivaluedMap<String, Object> underlying = new MultivaluedHashMap<>();
        final ResponseImpl.StringHeadersMultivaluedMap proxied =
                new ResponseImpl.StringHeadersMultivaluedMap(underlying);
        assertEquals(underlying.size(), proxied.size());
        assertEquals(underlying.isEmpty(), proxied.isEmpty());
        assertEquals(underlying.keySet(), proxied.keySet());
        assertTrue(underlying.values().isEmpty() && proxied.values().isEmpty());
        assertTrue(isValuesEquals(underlying, proxied));

        proxied.putSingle("name", "value");
        assertEquals(1, underlying.size());
        assertEquals("value", underlying.getFirst("name"));

        proxied.add("name1", "value1");
        assertEquals(2, underlying.size());
        assertEquals("value1", underlying.getFirst("name1"));

        proxied.addAll("name2", "value21", "value22");
        assertEquals(3, underlying.size());
        List<Object> value2 = underlying.get("name2");
        assertEquals(2, value2.size());
        assertEquals("value21", value2.get(0));
        assertEquals("value22", value2.get(1));

        proxied.addAll("name3", Collections.singletonList("value31"));
        List<Object> value3 = underlying.get("name3");
        assertEquals(1, value3.size());
        assertEquals("value31", value3.get(0));

        proxied.addFirst("name4", "value4");
        List<Object> value4 = underlying.get("name4");
        assertEquals(1, value4.size());
        assertEquals("value4", value4.get(0));

        assertEquals(underlying.size(), proxied.size());
        assertEquals(underlying.isEmpty(), proxied.isEmpty());
        assertEquals(underlying.keySet(), proxied.keySet());
        assertTrue(isValuesEquals(underlying, proxied));
        assertTrue(!underlying.values().isEmpty() && !proxied.values().isEmpty());
        assertEquals(underlying.containsKey("name"), proxied.containsKey("name"));
        assertEquals(underlying.containsKey("name1"), proxied.containsKey("name1"));
        assertEquals(underlying.containsKey("name2"), proxied.containsKey("name2"));
        assertEquals(underlying.containsKey("name3"), proxied.containsKey("name3"));
        assertEquals(underlying.containsKey("name4"), proxied.containsKey("name4"));

        assertEquals(underlying.containsValue("value"), proxied.containsValue("value"));
        assertEquals(underlying.containsValue("value1"), proxied.containsValue("value1"));
        assertEquals(underlying.containsValue("value21"), proxied.containsValue("value21"));
        assertEquals(underlying.containsValue("value22"), proxied.containsValue("value22"));
        assertEquals(underlying.containsValue("value31"), proxied.containsValue("value31"));
        assertEquals(underlying.containsValue("value4"), proxied.containsValue("value4"));

        underlying.clear();
        assertEquals(0, proxied.size());
        assertTrue(proxied.isEmpty());

        // test getFirst
        final Map<String, String> params = new HashMap<>();
        params.put("name1", "value1");
        params.put("name2", "value2");
        final Link link = new LinkImpl(URI.create("/abc/def"), params);
        underlying.add("link", link);
        assertEquals(link.toString(), proxied.getFirst("link"));

        // test equalsIgnoreValueOrder
        final MultivaluedMap<String, String> otherMap = new MultivaluedHashMap<>();
        otherMap.add("link", link.toString());
        assertFalse(proxied.equalsIgnoreValueOrder(otherMap));
        underlying.clear();

        // test get
        final Link link1 = new LinkImpl(URI.create("/def/mn"), params);
        underlying.add("name", link);
        underlying.add("name", link1);
        final List<String> values1 = proxied.get("name");
        assertEquals(2, values1.size());
        assertEquals(link.toString(), values1.get(0));
        assertEquals(link1.toString(), values1.get(1));
        assertNull(proxied.get("name1"));

        // test put
        final List<String> newValues = new ArrayList<>();
        newValues.add("value1");
        newValues.add("value2");
        List<String> previous = proxied.put("name", newValues);
        assertNotNull(previous);
        assertEquals(2, previous.size());
        assertEquals(link.toString(), previous.get(0));
        assertEquals(link1.toString(), previous.get(1));

        List<String> previous1 = proxied.put("name", null);
        assertNotNull(previous1);
        assertEquals(2, previous1.size());
        assertEquals("value1", previous1.get(0));
        assertEquals("value2", previous1.get(1));

        // test remove
        underlying.clear();
        underlying.add("name", link);
        underlying.add("name", link1);
        List<String> previous2 = proxied.remove("name");
        assertNotNull(previous2);
        assertEquals(link.toString(), previous2.get(0));
        assertEquals(link1.toString(), previous2.get(1));
        assertNull(proxied.remove("name2"));
        assertTrue(underlying.isEmpty());

        // test putAll
        Map<String, List<String>> m = new HashMap<>();
        m.put("name1", null);
        final List<String> values = new ArrayList<>(2);
        values.add("value1");
        values.add("value2");
        m.put("name", values);
        proxied.putAll(m);
        assertEquals(2, underlying.keySet().size());
        assertNull(underlying.get("name1"));
        assertEquals(2, underlying.get("name").size());
        assertEquals("value1", underlying.get("name").get(0));
        assertEquals("value2", underlying.get("name").get(1));

        // test values
        assertEquals(2, proxied.values().size());
        final List<String> values00 = new ArrayList<>(proxied.values()).get(0);
        assertEquals(2, values00.size());
        assertEquals("value1", values00.get(0));
        assertEquals("value2", values00.get(1));

        // test entrySet
        Set<Map.Entry<String, List<String>>> entries = proxied.entrySet();
        assertEquals(2, entries.size());
        List<Map.Entry<String, List<String>>> entriesList = new ArrayList<>(entries);
        Map.Entry<String, List<String>> entryItem = entriesList.get(0);
        assertEquals("name", entryItem.getKey());
        assertEquals(2, entryItem.getValue().size());
        assertEquals("value1", entryItem.getValue().get(0));
        assertEquals("value2", entryItem.getValue().get(1));
    }

    private boolean isValuesEquals(MultivaluedMap<String, Object> underlying,
                                   ResponseImpl.StringHeadersMultivaluedMap proxied) {
        final Collection<List<Object>> values1 = underlying.values();
        final Collection<List<String>> values2 = proxied.values();
        if (values1 == null) {
            return values2 == null;
        } else if (values2 == null) {
            return false;
        }

        if (values1.size() != values2.size()) {
            return false;
        }
        final Iterator<List<Object>> i1 = values1.iterator();
        final Iterator<List<String>> i2 = values2.iterator();
        while (i1.hasNext()) {
            List<Object> l1 = i1.next();
            if (!i2.hasNext()) {
                return false;
            }
            List<String> l2 = i2.next();
            if (!Arrays.equals(l1.toArray(), l2.toArray())) {
                return false;
            }
        }
        return true;
    }

}

