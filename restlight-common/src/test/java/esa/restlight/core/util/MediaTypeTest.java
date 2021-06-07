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
package esa.restlight.core.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaTypeTest {

    @Test
    void testOf() {
        final MediaType type0 = MediaType.of("application");
        assertEquals("application", type0.type());
        assertEquals("*", type0.subtype());
        assertTrue(type0.parameters().isEmpty());

        final MediaType type1 = MediaType.of("application", "jsonp");
        assertEquals("application", type1.type());
        assertEquals("jsonp", type1.subtype());
        assertTrue(type1.parameters().isEmpty());

        final MediaType type2 = MediaType.of("text", "plain", StandardCharsets.UTF_8);
        assertEquals("text", type2.type());
        assertEquals("plain", type2.subtype());
        assertEquals(1, type2.parameters().size());
        assertEquals(StandardCharsets.UTF_8, type2.charset());

        final Map<String, String> params = new LinkedHashMap<>(1);
        params.put("a", "b");
        params.put("x", "y");
        final MediaType type3 = MediaType.of("*", "*", params);
        assertEquals("*", type3.type());
        assertEquals("*", type3.subtype());
        assertEquals(2, type3.parameters().size());
        assertEquals("b", type3.getParameter("a"));
        assertEquals("y", type3.getParameter("x"));
    }

    @Test
    void testValueOf() {
        final MediaType type0 = MediaType.valueOf("application/json;charset=utf-8");
        assertEquals(MediaType.APPLICATION_JSON_UTF8, type0);
        assertThrows(IllegalArgumentException.class, () -> MediaType.valueOf("xx,"));

        final List<MediaType> types1 = MediaType.valuesOf("application/json, text/plain;a=b;c=d");
        assertEquals(MediaType.APPLICATION_JSON, types1.get(0));
        assertEquals("text", types1.get(1).type());
        assertEquals("plain", types1.get(1).subtype());
        assertEquals("b", types1.get(1).getParameter("a"));
        assertEquals("d", types1.get(1).getParameter("c"));
    }

    @Test
    void testValuesOf() {
        final List<MediaType> target = new LinkedList<>();

        MediaType.valuesOf("", target);
        assertTrue(target.isEmpty());

        MediaType.valuesOf("application/json, text/plain;a=b;c=d", target);
        assertEquals(MediaType.APPLICATION_JSON, target.get(0));
        assertEquals("text", target.get(1).type());
        assertEquals("plain", target.get(1).subtype());
        assertEquals("b", target.get(1).getParameter("a"));
        assertEquals("d", target.get(1).getParameter("c"));
    }

    @Test
    void testParseMediaType() {
        assertThrows(IllegalArgumentException.class, () -> MediaType.parseMediaType(""));

        final MediaType type0 = MediaType.parseMediaType("application/octet-stream");
        assertEquals("application", type0.type());
        assertEquals("octet-stream", type0.subtype());
        assertTrue(type0.parameters().isEmpty());
    }

    @Test
    void testParseTypes() {
        assertTrue(MediaType.parseMediaTypes("").isEmpty());

        final List<MediaType> types = MediaType.parseMediaTypes("*/*, text/plain");
        assertEquals(2, types.size());
        assertEquals(MediaType.ALL, types.get(0));
        assertEquals(MediaType.TEXT_PLAIN, types.get(1));
    }

    @Test
    void testSortBySpecificityAndQuality() {
        final List<MediaType> types = new LinkedList<>();
        types.add(MediaType.ALL);
        types.add(MediaType.of("text"));
        types.add(MediaType.TEXT_HTML);

        MediaType.sortBySpecificityAndQuality(types);
        assertEquals(MediaType.TEXT_HTML, types.get(0));
        assertEquals(MediaType.of("text"), types.get(1));
        assertEquals(MediaType.ALL, types.get(2));
    }

    @Test
    void testIncludes() {
        assertTrue(MediaType.ALL.includes(MediaType.TEXT_HTML));
        assertFalse(MediaType.TEXT_HTML.includes(MediaType.ALL));

        assertTrue(MediaType.of("text").includes(MediaType.TEXT_HTML));
        assertFalse(MediaType.TEXT_HTML.includes(MediaType.of("text")));
    }

    @Test
    void testIsCompatibleWith() {
        assertTrue(MediaType.ALL.isCompatibleWith(MediaType.TEXT_HTML));
        assertTrue(MediaType.TEXT_HTML.isCompatibleWith(MediaType.ALL));

        assertFalse(MediaType.TEXT_HTML.isCompatibleWith(MediaType.TEXT_PLAIN));
        assertFalse(MediaType.TEXT_PLAIN.isCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void testCopyQualityValue() {
        final MediaType type0 = MediaType.parseMediaType("text/plain");
        assertSame(type0, type0.copyQualityValue(MediaType.ALL));

        final MediaType type1 = MediaType.parseMediaType("text/plain;a=b;c=d;m=n");
        final MediaType type2 = type1.copyQualityValue(MediaType.parseMediaType("*/*;a=xxx;q=11"));
        assertNotSame(type1, type2);
        assertEquals("text", type2.type());
        assertEquals("plain", type2.subtype());
        assertEquals(4, type2.parameters().size());
        assertEquals("b", type2.getParameter("a"));
        assertEquals("d", type2.getParameter("c"));
        assertEquals("n", type2.getParameter("m"));
        assertEquals(11L, type2.qValue());
    }

    @Test
    void testQValue() {
        final MediaType type0 = MediaType.ALL;
        assertEquals(1L, type0.qValue());

        final MediaType type1 = MediaType.of("text", "plain", Collections.singletonMap("q", "10"));
        assertEquals(10L, type1.qValue());
    }
}

