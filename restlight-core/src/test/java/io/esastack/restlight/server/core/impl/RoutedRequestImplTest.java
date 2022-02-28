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
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.RoutedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoutedRequestImplTest {

    private HttpRequest underlying;
    private RoutedRequest request;

    @BeforeEach
    void init() {
        this.underlying = mock(HttpRequest.class);
        this.request = new RoutedRequestImpl(underlying);
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
