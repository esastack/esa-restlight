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
package io.esastack.restlight.server.core.impl;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.server.core.FilteringRequest;
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilteringRequestImplTest {

    private HttpRequest underlying;
    private FilteringRequest request;

    @BeforeEach
    void init() {
        this.underlying = mock(HttpRequest.class);
        this.request = new FilteringRequestImpl(underlying);
    }

    @Test
    void testMethod() {
        when(underlying.method()).thenReturn(HttpMethod.POST);
        assertEquals(HttpMethod.POST, request.method());
        request.method(HttpMethod.GET);
        assertEquals(HttpMethod.GET, request.method());
        assertEquals(HttpMethod.POST, underlying.method());
    }

    @Test
    void testUri() {
        String origin = "http://origin/123/123?aaa=bbb";
        String changed = "http://origin/456/456?aaa=ccc";
        when(underlying.uri()).thenReturn(origin);
        assertEquals(origin, request.uri());
        request.uri(changed);
        assertEquals(changed, request.uri());
        assertEquals(origin, underlying.uri());
        assertEquals("/456/456", request.path());
        assertEquals("aaa=ccc", request.query());
        assertEquals("ccc", request.paramsMap().get("aaa").get(0));
    }

    @Test
    void testBody() {
        Buffer origin = mock(Buffer.class);
        Buffer changed = mock(Buffer.class);
        when(underlying.body()).thenReturn(origin);
        assertEquals(origin, request.body());
        request.body(changed);
        assertEquals(changed, request.body());
        assertEquals(origin, underlying.body());
        byte[] data = new byte[1];
        request.body(data);
        assertEquals(1, request.body().readableBytes());
        assertEquals(origin, underlying.body());
    }

    @Test
    void testInputStream() {
        HttpInputStream origin = mock(HttpInputStream.class);
        HttpInputStream changed = mock(HttpInputStream.class);
        when(underlying.inputStream()).thenReturn(origin);
        assertEquals(origin, request.inputStream());
        request.inputStream(changed);
        assertTrue(request.inputStream() instanceof HttpInputStreamImpl);
        assertEquals(origin, underlying.inputStream());
    }

}
