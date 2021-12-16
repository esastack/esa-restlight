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
package io.esastack.restlight.server.core.impl;

import io.esastack.commons.net.http.CookieUtil;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpserver.core.Response;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpResponseImplTest {

    @Test
    void testDelegate() {
        final Response mock = mock(Response.class);
        final HttResponseImpl response = new HttResponseImpl(mock);

        when(mock.status()).thenReturn(500);
        assertEquals(500, response.status());

        response.status(100);
        verify(mock).setStatus(eq(100));

        when(mock.isKeepAlive()).thenReturn(true);
        assertTrue(response.isKeepAlive());

        when(mock.alloc()).thenReturn(Unpooled.EMPTY_BUFFER.alloc());

        when(mock.toString()).thenReturn("foo");
        assertSame("foo", response.toString());

        response.addCookie(CookieUtil.wrap(new DefaultCookie("a", "1")));
        verify(mock).addCookie(argThat(argument -> "a".equals(argument.name()) && "1".equals(argument.value())));
        response.addCookie("b", "2");
        verify(mock).addCookie(eq("b"), eq("2"));

        final HttpHeaders headers = new Http1HeadersImpl();
        when(mock.headers()).thenReturn(headers);
        final HttpHeaders trailers = new Http1HeadersImpl();
        when(mock.trailers()).thenReturn(trailers);
        assertSame(headers, response.headers());
        assertSame(trailers, response.trailers());
    }

    @Test
    void testReset() {
        final Response mock = mock(Response.class);
        final HttResponseImpl response = new HttResponseImpl(mock);

        final HttpHeaders headers = new Http1HeadersImpl().set("a", "1");
        when(mock.headers()).thenReturn(headers);
        final HttpHeaders trailers = new Http1HeadersImpl().set("b", "2");
        when(mock.trailers()).thenReturn(trailers);

        response.reset();

        assertTrue(headers.isEmpty());
        assertTrue(trailers.isEmpty());
    }

    @Test
    void testSendRedirect() {
        final HttpHeaders headers = new Http1HeadersImpl();
        final Response mock = mock(Response.class);
        when(mock.headers()).thenReturn(headers);
        final HttResponseImpl response = new HttResponseImpl(mock);

        response.sendRedirect("foo");
        verify(mock).setStatus(HttpStatus.FOUND.code());
        assertEquals(1, headers.size());
        assertEquals(headers.get(HttpHeaderNames.LOCATION), "foo");
    }
}
