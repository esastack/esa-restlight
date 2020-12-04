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
package esa.restlight.test.mock;

import esa.commons.NetworkUtils;
import esa.httpserver.core.HttpInputStream;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MockAsyncRequestTest {

    @Test
    void testGetProtocol() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withProtocol(HttpVersion.HTTP_1_1).build();
        assertEquals(HttpVersion.HTTP_1_1.text(), request.protocol());
    }

    @Test
    void testGetScheme() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withSchema(HttpScheme.HTTPS)
                .build();
        assertEquals("HTTPS", request.scheme());
    }

    @Test
    void testUri() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/abc/def?a=1&b=2")
                .build();
        assertEquals("/abc/def?a=1&b=2", request.uri());
        assertEquals("/abc/def", request.path());
        assertEquals("a=1&b=2", request.query());
        assertEquals("1", request.getParameter("a"));
        assertEquals("2", request.getParameter("b"));
    }

    @Test
    void testGetBodyByteBuf() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .build();
        assertEquals(0, request.byteBufBody().readableBytes());
        assertEquals(0, request.byteBufBody().maxWritableBytes());
        assertEquals(Unpooled.EMPTY_BUFFER, request.byteBufBody());

        final MockAsyncRequest request1 = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withBody("Restlight is good!".getBytes()).build();
        assertEquals("Restlight is good!".getBytes().length, request1.byteBufBody().readableBytes());

        byte[] result = new byte[18];
        request1.byteBufBody().getBytes(0, result);
        assertArrayEquals(result, request1.body());
    }

    @Test
    void testGetContentLength() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withBody("Restlight is good!".getBytes())
                .withUri("/")
                .build();
        assertEquals("Restlight is good!".getBytes().length, request.contentLength());
    }

    @Test
    void testGetRemoteAddr() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withRemoteAddr("127.0.0.9")
                .withUri("/")
                .build();
        assertEquals("127.0.0.9", request.remoteAddr());
    }

    @Test
    void testGetRemotePort() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withRemotePort(8080)
                .withUri("/")
                .build();
        assertEquals(8080, request.remotePort());
    }

    @Test
    void testGetTcpSourceIp() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withTcpSourceId("10.10.12.13")
                .withUri("/")
                .build();
        assertEquals("10.10.12.13", request.tcpSourceAddr());
    }

    @Test
    void testGetMethod() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withMethod("GET")
                .withUri("/")
                .build();
        assertEquals("GET", request.rawMethod());
    }

    @Test
    void testGetInputStream() throws IOException {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .build();
        HttpInputStream ins = request.inputStream();
        assertEquals(0, ins.available());

        final MockAsyncRequest request1 = MockAsyncRequest.aMockRequest()
                .withBody("Restlight is good!".getBytes())
                .withUri("/")
                .build();
        assertEquals("Restlight is good!".getBytes().length, request1.inputStream().available());
    }

    @Test
    void testGetLocalAddr() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withLocalAddr("127.0.0.1")
                .withUri("/")
                .build();
        assertEquals("127.0.0.1", request.localAddr());
    }

    @Test
    void testGetLocalPort() {
        final int port = NetworkUtils.selectRandomPort();
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withLocalPort(port)
                .withUri("/")
                .build();
        assertEquals(port, request.localPort());
    }

    @Test
    void testOperateHeader() {
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("B", "X");
        headers.add("B", "Y");
        headers.add("B", "Z");

        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withHeaders(headers)
                .withHeader("A", "X")
                .withHeader("A", "Y")
                .withHeader("A", "Z").build();

        assertEquals("X", request.getHeader("B"));
        assertEquals(3, request.headers().getAll("B").size());
        assertEquals("X", request.getHeader("A"));
        assertEquals(3, request.headers().getAll("A").size());
    }

    @Test
    void testOperateAttribute() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withAttribute("A", "X")
                .withAttribute("A", "Y").build();
        assertEquals("Y", request.getAttribute("A"));

        request.removeAttribute("A");
        assertNull(request.getAttribute("A"));

        request.setAttribute("foo", "f");
        assertEquals("f", request.getAttribute("foo"));

        assertArrayEquals(new String[]{"foo"}, request.attributeNames());
    }

    @Test
    void testOperateParameter() {
        final Map<String, List<String>> params = new HashMap<>();
        final List<String> values = new ArrayList<>(3);
        values.add("X");
        values.add("Y");
        values.add("Z");
        params.put("A", values);

        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withParameters(params)
                .withParameter("B", "X")
                .withParameter("B", "Y")
                .withParameter("B", "Z").build();

        assertEquals(2, request.parameterMap().size());

        assertEquals(3, request.getParameters("A").size());
        assertEquals("X", request.getParameter("A"));

        assertEquals(3, request.getParameters("B").size());
        assertEquals("X", request.getParameter("B"));
    }

    @Test
    void testTrailer() {
        final HttpHeaders headers = new DefaultHttpHeaders();
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withTrailers(headers)
                .build();
        assertSame(headers, request.trailers());
    }

    @Test
    void testAlloc() {
        assertSame(UnpooledByteBufAllocator.DEFAULT, MockAsyncRequest.aMockRequest().build().alloc());
    }

    @Test
    void testCookies() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        assertTrue(request.cookies().isEmpty());

        final MockAsyncRequest request1 = MockAsyncRequest.aMockRequest()
                .withCookie("foo", "A")
                .withCookie(new DefaultCookie("bar", "B"))
                .build();
        assertEquals(2, request1.cookies().size());
        final Iterator<Cookie> it = request1.cookies().iterator();
        assertEquals(new DefaultCookie("bar", "B"), it.next());
        assertEquals(new DefaultCookie("foo", "A"), it.next());
    }

    @Test
    void testAsyncTimeout() {
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withAsyncTimeOut(1024L)
                .build();
        assertEquals(1024L, request.getAsyncTimeout());
    }
}
