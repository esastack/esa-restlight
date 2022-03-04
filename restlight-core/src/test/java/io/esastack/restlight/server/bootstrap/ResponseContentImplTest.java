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
package io.esastack.restlight.server.bootstrap;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.httpserver.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.SucceededFuture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ResponseContentImplTest {

    @Test
    void testWrite() {
        final Response response = mock(Response.class);
        final ResponseContent content = new ResponseContentImpl(response);
        final byte[] data = new byte[1];
        final Future<Void> future = new SucceededFuture<>(null, null);
        when(response.write(data)).thenReturn(future);
        content.write(data);
        verify(response, Mockito.times(1)).write(data);
    }

    @Test
    void testWriteBuffer() {
        final Response response = mock(Response.class);
        final ResponseContent content = new ResponseContentImpl(response);

        //write null
        content.write((Buffer) null);
        verify(response, Mockito.times(0)).write((ByteBuf) null);

        //write buffer without ByteBuf
        final Buffer buffer = mock(Buffer.class);
        final Future<Void> future = new SucceededFuture<>(null, null);
        when(response.write((byte[]) any())).thenReturn(future);
        content.write(buffer);
        verify(response, Mockito.times(1)).write((byte[]) any());

        //can,t test write buffer with ByteBuf,because unwrap() is a private method.
    }

    @Test
    void testSendFile() {
        final Response response = mock(Response.class);
        final ResponseContent content = new ResponseContentImpl(response);
        final Future<Void> future = new SucceededFuture<>(null, null);
        final File file = mock(File.class);

        when(response.sendFile(file)).thenReturn(future);
        when(response.end()).thenReturn(future);
        content.writeThenEnd(file);
        verify(response, Mockito.times(1)).sendFile(file);
        verify(response, Mockito.times(1)).end();
    }

    @Test
    void testEndAndCommitted() {
        final Response response = mock(Response.class);
        final ResponseContent content = new ResponseContentImpl(response);

        content.end();
        verify(response, Mockito.times(2)).end();

        when(response.isCommitted()).thenReturn(true);
        assertTrue(content.isCommitted());

        when(response.isEnded()).thenReturn(true);
        assertTrue(content.isEnded());
    }

    @Test
    void testAlloc() {
        final Response response = mock(Response.class);
        final ResponseContent content = new ResponseContentImpl(response);

        ByteBufAllocator allocator = mock(ByteBufAllocator.class);
        when(response.alloc()).thenReturn(allocator);
        assertEquals(content.alloc(), allocator);
    }

}
