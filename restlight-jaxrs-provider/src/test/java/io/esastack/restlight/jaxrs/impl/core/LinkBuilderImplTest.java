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
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkBuilderImplTest {

    @Test
    void testLink() {
        final URI uri = URI.create("/abc/def");
        final Map<String, String> params = new HashMap<>();
        params.put("name", "value");
        params.put(Link.REL, "/xyz0");
        params.put(Link.TITLE, "pq");
        params.put(Link.TYPE, "application/json");
        Link link = new LinkImpl(uri, params);

        LinkBuilderImpl builder = new LinkBuilderImpl();
        builder.link(link);
        final Link another = builder.build();
        assertEquals("value", another.getParams().get("name"));
        assertEquals("/xyz0", another.getRel());
        assertEquals("pq", another.getTitle());
        assertEquals("application/json", another.getType());
        assertEquals(link, another);
    }

    @Test
    void testStringLink() {
        Link link = new LinkBuilderImpl().link("</abc/def>;rel=\"xyz\";" +
                "title=\"pq\";type=\"application/json\";name=\"value\"").build();
        assertEquals("value", link.getParams().get("name"));
        assertEquals("xyz", link.getRel());
        assertEquals("pq", link.getTitle());
        assertEquals("application/json", link.getType());

        assertThrows(NullPointerException.class, () -> new LinkBuilderImpl().link((String) null).build());
    }

    @Test
    void testUris() {
        final LinkBuilderImpl builder = new LinkBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder.uri((URI) null));
        assertEquals("/xyz", builder.uri(URI.create("/xyz")).build().getUri().getPath());
        assertThrows(IllegalArgumentException.class, () -> builder.uri((String) null));
        assertEquals("/mn", builder.uri("/mn").build().getUri().getPath());

        assertEquals("<http://localhost:8080/mn>",
                builder.baseUri(URI.create("http://localhost:8080")).build().toString());
        assertEquals("</mn>", builder.baseUri((URI) null).build().toString());
        assertEquals("<http://localhost:8080/mn>",
                builder.baseUri("http://localhost:8080").build().toString());
        assertThrows(IllegalArgumentException.class, () -> builder.uriBuilder(null));

        assertEquals("/xyz", builder.uriBuilder(UriBuilder.fromUri("/xyz")).build().getUri().getPath());
    }

    @Test
    void testParam() {
        final Link.Builder builder = new LinkBuilderImpl().uri("/ddd");
        assertThrows(IllegalArgumentException.class, () -> builder.rel(null));
        builder.rel("/def");
        builder.rel("/xyz");
        final List<String> rels = builder.build().getRels();
        assertEquals(2, rels.size());
        assertEquals("/def", rels.get(0));
        assertEquals("/xyz", rels.get(1));

        assertThrows(IllegalArgumentException.class, () -> builder.title(null));
        builder.title("pq");
        assertEquals("pq", builder.build().getTitle());

        assertThrows(IllegalArgumentException.class, () -> builder.type(null));
        builder.type("application/json");
        assertEquals("application/json", builder.build().getType());

        assertThrows(IllegalArgumentException.class, () -> builder.param(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> builder.param("name", null));
        builder.param("name", "value");
        assertEquals("value", builder.build().getParams().get("name"));
    }

    @Test
    void testBuildRelativized() {
        final URI baseUri = URI.create("http://example.com:8080/app/root/");
        final LinkBuilderImpl builder = new LinkBuilderImpl();
        builder.baseUri(baseUri);
        builder.uri("http://example.com:8080/app/root/a/b/c/resource.html");
        assertEquals(URI.create("http://example.com:8080/app/root/a/b/c/resource.html"),
                builder.buildRelativized(URI.create("a/b/c/d/file.txt")).getUri());
    }

}

