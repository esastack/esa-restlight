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
package io.esastack.httpserver.impl;

import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.io.IOUtils;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpserver.core.Aggregation;
import io.esastack.httpserver.core.AttributesImpl;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRequestImplTest {

    @Test
    void testHeaderAndTrailerConvert() throws IOException {
        final Request mock = mock(Request.class);
        when(mock.method()).thenReturn(HttpMethod.POST);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        when(mock.headers()).thenReturn(new Http1HeadersImpl());
        final Aggregation aggregation = mock(Aggregation.class);
        final ByteBuf body = Unpooled.copiedBuffer("abc".getBytes(StandardCharsets.UTF_8));
        when(aggregation.body()).thenReturn(body);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());

        when(mock.version()).thenReturn(HttpVersion.HTTP_1_1);
        assertEquals(HttpVersion.HTTP_1_1, req.httpVersion());

        when(mock.scheme()).thenReturn("http");
        assertEquals("http", req.scheme());

        when(mock.uri()).thenReturn("/foo?a=1");
        assertEquals("/foo?a=1", req.uri());

        when(mock.path()).thenReturn("/foo");
        assertEquals("/foo", req.path());

        when(mock.query()).thenReturn("a=1");
        assertEquals("a=1", req.query());

        when(mock.version()).thenReturn(HttpVersion.HTTP_1_1);

        assertFalse(req.hasAttribute("name0"));
        req.setAttribute("name0", "value1");
        assertTrue(req.hasAttribute("name0"));

        when(mock.scheme()).thenReturn("https");
        assertEquals("https", req.scheme());

        when(mock.path()).thenReturn("/abc");
        assertEquals("/abc", req.path());

        when(mock.uri()).thenReturn("/def");
        assertEquals("/def", req.uri());

        assertEquals(HttpMethod.POST, req.method());
        assertEquals("POST", req.rawMethod());

        assertSame(body, BufferUtil.unwrap(req.bufferBody()));
        assertEquals("abc", new String(req.body(), StandardCharsets.UTF_8));
        assertEquals(3L, req.contentLength());

        final HttpInputStream in = req.inputStream();
        assertSame(in, req.inputStream());
        assertTrue(in instanceof ByteBufHttpInputStream);
        assertEquals("abc", IOUtils.toString(in, StandardCharsets.UTF_8));

        when(mock.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        assertEquals("127.0.0.1", req.remoteAddr());
        assertEquals(8080, req.remotePort());

        when(mock.tcpSourceAddress()).thenReturn(new InetSocketAddress("127.0.0.2", 8080));
        assertEquals("127.0.0.2", req.tcpSourceAddr());

        when(mock.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 1234));
        assertEquals("127.0.0.1", req.localAddr());
        assertEquals(1234, req.localPort());

        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("a", "1");
        params.add("b", "2");
        when(mock.paramMap()).thenReturn(params);
        assertSame(params, req.parameterMap());

        assertEquals("1", req.getParameter("a"));
        assertEquals("2", req.getParameter("b"));
        assertEquals(1, req.getParameters("a").size());
        assertEquals(2, req.parameterMap().size());
        assertEquals(2, req.parameterMap().size());

        when(mock.toString()).thenReturn("foo");
        assertEquals("foo", req.toString());
    }

    @Test
    void testConvertHttp1Header() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("a", "1");
        headers.add("b", "2");
        when(mock.headers()).thenReturn(headers);

        final Aggregation aggregation = mock(Aggregation.class);
        final HttpHeaders trailer = new Http1HeadersImpl();
        trailer.add("c", "1");
        trailer.add("d", "2");
        when(aggregation.trailers()).thenReturn(trailer);
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        assertSame(headers, req.headers());
        assertSame(trailer, req.trailers());
        verifyHeaders(req);
    }

    @Test
    void testConvertHttp2Header() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        final HttpHeaders headers = new Http2HeadersImpl();
        headers.add("a", "1");
        headers.add("b", "2");
        when(mock.headers()).thenReturn(headers);

        final Aggregation aggregation = mock(Aggregation.class);
        final HttpHeaders trailer = new Http2HeadersImpl();
        trailer.add("c", "1");
        trailer.add("d", "2");
        when(aggregation.trailers()).thenReturn(trailer);
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        assertSame(headers, req.headers());
        assertSame(trailer, req.trailers());
        verifyHeaders(req);
    }

    @Test
    void testDecodeCookies() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add(HttpHeaderNames.COOKIE, "a=1; b=2");
        when(mock.headers()).thenReturn(headers);

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        final Set<Cookie> cookies = req.cookies();
        assertNotNull(cookies);
        assertEquals(2, cookies.size());
        assertEquals("a", req.getCookie("a").name());
        assertEquals("1", req.getCookie("a").value());
        assertEquals("b", req.getCookie("b").name());
        assertEquals("2", req.getCookie("b").value());

        assertSame(cookies, req.cookies());
    }

    @Test
    void testDecodeEmptyCookies() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        final HttpHeaders headers = new Http1HeadersImpl();
        when(mock.headers()).thenReturn(headers);

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        final Set<Cookie> cookies = req.cookies();
        assertTrue(cookies.isEmpty());
        assertSame(cookies, req.cookies());
    }

    @Test
    void testMergeUrlEncodedParams() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        when(mock.method()).thenReturn(HttpMethod.POST);
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.set(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.APPLICATION_FORM_URLENCODED_VALUE);
        when(mock.headers()).thenReturn(headers);

        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("a", "1");
        params.add("b", "2");
        when(mock.paramMap()).thenReturn(params);

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        final ByteBuf body = Unpooled.copiedBuffer("b=3&c=4".getBytes(StandardCharsets.UTF_8));
        when(aggregation.body()).thenReturn(body);
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        assertEquals(3, req.parameterMap().size());
        assertEquals("1", req.getParameter("a"));
        assertEquals("2", req.getParameter("b"));
        assertEquals("4", req.getParameter("c"));
    }

    @Test
    void testMergeUrlEncodedParamsWithIllegalContentType() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        when(mock.method()).thenReturn(HttpMethod.POST);
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.set(HttpHeaderNames.CONTENT_TYPE, "abcdefghijklmnopqrstuvwxyz");
        when(mock.headers()).thenReturn(headers);

        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("a", "1");
        params.add("b", "2");
        when(mock.paramMap()).thenReturn(params);

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        final ByteBuf body = Unpooled.copiedBuffer("b=3&c=4".getBytes(StandardCharsets.UTF_8));
        when(aggregation.body()).thenReturn(body);
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        assertEquals(2, req.parameterMap().size());
        assertEquals("1", req.getParameter("a"));
        assertEquals("2", req.getParameter("b"));
    }

    @Test
    void testMergeUrlEncodedParamsWithIllegalBody() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        when(mock.method()).thenReturn(HttpMethod.POST);
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        when(mock.headers()).thenReturn(headers);

        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("a", "1");
        params.add("b", "2");
        when(mock.paramMap()).thenReturn(params);

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        final ByteBuf body = Unpooled.copiedBuffer("%%%+++".getBytes(StandardCharsets.UTF_8));
        when(aggregation.body()).thenReturn(body);
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());
        assertEquals(2, req.parameterMap().size());
        assertEquals("1", req.getParameter("a"));
        assertEquals("2", req.getParameter("b"));
    }

    @Test
    void testAttribute() {
        final Request mock = mock(Request.class);
        when(mock.rawMethod()).thenReturn(HttpMethod.POST.name());
        when(mock.headers()).thenReturn(new Http1HeadersImpl());

        final Aggregation aggregation = mock(Aggregation.class);
        when(aggregation.trailers()).thenReturn(new Http1HeadersImpl());
        when(mock.aggregated()).thenReturn(aggregation);

        final HttpRequestImpl req = new HttpRequestImpl(mock, new AttributesImpl());

        assertEquals(0, req.attributeNames().length);
        req.setAttribute("a", 1);
        assertEquals(1, req.attributeNames().length);
        assertEquals(1, req.getAttribute("a"));
        assertEquals(Integer.valueOf(1), req.getUncheckedAttribute("a"));
        req.setAttribute("b", "2");
        assertArrayEquals(new String[]{"a", "b"}, req.attributeNames());
        assertEquals(2, req.attributeNames().length);
        assertEquals("2", req.removeAttribute("b"));
        assertEquals(Integer.valueOf(1), req.removeUncheckedAttribute("a"));

        assertEquals(0, req.attributeNames().length);
    }


    private static void verifyHeaders(HttpRequestImpl req) {
        assertEquals("1", req.headers().get("a"));
        assertEquals("2", req.headers().get("b"));
        assertTrue(req.containsHeader("a"));
        assertTrue(req.containsHeader("b"));
        assertTrue(req.containsHeader(AsciiString.of("a")));
        assertTrue(req.containsHeader(AsciiString.of("b")));
        assertEquals("1", req.getHeader("a"));
        assertEquals("2", req.getHeader("b"));
        assertEquals("1", req.getHeader(AsciiString.of("a")));
        assertEquals("2", req.getHeader(AsciiString.of("b")));
        assertEquals(1, req.getIntHeader("a"));
        assertEquals(2, req.getIntHeader("b"));
        assertEquals(1, req.getIntHeader(AsciiString.of("a")));
        assertEquals(2, req.getIntHeader(AsciiString.of("b")));
        assertEquals((short) 1, req.getShortHeader("a"));
        assertEquals((short) 2, req.getShortHeader("b"));
        assertEquals((short) 1, req.getShortHeader(AsciiString.of("a")));
        assertEquals((short) 2, req.getShortHeader(AsciiString.of("b")));

        req.headers().set("x", "y");
        req.headers().set((CharSequence) "y", "z");
        req.headers().set("m", "n");
        req.headers().set((CharSequence) "m0", "n");
        assertEquals("y", req.getHeader("x"));
        assertEquals("z", req.getHeader("y"));
        assertEquals("n", req.getHeader("m"));
        assertEquals("n", req.getHeader("m0"));
    }

}
