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
package io.esastack.restlight.core.resolver;

import io.esastack.httpserver.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResponseEntityChannelImplTest {

    @Test
    void testWrite() {
        final Response response = mock(Response.class);
        when(response.alloc()).thenReturn(Unpooled.EMPTY_BUFFER.alloc());
        final ResponseEntityChannelImpl.ByteBufHttpOutputStream out =
                new ResponseEntityChannelImpl.ByteBufHttpOutputStream(8, response);
        final ByteBuf buf = Unpooled.buffer();
        final ByteBuf write = Unpooled.buffer();

        when(response.write(any(ByteBuf.class))).then(invocation -> {
            final Object arg = invocation.getArgument(0);
            if (arg instanceof ByteBuf) {
                write.writeBytes((ByteBuf) arg);
            }
            return mock(ChannelFuture.class);
        });
        out.write(1);
        buf.writeByte(1);
        final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};

        out.write(bytes, 0, 8);
        buf.writeBytes(bytes);
        verify(response).write(any(ByteBuf.class));

        out.writeBoolean(true);
        buf.writeBoolean(true);
        out.writeByte(1);
        buf.writeByte(1);
        out.writeShort(2);
        buf.writeShort(2);
        out.writeChar(3);
        buf.writeChar(3);
        out.writeInt(4);
        buf.writeInt(4);
        out.writeLong(5L);
        buf.writeLong(5L);
        out.writeFloat(6.0f);
        buf.writeFloat(6.0f);
        out.writeDouble(7.0D);
        buf.writeDouble(7.0D);
        out.writeBytes("hello world");
        ByteBufUtil.writeAscii(buf, "hello world");
        out.writeChars("hello http server");
        for (char c : "hello http server".toCharArray()) {
            buf.writeChar(c);
        }
        out.writeUTF("foo");
        ByteBufUtil.writeUtf8(buf, "foo");

        out.close();
        assertArrayEquals(ByteBufUtil.getBytes(buf), ByteBufUtil.getBytes(write));

        assertThrows(IllegalStateException.class, () -> out.write(1));
        assertThrows(IllegalStateException.class, () -> out.write(bytes, 0, 8));
        assertThrows(IllegalStateException.class, () -> out.writeBoolean(true));
        assertThrows(IllegalStateException.class, () -> out.writeByte(1));
        assertThrows(IllegalStateException.class, () -> out.writeShort(2));
        assertThrows(IllegalStateException.class, () -> out.writeChar(3));
        assertThrows(IllegalStateException.class, () -> out.writeInt(4));
        assertThrows(IllegalStateException.class, () -> out.writeLong(5L));
        assertThrows(IllegalStateException.class, () -> out.writeFloat(6.0f));
        assertThrows(IllegalStateException.class, () -> out.writeFloat(6.0f));
        assertThrows(IllegalStateException.class, () -> out.writeDouble(7.0D));
        assertThrows(IllegalStateException.class, () -> out.writeBytes("hello world"));
        assertThrows(IllegalStateException.class, () -> out.writeChars("hello http server"));
        assertThrows(IllegalStateException.class, () -> out.writeUTF("foo"));

        out.close();
        assertTrue(out.isClosed());
    }

    @Test
    void testWriteError() {
        final Response response = mock(Response.class);
        final AtomicReference<ByteBuf> buffer = new AtomicReference<>();
        final ByteBufAllocator alloc = mock(ByteBufAllocator.class);
        when(alloc.buffer(anyInt(), anyInt())).thenAnswer(invocation -> {
            int initCapacity = invocation.getArgument(0);
            int maxCapacity = invocation.getArgument(1);
            buffer.set(Unpooled.buffer(initCapacity, maxCapacity));
            return buffer.get();
        });
        when(response.alloc()).thenReturn(alloc);
        final ResponseEntityChannelImpl.ByteBufHttpOutputStream out =
                new ResponseEntityChannelImpl.ByteBufHttpOutputStream(8, response);

        final AtomicReference<ByteBuf> written = new AtomicReference<>();
        when(response.write(any(ByteBuf.class))).then(invocation -> {
            final Object arg = invocation.getArgument(0);
            if (arg instanceof ByteBuf) {
                written.set((ByteBuf) arg);
            }
            throw new IllegalStateException();
        });

        assertThrows(IllegalStateException.class, () -> out.write(new byte[10]));

        assertNotNull(written.get());
        assertEquals(0, written.get().refCnt());
        assertEquals(1, buffer.get().refCnt());

        out.close();
        assertEquals(0, buffer.get().refCnt());
    }

}

