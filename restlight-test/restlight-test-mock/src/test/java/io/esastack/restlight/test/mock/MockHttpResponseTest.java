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
package io.esastack.restlight.test.mock;

import esa.commons.http.MimeMappings;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.httpserver.core.HttpOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHttpResponseTest {

    @Test
    void testGetStatus() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();
        response.setStatus(302);
        assertEquals(302, response.status());
    }

    @Test
    void testOperateHeader() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();
        CharSequence seq = "A";
        response.addHeader(seq, "foo");
        assertEquals("foo", response.getHeader(seq));
        response.setHeader(seq, "bar");
        assertEquals("bar", response.getHeader(seq));

        response.setHeaders(seq, Arrays.asList("foo", "bar"));
        assertArrayEquals(new String[]{"foo", "bar"}, response.getHeaders(seq).toArray());

        response.reset();

        response.addIntHeader(seq, 1);
        assertEquals("1", response.getHeader(seq));
        response.setIntHeader(seq, 1);
        assertEquals("1", response.getHeader(seq));

        response.reset();

        response.addShortHeader(seq, (short) 1);
        assertEquals("1", response.getHeader(seq));
        response.setShortHeader(seq, (short) 1);
        assertEquals("1", response.getHeader(seq));

        assertTrue(response.headerNames().contains(seq));
        assertTrue(response.containsHeader(seq));
    }

    @Test
    void testOperateCookie() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        response.addCookie(new CookieImpl("c1", "v1"));
        response.addCookie("c2", "v2");

        final Collection<String> cookies = response.getHeaders(HttpHeaderNames.SET_COOKIE);
        assertEquals(2, cookies.size());
    }

    @Test
    void testSendFile() throws IOException {
        File file = File.createTempFile("restlight-test-", ".xml");
        file.deleteOnExit();
        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write("foo".getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
            response.sendFile(file);
            assertTrue(response.isCommitted());
            assertEquals("foo", response.getSentData().string(StandardCharsets.UTF_8));
            assertEquals(MimeMappings.getMimeType(file.getName()), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
        } finally {
            file.delete();
        }
    }

    @Test
    void testGetOutputStream0() throws IOException {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();

        HttpOutputStream os = response.outputStream();
        os.write("Hello".getBytes());
        assertThrows(IllegalStateException.class, () -> response.sendResult(200, new byte[0]));
    }

    @Test
    void testGetOutputStream1() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();

        response.sendResult(200, new byte[0]);
        assertTrue(response.isCommitted());
        assertThrows(IllegalStateException.class, () -> response.outputStream().write("Hello".getBytes()));
    }

    @Test
    void testSendResult0() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();

        response.setStatus(200);
        response.sendResult(302, "Hello".getBytes(), 0, "Hello".getBytes().length);
        assertEquals(302, response.status());

        assertTrue(response.isCommitted());

        assertThrows(IllegalStateException.class,
                () -> response.sendResult(404, null, 0, true));
    }

    @Test
    void testSendResult1() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();

        response.sendResult(404, null, 0, true);
        assertEquals(404, response.status());
        assertTrue(response.isCommitted());

        assertThrows(IllegalStateException.class,
                () -> response.sendResult(302, "Hello".getBytes(), 0, "Hello".getBytes().length));
    }

    @Test
    void testCollectResult() throws IOException {
        MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();

        response.sendResult(404, null, 0, true);
        assertEquals(404, response.status());
        assertTrue(response.isCommitted());
        assertEquals(0, response.getSentData().readableBytes());

        response = MockHttpResponse.aMockResponse()
                .build();
        response.sendResult("a".getBytes(StandardCharsets.UTF_8));
        assertEquals("a", response.getSentData().string(StandardCharsets.UTF_8));

        response = MockHttpResponse.aMockResponse()
                .build();
        response.sendResult(BufferUtil.wrap(Unpooled.copiedBuffer("a".getBytes(StandardCharsets.UTF_8))));
        assertEquals("a", response.getSentData().string(StandardCharsets.UTF_8));

        response = MockHttpResponse.aMockResponse()
                .build();
        response.outputStream().writeUTF("abc");
        response.outputStream().close();
        assertEquals("abc", response.getSentData().string(StandardCharsets.UTF_8));
    }

    @Test
    void testOpsAfterCommitted() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        // end response
        response.sendResult(200);
        response.addHeader("a", "b");
        response.setHeader("x", "y");
        response.setHeaders("m", Collections.singletonList("n"));
        response.setIntHeader("int1", 1);
        response.addIntHeader("int2", 2);
        response.setShortHeader("short1", (short) 1);
        response.addShortHeader("short2", (short) 2);
    }

    @Test
    void testCallEndListener() {
        final AtomicBoolean end0 = new AtomicBoolean();
        final AtomicBoolean end1 = new AtomicBoolean();

        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .withEndListener(rsp -> end0.set(true))
                .withEndListeners(Collections.singletonList(rsp -> end1.set(true)))
                .build();
        response.callEndListener();
        assertTrue(end0.get());
        assertTrue(end1.get());
    }
}
