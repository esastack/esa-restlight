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
package io.esastack.restlight.core.mock;

import esa.commons.NetworkUtils;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.context.HttpInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpScheme;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHttpRequestTest {

    @Test
    void testGetProtocol() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withHttpVersion(HttpVersion.HTTP_1_1).build();
        assertEquals(HttpVersion.HTTP_1_1, request.httpVersion());
    }

    @Test
    void testGetScheme() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withScheme(HttpScheme.HTTPS.toString())
                .build();
        assertEquals("https", request.scheme());
    }

    @Test
    void testUri() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/abc/def?a=1&b=2")
                .build();
        assertEquals("/abc/def?a=1&b=2", request.uri());
        assertEquals("/abc/def", request.path());
        assertEquals("a=1&b=2", request.query());
        assertEquals("1", request.getParam("a"));
        assertEquals("2", request.getParam("b"));
    }

    @Test
    void testGetBodyByteBuf() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .build();
        assertEquals(0, request.body().readableBytes());
        assertEquals(Unpooled.EMPTY_BUFFER, BufferUtil.unwrap(request.body()));

        final MockHttpRequest request1 = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withBody("Restlight is good!".getBytes()).build();
        assertEquals("Restlight is good!".getBytes().length, request1.body().readableBytes());
    }

    @Test
    void testGetContentLength() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withBody("Restlight is good!".getBytes())
                .withUri("/")
                .build();
        assertEquals("Restlight is good!".getBytes().length, request.contentLength());
    }

    @Test
    void testGetRemoteAddr() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withRemoteAddr("127.0.0.9")
                .withUri("/")
                .build();
        assertEquals("127.0.0.9", request.remoteAddr());
    }

    @Test
    void testGetRemotePort() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withRemotePort(8080)
                .withUri("/")
                .build();
        assertEquals(8080, request.remotePort());
    }

    @Test
    void testGetTcpSourceIp() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withTcpSourceId("10.10.12.13")
                .withUri("/")
                .build();
        assertEquals("10.10.12.13", request.tcpSourceAddr());
    }

    @Test
    void testGetMethod() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withMethod("GET")
                .withUri("/")
                .build();
        assertEquals("GET", request.rawMethod());
    }

    @Test
    void testGetInputStream() throws IOException {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .build();
        HttpInputStream ins = request.inputStream();
        assertEquals(0, ins.available());

        final MockHttpRequest request1 = MockHttpRequest.aMockRequest()
                .withBody("Restlight is good!".getBytes())
                .withUri("/")
                .build();
        assertEquals("Restlight is good!".getBytes().length, request1.inputStream().available());
    }

    @Test
    void testGetLocalAddr() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withLocalAddr("127.0.0.1")
                .withUri("/")
                .build();
        assertEquals("127.0.0.1", request.localAddr());
    }

    @Test
    void testGetLocalPort() {
        final int port = NetworkUtils.selectRandomPort();
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withLocalPort(port)
                .withUri("/")
                .build();
        assertEquals(port, request.localPort());
    }

    @Test
    void testOperateHeader() {
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.set("B", "X");
        headers.add("B", "Y");
        headers.add("B", "Z");

        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withHeaders(headers)
                .withHeader("A", "X")
                .withHeader("A", "Y")
                .withHeader("A", "Z").build();

        assertEquals("X", request.headers().get("B"));
        assertEquals(3, request.headers().getAll("B").size());
        assertEquals("X", request.headers().get("A"));
        assertEquals(3, request.headers().getAll("A").size());
    }

    @Test
    void testOperateParameter() {
        final Map<String, List<String>> params = new HashMap<>();
        final List<String> values = new ArrayList<>(3);
        values.add("X");
        values.add("Y");
        values.add("Z");
        params.put("A", values);

        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withParameters(params)
                .withParameter("B", "X")
                .withParameter("B", "Y")
                .withParameter("B", "Z").build();

        assertEquals(2, request.paramsMap().size());

        assertEquals(3, request.getParams("A").size());
        assertEquals("X", request.getParam("A"));

        assertEquals(3, request.getParams("B").size());
        assertEquals("X", request.getParam("B"));
    }

    @Test
    void testTrailer() {
        final HttpHeaders headers = new Http1HeadersImpl();
        final MockHttpRequest request = MockHttpRequest.aMockRequest()
                .withTrailers(headers)
                .build();
        assertSame(headers, request.trailers());
    }

    @Test
    void testCookies() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest().build();
        assertTrue(request.cookies().isEmpty());

        final MockHttpRequest request1 = MockHttpRequest.aMockRequest()
                .withCookie("foo", "A")
                .withCookie(new CookieImpl("bar", "B"))
                .build();
        assertEquals(2, request1.cookies().size());
        final Iterator<Cookie> it = request1.cookies().iterator();
        assertEquals(new CookieImpl("bar", "B"), it.next());
        assertEquals(new CookieImpl("foo", "A"), it.next());
    }
}
