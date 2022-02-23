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

import jakarta.ws.rs.core.Link;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new LinkImpl(null, new HashMap<>()));
        assertThrows(NullPointerException.class, () -> new LinkImpl(URI.create("/abc"), null));
        assertDoesNotThrow(() -> new LinkImpl(URI.create("/abc"), new HashMap<>()));
    }

    @Test
    void testBasic() {
        final URI uri = URI.create("/abc/def");
        final Map<String, String> params = new HashMap<>();
        params.put(Link.REL, "xyz");
        params.put(Link.TITLE, "pq");
        params.put(Link.TYPE, "application/json");
        params.put("name", "value");
        final Link link1 = new LinkImpl(uri, params);
        assertSame(uri, link1.getUri());
        assertEquals(4, link1.getParams().size());
        assertEquals(uri, link1.getUriBuilder().build());
        assertEquals("xyz", link1.getRel());
        assertEquals("pq", link1.getTitle());
        assertEquals("application/json", link1.getType());

        params.put(Link.REL, "xyz xyz1 xyz2 xyz3");
        List<String> rels = link1.getRels();
        assertEquals(4, rels.size());
        assertEquals("xyz", rels.get(0));
        assertEquals("xyz1", rels.get(1));
        assertEquals("xyz2", rels.get(2));
        assertEquals("xyz3", rels.get(3));

        final Link link2 = new LinkImpl(uri, Collections.emptyMap());
        assertTrue(link2.getRels().isEmpty());
    }

    @Test
    void testEquals() {
        final URI uri = URI.create("/abc/def");
        final Map<String, String> params = new HashMap<>();
        params.put(Link.REL, "xyz");
        params.put(Link.TITLE, "pq");
        params.put(Link.TYPE, "application/json");
        params.put("name", "value");

        final Link link1 = new LinkImpl(uri, params);
        final Link link2 = new LinkImpl(uri, params);
        assertEquals(link1, link2);
        assertEquals(link2, link1);
        assertNotEquals(link1, null);
        assertNotEquals(link1, new Object());
    }

    @Test
    void testToString() {
        final URI uri = URI.create("/abc/def");
        final Map<String, String> params = new HashMap<>();
        params.put(Link.REL, "xyz");
        params.put(Link.TITLE, "pq");
        params.put(Link.TYPE, "application/json");
        params.put("name", "value");
        final Link link = new LinkImpl(uri, params);
        assertEquals("</abc/def>;rel=\"xyz\";title=\"pq\";type=\"application/json\";name=\"value\"",
                link.toString());
        assertEquals("</abc/def>;rel=\"xyz\";title=\"pq\";type=\"application/json\";name=\"value\"",
                link.toString());
    }

}

