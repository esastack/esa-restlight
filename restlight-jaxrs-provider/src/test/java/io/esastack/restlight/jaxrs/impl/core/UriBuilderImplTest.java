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
package io.esastack.restlight.jaxrs.impl.core;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UriBuilderImplTest {

    @Test
    void testConstructor() {
        assertDoesNotThrow(UriBuilderImpl::new);
    }

    @Test
    void testClone() {
        final String scheme = "https";
        final String userInfo = "LiMing.com";
        final String host = "localhost";
        final int port = 8080;
        final String path = "/abc/def";
        final String query = "a=b&c=d";
        final String fragment = "xyz";

        URI uri = UriBuilder.newInstance().scheme(scheme)
                .userInfo(userInfo)
                .host(host)
                .port(port)
                .path(path)
                .replaceQuery(query)
                .fragment(fragment)
                .clone()
                .build();
        assertEquals(scheme, uri.getScheme());
        assertEquals(userInfo, uri.getRawUserInfo());
        assertEquals(host, uri.getHost());
        assertEquals(port, uri.getPort());
        assertEquals(path, uri.getPath());
        assertEquals(query, uri.getQuery());
        assertEquals(fragment, uri.getFragment());
    }

    @Test
    void testUri() {
        final UriBuilder builder = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder.uri((URI) null));
        final URI uri = URI.create("https://LiMing.com@localhost:8080/abc/def?a=b&c=d#xyz");
        final URI uri1 = builder.uri(uri).build();
        assertEquals(uri.getScheme(), uri1.getScheme());
        assertEquals(uri.getRawUserInfo(), uri1.getRawUserInfo());
        assertEquals(uri.getHost(), uri1.getHost());
        assertEquals(uri.getPort(), uri1.getPort());
        assertEquals(uri.getPath(), uri1.getPath());
        assertEquals(uri.getQuery(), uri1.getQuery());
        assertEquals(uri.getFragment(), uri1.getFragment());
    }

    @Test
    void testStringUri() {
        final UriBuilder builder = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder.uri((String) null));
        final URI uri = URI.create("https://LiMing.com@localhost:8080/abc/def?a=b&c=d#xyz");
        final URI uri1 = builder.uri(uri.toString()).build();
        assertEquals(uri.getScheme(), uri1.getScheme());
        assertEquals(uri.getRawUserInfo(), uri1.getRawUserInfo());
        assertEquals(uri.getHost(), uri1.getHost());
        assertEquals(uri.getPort(), uri1.getPort());
        assertEquals(uri.getPath(), uri1.getPath());
        assertEquals(uri.getQuery(), uri1.getQuery());
        assertEquals(uri.getFragment(), uri1.getFragment());
    }

    @Test
    void testScheme() {
        assertEquals("http", new UriBuilderImpl().scheme(null).host("localhost").build().getScheme());
        assertEquals("https", new UriBuilderImpl().scheme("https").host("localhost").build().getScheme());
        assertThrows(IllegalArgumentException.class, () -> new UriBuilderImpl().scheme("HTTPS"));
    }

    @Test
    void testSchemeSpecificPart() {
        final UriBuilder builder = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder.schemeSpecificPart(null));

        builder.fragment("xyz");
        final URI uri = builder.schemeSpecificPart("LiMing.com@localhost:9090/abc/def")
                .build();
        assertEquals("http", uri.getScheme());
        assertEquals("LiMing.com", uri.getRawUserInfo());
        assertEquals("LiMing.com@localhost:9090", uri.getAuthority());
        assertEquals("localhost", uri.getHost());
        assertEquals(9090, uri.getPort());
        assertEquals("/abc/def", uri.getPath());
        assertEquals("xyz", uri.getFragment());
        assertNull(uri.getQuery());
    }

    @Test
    void testUriInfo() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080/abc/def?a=b&c=d#xyz");
        builder.userInfo("LiMing.com1");
        assertEquals("LiMing.com1", builder.build().getUserInfo());
    }

    @Test
    void testHost() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080/abc/def?a=b&c=d#xyz");
        assertThrows(IllegalArgumentException.class, () -> builder.host(null));
        assertThrows(IllegalArgumentException.class, () -> builder.host(""));
        builder.host("127.0.0.1");
        assertEquals("127.0.0.1", builder.build().getHost());
    }

    @Test
    void testPort() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080/abc/def?a=b&c=d#xyz");
        assertThrows(IllegalArgumentException.class, () -> builder.port(0));
        assertThrows(IllegalArgumentException.class, () -> builder.port(65536));
        builder.port(9999);
        assertEquals(9999, builder.build().getPort());
    }

    @Test
    void testReplacePath() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080");
        builder.replacePath("def");
        assertEquals("/def", builder.build().getPath());

        builder.replacePath(null);
        assertEquals("", builder.build().getPath());
    }

    @Test
    void testStringPath() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder.path((String) null));

        builder.path("def");
        assertEquals("/def", builder.build().getPath());

        builder.path("mn/");
        assertEquals("/def/mn/", builder.build().getPath());

        builder.path("/xyz");
        assertEquals("/def/mn/xyz", builder.build().getPath());
    }

    @Test
    void testClassPath() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder.path((Class) null));
        assertThrows(IllegalArgumentException.class, () -> builder.path(Object.class));

        builder.path(Hello.class);
        assertEquals("/abc/", builder.build().getPath());
    }

    @Test
    void testMethodPath() throws Throwable {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder1.path(null, "hello"));
        assertThrows(IllegalArgumentException.class, () -> builder1.path(Hello.class, "hello"));
        assertThrows(IllegalArgumentException.class, () -> builder1.path(Object.class, "equals"));

        builder1.path(Hello.class, "hello0");
        assertEquals("/def2", builder1.build().getPath());

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder2.path((Method) null));
        assertThrows(IllegalArgumentException.class, () -> builder2.path(Object.class.getMethod("equals",
                Object.class)));
        assertEquals("/def", builder2.path(Hello.class.getDeclaredMethod("hello")).build().getPath());
    }

    @Test
    void testSegment() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder.segment(null));
        assertThrows(IllegalArgumentException.class, () -> builder.segment(new String[]{"abc", null}));

        URI uri = builder.segment("/abc/", "def%").build();
        assertEquals("/%2Fabc%2F/def%25", uri.getRawPath());
        assertEquals("//abc//def%", uri.getPath());
    }

    @Test
    void testReplaceMatrix() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        builder1.replaceMatrix("/a=b%&c=d");
        URI uri1 = builder1.build();
        assertEquals("/;%2Fa=b%25&c=d", uri1.getRawPath());
        assertEquals("/;/a=b%&c=d", uri1.getPath());

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080/abc");
        builder2.replaceMatrix("a=/b&c=%d");
        URI uri2 = builder2.build();
        assertEquals("/abc;a=%2Fb&c=%25d", uri2.getRawPath());
        assertEquals("/abc;a=/b&c=%d", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080/abc;a=b");
        builder3.replaceMatrix("m=/n&c=%d");
        URI uri3 = builder3.build();
        assertEquals("/abc;m=%2Fn&c=%25d", uri3.getRawPath());
        assertEquals("/abc;m=/n&c=%d", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.uri("https://LiMing.com@localhost:8080/abc;a=b");
        builder4.replaceMatrix(null);
        URI uri4 = builder4.build();
        assertEquals("/abc", uri4.getRawPath());
        assertEquals("/abc", uri4.getPath());
    }

    @Test
    void testMatrixParam() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder1.matrixParam("a", null));
        assertThrows(IllegalArgumentException.class, () -> builder1.matrixParam(null, new Object()));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080");
        builder2.matrixParam("a", "c%d", "m/n");
        URI uri2 = builder2.build();
        assertEquals("/;a=c%25d;a=m%2Fn", uri2.getRawPath());
        assertEquals("/;a=c%d;a=m/n", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080/abc");
        builder2.uri("https://LiMing.com@localhost:8080");
        builder3.matrixParam("a", "c%d", "m/n");
        URI uri3 = builder3.build();
        assertEquals("/abc;a=c%25d;a=m%2Fn", uri3.getRawPath());
        assertEquals("/abc;a=c%d;a=m/n", uri3.getPath());
    }

    @Test
    void testReplaceMatrixParam() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder1.replaceMatrixParam(null, new Object()));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080");
        builder2.replaceMatrixParam("a", "c%d", "m/n");
        URI uri2 = builder2.build();
        assertEquals("/;a=c%25d;a=m%2Fn", uri2.getRawPath());
        assertEquals("/;a=c%d;a=m/n", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080/abc");
        builder2.uri("https://LiMing.com@localhost:8080");
        builder3.replaceMatrixParam("a", "c%d", "m/n");
        URI uri3 = builder3.build();
        assertEquals("/abc;a=c%25d;a=m%2Fn", uri3.getRawPath());
        assertEquals("/abc;a=c%d;a=m/n", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.uri("https://LiMing.com@localhost:8080/abc;a=b");
        builder4.replaceMatrixParam("a", null);
        URI uri4 = builder4.build();
        assertEquals("/abc", uri4.getRawPath());
        assertEquals("/abc", uri4.getPath());
    }

    @Test
    void testReplaceQuery() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080?a=b&c=d");
        builder1.replaceQuery("x=%d&y=m/n");
        final URI uri1 = builder1.build();
        assertEquals("x=%25d&y=m%2Fn", uri1.getRawQuery());
        assertEquals("x=%d&y=m/n", uri1.getQuery());

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080?a=b&c=d");
        builder2.replaceQuery(null);
        final URI uri2 = builder2.build();
        assertNull(uri2.getRawQuery());
        assertNull(uri2.getQuery());
    }

    @Test
    void testQueryParam() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder1.queryParam(null, new Object()));
        assertThrows(IllegalArgumentException.class, () -> builder1.queryParam("a", null));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080");
        builder2.queryParam("a", "c%d", "m/n");
        final URI uri2 = builder2.build();
        assertEquals("a=c%25d&a=m%2Fn", uri2.getRawQuery());
        assertEquals("a=c%d&a=m/n", uri2.getQuery());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080?a=b");
        builder3.queryParam("a", "c%d", "m/n");
        final URI uri3 = builder3.build();
        assertEquals("a=b&a=c%25d&a=m%2Fn", uri3.getRawQuery());
        assertEquals("a=b&a=c%d&a=m/n", uri3.getQuery());
    }

    @Test
    void testReplaceQueryParam() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080");
        assertThrows(IllegalArgumentException.class, () -> builder1.replaceQueryParam(null, new Object()));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080");
        builder2.replaceQueryParam("a", "c%d", "m/n");
        final URI uri2 = builder2.build();
        assertEquals("a=c%25d&a=m%2Fn", uri2.getRawQuery());
        assertEquals("a=c%d&a=m/n", uri2.getQuery());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080?a=b");
        builder3.replaceQueryParam("a", "c%d", "m/n");
        final URI uri3 = builder3.build();
        assertEquals("a=c%25d&a=m%2Fn", uri3.getRawQuery());
        assertEquals("a=c%d&a=m/n", uri3.getQuery());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.uri("https://LiMing.com@localhost:8080?a=b");
        builder4.replaceQueryParam("a", null);
        final URI uri4 = builder4.build();
        assertNull(uri4.getRawQuery());
        assertNull(uri4.getQuery());
    }

    @Test
    void testFragment() {
        final UriBuilder builder1 = new UriBuilderImpl();
        builder1.uri("https://LiMing.com@localhost:8080#mn");
        builder1.fragment("x%ym/n");
        final URI uri1 = builder1.build();
        assertEquals("x%25ym%2Fn", uri1.getRawFragment());
        assertEquals("x%ym/n", uri1.getFragment());

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.uri("https://LiMing.com@localhost:8080#mn");
        builder2.fragment("");
        final URI uri2 = builder2.build();
        assertNull(uri2.getRawFragment());
        assertNull(uri2.getFragment());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.uri("https://LiMing.com@localhost:8080");
        builder3.fragment("x%ym/n");
        final URI uri3 = builder3.build();
        assertEquals("x%25ym%2Fn", uri3.getRawFragment());
        assertEquals("x%ym/n", uri3.getFragment());
    }

    @Test
    void testResolveTemplate1() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplate(null, new Object()));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplate("ab", null));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplate("name", "LiMin/g");
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.scheme("http");
        builder3.userInfo("{name}.com");
        builder3.host("{name}.domain");
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.replaceQuery("name={name}");
        builder3.fragment("xyz{name}");

        builder3.resolveTemplate("name0", "LiMin/g");
        assertThrows(UriBuilderException.class, builder3::build);

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder4.resolveTemplate("name", "LiMing");
        assertThrows(UriBuilderException.class, builder4::build);
    }

    @Test
    void testResolveTemplate2() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplate(null, new Object(),
                false));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplate("ab", null,
                false));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplate("name", "LiMin/g", false);
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.scheme("http");
        builder3.userInfo("{name}.com");
        builder3.host("{name}.domain");
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.replaceQuery("name={name}");
        builder3.fragment("xyz{name}");

        builder3.resolveTemplate("name0", "LiMin/g", false);
        assertThrows(UriBuilderException.class, builder3::build);

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder4.resolveTemplate("name", "LiMing", false);
        assertThrows(UriBuilderException.class, builder4::build);
    }

    @Test
    void testResolveTemplateFromEncoded() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplateFromEncoded(null,
                new Object()));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplateFromEncoded("ab",
                null));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplateFromEncoded("name", "LiMin/g");
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.resolveTemplateFromEncoded("name", "LiMin/g%yy%E4%B8%AD");
        final URI uri3 = builder3.build();
        assertEquals("/abc/LiMin%2Fg%25yy%E4%B8%AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy中//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{name}.com");
        builder4.host("{name}.domain");
        builder4.path("/abc/{name}%2F/mn%25");
        builder4.replaceQuery("name={name}");
        builder4.fragment("xyz{name}");

        builder4.resolveTemplate("name0", "LiMin/g", false);
        assertThrows(UriBuilderException.class, builder4::build);

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder5.resolveTemplate("name", "LiMing", false);
        assertThrows(UriBuilderException.class, builder5::build);
    }

    @Test
    void testResolveTemplates1() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(
                Collections.singletonMap("ab", null)));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(
                Collections.singletonMap(null, "ab")));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplates(Collections.singletonMap("name", "LiMin/g"));
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.scheme("http");
        builder3.userInfo("{name}.com");
        builder3.host("{name}.domain");
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.replaceQuery("name={name}");
        builder3.fragment("xyz{name}");

        builder3.resolveTemplates(Collections.singletonMap("name0", "LiMin/g"));
        assertThrows(UriBuilderException.class, builder3::build);

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder4.resolveTemplates(Collections.singletonMap("name", "LiMing"));
        assertThrows(UriBuilderException.class, builder4::build);
    }

    @Test
    void testResolveTemplates2() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(null,
                false));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(
                Collections.singletonMap("ab", null), false));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplates(
                Collections.singletonMap(null, "ab"), false));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplates(Collections.singletonMap("name", "LiMin/g"), false);
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.scheme("http");
        builder3.userInfo("{name}.com");
        builder3.host("{name}.domain");
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.replaceQuery("name={name}");
        builder3.fragment("xyz{name}");

        builder3.resolveTemplates(Collections.singletonMap("name0", "LiMin/g"), false);
        assertThrows(UriBuilderException.class, builder3::build);

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder4.resolveTemplates(Collections.singletonMap("name", "LiMing"), false);
        assertThrows(UriBuilderException.class, builder4::build);
    }

    @Test
    void testResolveTemplatesFromEncoded() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplatesFromEncoded(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplatesFromEncoded(
                Collections.singletonMap("ab", null)));
        assertThrows(IllegalArgumentException.class, () -> builder1.resolveTemplatesFromEncoded(
                Collections.singletonMap(null, "ab")));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        builder2.resolveTemplatesFromEncoded(Collections.singletonMap("name", "LiMin/g"));
        final URI uri2 = builder2.build();
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{name}%2F/mn%25");
        builder3.resolveTemplatesFromEncoded(Collections.singletonMap("name", "LiMin/g%yy%E4%B8%AD"));
        final URI uri3 = builder3.build();
        assertEquals("/abc/LiMin%2Fg%25yy%E4%B8%AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy中//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{name}.com");
        builder4.host("{name}.domain");
        builder4.path("/abc/{name}%2F/mn%25");
        builder4.replaceQuery("name={name}");
        builder4.fragment("xyz{name}");

        builder4.resolveTemplatesFromEncoded(Collections.singletonMap("name0", "LiMin/g"));
        assertThrows(UriBuilderException.class, builder4::build);

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        builder5.resolveTemplatesFromEncoded(Collections.singletonMap("name", "LiMing"));
        assertThrows(UriBuilderException.class, builder5::build);
    }

    @Test
    void testBuildFromMap1() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.buildFromMap(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.buildFromMap(
                Collections.singletonMap("ab", null)));
        assertDoesNotThrow(() -> builder1.buildFromMap(Collections.singletonMap(null, "ab")));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        final URI uri2 = builder2.buildFromMap(Collections.singletonMap("name", "LiMin/g"));
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{name}%2F/mn%25");
        final URI uri3 = builder3.buildFromMap(Collections.singletonMap("name", "LiMin/g%yy%E4%B8%AD"));
        assertEquals("/abc/LiMin%2Fg%25yy%25E4%25B8%25AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy%E4%B8%AD//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{name}.com");
        builder4.host("{name}.domain");
        builder4.path("/abc/{name}%2F/mn%25");
        builder4.replaceQuery("name={name}");
        builder4.fragment("xyz{name}");
        assertThrows(UriBuilderException.class,
                () -> builder4.buildFromMap(Collections.singletonMap("name0", "LiMin/g")));

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.buildFromMap(Collections.singletonMap("name", "LiMing")));
    }

    @Test
    void testBuildFromMap2() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.buildFromMap(null, false));
        assertThrows(IllegalArgumentException.class, () -> builder1.buildFromMap(
                Collections.singletonMap("ab", null), false));
        assertDoesNotThrow(() -> builder1.buildFromMap(Collections.singletonMap(null, "ab"), false));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        final URI uri2 = builder2.buildFromMap(Collections.singletonMap("name", "LiMin/g"), false);
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{name}%2F/mn%25");
        final URI uri3 = builder3.buildFromMap(Collections.singletonMap("name", "LiMin/g%yy%E4%B8%AD"),
                false);
        assertEquals("/abc/LiMin/g%25yy%25E4%25B8%25AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy%E4%B8%AD//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{name}.com");
        builder4.host("{name}.domain");
        builder4.path("/abc/{name}%2F/mn%25");
        builder4.replaceQuery("name={name}");
        builder4.fragment("xyz{name}");
        assertThrows(UriBuilderException.class,
                () -> builder4.buildFromMap(Collections.singletonMap("name0", "LiMin/g"), false));

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.buildFromMap(Collections.singletonMap("name", "LiMing"), false));
    }

    @Test
    void testBuildFromEncodedMap() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.buildFromEncodedMap(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.buildFromEncodedMap(
                Collections.singletonMap("ab", null)));
        assertDoesNotThrow(() -> builder1.buildFromEncodedMap(Collections.singletonMap(null, "ab")));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{name}.com");
        builder2.host("{name}.domain");
        builder2.path("/abc/{name}%2F/mn%25");
        builder2.replaceQuery("name={name}");
        builder2.fragment("xyz{name}");

        final URI uri2 = builder2.buildFromEncodedMap(Collections.singletonMap("name", "LiMin/g"));
        assertEquals("http://LiMin/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{name}%2F/mn%25");
        final URI uri3 = builder3.buildFromEncodedMap(Collections.singletonMap("name", "LiMin/g%yy%E4%B8%AD"));
        assertEquals("/abc/LiMin%2Fg%25yy%E4%B8%AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy中//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{name}.com");
        builder4.host("{name}.domain");
        builder4.path("/abc/{name}%2F/mn%25");
        builder4.replaceQuery("name={name}");
        builder4.fragment("xyz{name}");
        assertThrows(UriBuilderException.class,
                () -> builder4.buildFromEncodedMap(Collections.singletonMap("name0", "LiMin/g")));

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{name:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.buildFromEncodedMap(Collections.singletonMap("name", "LiMing")));
    }

    @Test
    void testBuild1() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.build(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.build("ab", null));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{a}.com");
        builder2.host("{a}.{b}.{a}.domain");
        builder2.path("/abc/{a}%2F/mn%25");
        builder2.replaceQuery("name={a}");
        builder2.fragment("xyz{a}");

        final URI uri2 = builder2.build("LiMin/g", "y", "z");
        assertEquals(
                "http://LiMin/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{a}%2F/mn%25");
        final URI uri3 = builder3.build("LiMin/g%yy%E4%B8%AD");
        assertEquals("/abc/LiMin%2Fg%25yy%25E4%25B8%25AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy%E4%B8%AD//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{a}.com");
        builder4.host("{a}.domain");
        builder4.path("/abc/{a}%2F/mn%25");
        builder4.replaceQuery("name={a}");
        builder4.fragment("xyz{a}");
        assertThrows(UriBuilderException.class, builder4::build);

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{a:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.build("LiMing"));
    }

    @Test
    void testBuild2() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.build(null, false));
        assertThrows(IllegalArgumentException.class, () -> builder1.build(new String[] {"ab", null},
                false));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{a}.com");
        builder2.host("{a}.{b}.{a}.domain");
        builder2.path("/abc/{a}%2F/mn%25");
        builder2.replaceQuery("name={a}");
        builder2.fragment("xyz{a}");

        final URI uri2 = builder2.build(new String[] {"LiMin/g", "y", "z"}, false);
        assertEquals(
                "http://LiMin/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin/g%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin/g%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{a}%2F/mn%25");
        final URI uri3 = builder3.build(new String[] {"LiMin/g%yy%E4%B8%AD"}, false);
        assertEquals("/abc/LiMin/g%25yy%25E4%25B8%25AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy%E4%B8%AD//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{a}.com");
        builder4.host("{a}.domain");
        builder4.path("/abc/{a}%2F/mn%25");
        builder4.replaceQuery("name={a}");
        builder4.fragment("xyz{a}");
        assertThrows(UriBuilderException.class, () -> builder4.build(new String[0], false));

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{a:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.build(new String[] {"LiMing"}, false));
    }

    @Test
    void testBuildFromEncoded() {
        final UriBuilder builder1 = new UriBuilderImpl();
        assertDoesNotThrow(() -> builder1.buildFromEncoded(null));
        assertThrows(IllegalArgumentException.class, () -> builder1.buildFromEncoded("ab", null));

        final UriBuilder builder2 = new UriBuilderImpl();
        builder2.scheme("http");
        builder2.userInfo("{a}.com");
        builder2.host("{a}.{b}.{a}.domain");
        builder2.path("/abc/{a}%2F/mn%25");
        builder2.replaceQuery("name={a}");
        builder2.fragment("xyz{a}");

        final URI uri2 = builder2.buildFromEncoded("LiMin/g", "y", "z");
        assertEquals(
                "http://LiMin/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25?name=LiMin/g#xyzLiMin/g",
                uri2.toString());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin%2Fg%2F/mn%25", uri2.getRawPath());
        assertEquals("/g.com@LiMin/g.y.LiMin/g.domain/abc/LiMin/g//mn%", uri2.getPath());

        final UriBuilder builder3 = new UriBuilderImpl();
        builder3.path("/abc/{a}%2F/mn%25");
        final URI uri3 = builder3.buildFromEncoded("LiMin/g%yy%E4%B8%AD");
        assertEquals("/abc/LiMin%2Fg%25yy%E4%B8%AD%2F/mn%25", uri3.getRawPath());
        assertEquals("/abc/LiMin/g%yy中//mn%", uri3.getPath());

        final UriBuilder builder4 = new UriBuilderImpl();
        builder4.scheme("http");
        builder4.userInfo("{a}.com");
        builder4.host("{a}.domain");
        builder4.path("/abc/{a}%2F/mn%25");
        builder4.replaceQuery("name={a}");
        builder4.fragment("xyz{a}");
        assertThrows(UriBuilderException.class, builder4::buildFromEncoded);

        final UriBuilder builder5 = new UriBuilderImpl();
        builder5.path("/abc/{a:[\\p{L}\\.]+}%2F/mn%25");
        assertThrows(UriBuilderException.class,
                () -> builder5.buildFromEncoded("LiMing"));
    }

    @Test
    void testToTemplate() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.scheme("http");
        builder.userInfo("{a}.com");
        builder.host("{a}.{b}.{a}.domain");
        builder.path("/abc/{a}%2F/mn%25");
        builder.replaceQuery("name={a}");
        builder.fragment("xyz{a}");

        assertEquals("http://{a}.com@{a}.{b}.{a}.domain/abc/{a}%2F/mn%25?name={a}#xyz{a}",
                builder.toTemplate());
    }

    @Test
    void testToString() {
        final UriBuilder builder = new UriBuilderImpl();
        builder.scheme("http");
        builder.userInfo("{a}.com");
        builder.host("{a}.{b}.{a}.domain");
        builder.path("/abc/{a}%2F/mn%25");
        builder.replaceQuery("name={a}");
        builder.fragment("xyz{a}");

        assertEquals("UriBuilderImpl{scheme='http', userInfo='{a}.com', host='{a}.{b}.{a}.domain'," +
                " port=0, path='/abc/{a}%2F/mn%25', query='name={a}', fragment='xyz{a}'}", builder.toString());
    }

    @Path("abc/")
    private static class Hello {

        @Path("/def")
        public void hello() {

        }

        @Path("/def1")
        public void hello(String name) {

        }

        @Path("/def2")
        public void hello0(String name) {

        }
    }
}

