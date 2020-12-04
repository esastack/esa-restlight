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
package esa.httpserver.impl;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpOutputStream;
import esa.httpserver.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.AsciiString;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncResponseImplTest {

    @Test
    void testDelegate() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        when(mock.status()).thenReturn(500);
        assertEquals(500, response.status());

        response.setStatus(100);
        verify(mock).setStatus(eq(100));

        when(mock.isKeepAlive()).thenReturn(true);
        assertTrue(response.isKeepAlive());

        when(mock.alloc()).thenReturn(Unpooled.EMPTY_BUFFER.alloc());
        assertSame(Unpooled.EMPTY_BUFFER.alloc(), response.alloc());

        when(mock.toString()).thenReturn("foo");
        assertSame("foo", response.toString());

        response.addCookie(new DefaultCookie("a", "1"));
        verify(mock).addCookie(argThat(argument -> "a".equals(argument.name()) && "1".equals(argument.value())));
        response.addCookie("b", "2");
        verify(mock).addCookie(eq("b"), eq("2"));

        final HttpHeaders headers = new Http1HeadersImpl();
        when(mock.headers()).thenReturn(headers);
        final HttpHeaders trailers = new Http1HeadersImpl();
        when(mock.trailers()).thenReturn(trailers);

        response.setHeader("a", "1");
        response.setHeader(AsciiString.of("b"), "2");
        assertEquals("1", response.getHeader("a"));
        assertEquals("2", response.getHeader(AsciiString.of("b")));
        assertTrue(response.containsHeader("a"));
        assertTrue(response.containsHeader(AsciiString.of("b")));

        response.addHeader("a", "2");
        assertArrayEquals(new String[]{"1", "2"}, response.getHeaders("a").toArray(new String[0]));
        assertArrayEquals(new String[]{"a", "b"}, response.headerNames().toArray(new String[0]));
        assertArrayEquals(new String[]{"a", "b"}, response.getHeaderNames().toArray(new String[0]));


        response.setIntHeader("c", 1);
        response.setIntHeader(AsciiString.of("d"), 2);
        assertEquals("1", response.getHeader("c"));
        assertEquals("2", response.getHeader(AsciiString.of("d")));
        response.addIntHeader("c", 2);
        response.addIntHeader(AsciiString.of("d"), 3);
        assertArrayEquals(new String[]{"1", "2"}, response.getHeaders("c").toArray(new String[0]));
        assertArrayEquals(new String[]{"2", "3"}, response.getHeaders("d").toArray(new String[0]));


        response.setShortHeader("e", (short) 1);
        response.setShortHeader(AsciiString.of("f"), (short) 2);
        assertEquals("1", response.getHeader("e"));
        assertEquals("2", response.getHeader(AsciiString.of("f")));
        response.addShortHeader("e", (short) 2);
        response.addShortHeader(AsciiString.of("f"), (short) 3);
        assertArrayEquals(new String[]{"1", "2"}, response.getHeaders("e").toArray(new String[0]));
        assertArrayEquals(new String[]{"2", "3"}, response.getHeaders("f").toArray(new String[0]));

        response.setHeaders("g", Arrays.asList("1", "2"));
        response.setHeaders(AsciiString.of("h"), Arrays.asList("3", "4"));
        assertArrayEquals(new String[]{"1", "2"}, response.getHeaders("g").toArray(new String[0]));
        assertArrayEquals(new String[]{"3", "4"}, response.getHeaders("h").toArray(new String[0]));


        response.setTrailer("a", "1");
        response.setTrailer(AsciiString.of("b"), "2");
        assertEquals("1", trailers.get("a"));
        assertEquals("2", trailers.get(AsciiString.of("b")));

        response.addTrailer("a", "2");
        assertArrayEquals(new String[]{"1", "2"}, trailers.getAll("a").toArray(new String[0]));

        response.setTrailers("c", Arrays.asList("1", "2"));
        assertArrayEquals(new String[]{"1", "2"}, trailers.getAll("c").toArray(new String[0]));
    }

    @Test
    void testOutputStream() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        when(mock.isEnded()).thenReturn(true);

        assertThrows(IllegalStateException.class, response::outputStream);

        reset(mock);
        when(mock.isEnded()).thenReturn(false);
        when(mock.isCommitted()).thenReturn(true);

        assertThrows(IllegalStateException.class, response::outputStream);
        reset(mock);
        final HttpOutputStream os = initOutputStream(mock, response);
        assertSame(os, response.outputStream());
    }

    @Test
    void testSetBufferSize() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        assertEquals(AsyncResponse.DEFAULT_BUFFER_SIZE, response.bufferSize());
        response.setBufferSize(16);
        assertEquals(16, response.bufferSize());

        when(mock.isCommitted()).thenReturn(true);
        response.setBufferSize(32);
        assertEquals(16, response.bufferSize());

        reset(mock);
        when(mock.isEnded()).thenReturn(false);
        when(mock.isCommitted()).thenReturn(false);
        when(response.alloc()).thenReturn(Unpooled.EMPTY_BUFFER.alloc());
        assertNotNull(response.outputStream());

        response.setBufferSize(32);
        assertEquals(16, response.bufferSize());
    }

    @Test
    void testReset() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);


        response.setBufferSize(16);
        assertEquals(16, response.bufferSize());
        final HttpHeaders headers = new Http1HeadersImpl().set("a", "1");
        when(mock.headers()).thenReturn(headers);
        final HttpHeaders trailers = new Http1HeadersImpl().set("b", "2");
        when(mock.trailers()).thenReturn(trailers);

        response.reset();

        assertEquals(AsyncResponse.DEFAULT_BUFFER_SIZE, response.bufferSize());
        assertTrue(headers.isEmpty());
        assertTrue(trailers.isEmpty());
    }

    @Test
    void testSendResultWithByteArray() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        final ByteBuf buf = Unpooled.buffer();
        buf.writeLong(1L);
        buf.writeLong(2L);
        final byte[] body = ByteBufUtil.getBytes(buf);

        response.sendResult(body, 0, 8);
        verify(mock).end(same(body), eq(0), eq(8));

        reset(mock);

        response.sendResult(500, body, 0, 8);
        verify(mock).setStatus(eq(500));
        verify(mock).end(same(body), eq(0), eq(8));

        reset(mock);

        response.sendResult(body);
        verify(mock).end(same(body));

        reset(mock);

        response.sendResult(500, body);
        verify(mock).setStatus(eq(500));
        verify(mock).end(same(body));

        reset(mock);

        response.sendResult(500);
        verify(mock).setStatus(eq(500));
        verify(mock).end((byte[]) isNull());

        reset(mock);

        response.sendResult();
        verify(mock).end((byte[]) isNull());

        reset(mock);
        initOutputStream(mock, response);
        assertThrows(IllegalStateException.class, () -> response.sendResult(body, 0, 8));
        assertThrows(IllegalStateException.class, () -> response.sendResult(500, body, 0, 8));
        assertThrows(IllegalStateException.class, () -> response.sendResult(500, body));
        assertThrows(IllegalStateException.class, () -> response.sendResult(500));
        assertThrows(IllegalStateException.class, response::sendResult);
    }

    @Test
    void testSendResultWithByteBuf() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        final ByteBuf body = Unpooled.buffer();
        body.writeLong(1L);
        body.writeLong(2L);

        response.sendResult(body, 8, 8, true);
        verify(mock).end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)));
        assertEquals(1, body.refCnt());

        reset(mock);
        assertThrows(IndexOutOfBoundsException.class,
                () -> response.sendResult(body, -1, 8, true));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(Unpooled.EMPTY_BUFFER, 0, 0, true);
        verify(mock).end(same(Unpooled.EMPTY_BUFFER));

        reset(mock);
        response.sendResult(body, 8, 8, false);
        verify(mock).end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)));
        assertEquals(2, body.refCnt());

        reset(mock);
        assertFalse(body.release());

        when(mock.end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)))).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> response.sendResult(body, 8, 8, false));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(500, body, 8, 8, true);
        verify(mock).setStatus(eq(500));
        verify(mock).end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(500, body, 8, 8);
        verify(mock).setStatus(eq(500));
        verify(mock).end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(body, 8, 8);
        verify(mock).end(argThat((ArgumentMatcher<ByteBuf>) buf -> 0 == buf.readerIndex()
                && 8 == buf.writerIndex()
                && 2L == buf.getLong(0)));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(body, true);
        verify(mock).end(same(body));
        assertEquals(1, body.refCnt());

        when(mock.end(same(body))).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> response.sendResult(body, true));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(body, false);
        verify(mock).end(same(body));
        assertEquals(2, body.refCnt());
        assertFalse(body.release());

        reset(mock);
        response.sendResult(500, body, true);
        verify(mock).setStatus(eq(500));
        verify(mock).end(same(body));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(500, body);
        verify(mock).setStatus(eq(500));
        verify(mock).end(same(body));
        assertEquals(1, body.refCnt());

        reset(mock);
        response.sendResult(body);
        verify(mock).end(same(body));
        assertEquals(1, body.refCnt());

        reset(mock);
        initOutputStream(mock, response);
        assertThrows(IllegalStateException.class, () -> response.sendResult(body, 8, 8, true));
        assertThrows(IllegalStateException.class,
                () -> response.sendResult(500, body, 8, 8, true));
        assertThrows(IllegalStateException.class, () -> response.sendResult(body, 8, 8));
        assertThrows(IllegalStateException.class, () -> response.sendResult(body, 8, 8));
        assertThrows(IllegalStateException.class, () -> response.sendResult(body, true));
        assertThrows(IllegalStateException.class, () -> response.sendResult(500, body, true));
        assertThrows(IllegalStateException.class, () -> response.sendResult(500, body));
        assertThrows(IllegalStateException.class, () -> response.sendResult(body));
    }

    @Test
    void testSendRedirect() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        response.sendRedirect("foo");
        verify(mock).sendRedirect(eq("foo"));

        initOutputStream(mock, response);

        assertThrows(IllegalStateException.class, () -> response.sendRedirect("a"));
    }

    @Test
    void testSendFile() {
        final Response mock = mock(Response.class);
        final AsyncResponseImpl response = new AsyncResponseImpl(mock);

        final File f = new File("");
        response.sendFile(f);
        verify(mock).sendFile(same(f), eq(0L), eq(Long.MAX_VALUE));

        reset(mock);

        response.sendFile(f, 1);
        verify(mock).sendFile(same(f), eq(1L), eq(Long.MAX_VALUE));

        reset(mock);

        response.sendFile(f, 1L, 2L);
        verify(mock).sendFile(same(f), eq(1L), eq(2L));

        initOutputStream(mock, response);

        assertThrows(IllegalStateException.class, () -> response.sendFile(f));
    }

    private static HttpOutputStream initOutputStream(Response mock, AsyncResponseImpl response) {
        when(mock.isEnded()).thenReturn(false);
        when(mock.isCommitted()).thenReturn(false);
        when(response.alloc()).thenReturn(Unpooled.EMPTY_BUFFER.alloc());

        final HttpOutputStream os = response.outputStream();
        assertNotNull(os);
        return os;
    }
}
