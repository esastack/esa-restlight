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
package io.esastack.restlight.core.context.impl;

import io.esastack.commons.net.buffer.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByteBufHttpInputStreamTest {

    @Test
    void testReadBytes() throws IOException {
        final ByteBuf buf = Unpooled.copiedBuffer("hello world".getBytes());

        final ByteBufHttpInputStream in = new ByteBufHttpInputStream(BufferUtil.wrap(buf), false);
        assertEquals(buf.readableBytes(), in.available());

        final byte[] bytes = new byte[8];
        final byte[] dest = new byte[8];
        assertEquals(8, in.read(bytes, 0, 8));
        buf.getBytes(0, dest);
        assertArrayEquals(dest, bytes);
        assertEquals(8, in.readBytes());
    }

    @Test
    void testReadFully() throws IOException {
        final ByteBuf buf = Unpooled.copiedBuffer("hello world".getBytes());
        final ByteBufHttpInputStream in = new ByteBufHttpInputStream(BufferUtil.wrap(buf), false);
        final byte[] bytes = new byte[8];
        final byte[] dest = new byte[8];
        assertEquals("hello world", in.readString(StandardCharsets.US_ASCII));
        in.readFully(bytes, 0, 8);
        buf.getBytes(0, dest);
        assertArrayEquals(dest, bytes);
    }

    @Test
    void testReadSingle() throws IOException {
        assertTrue(new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyBoolean(true)), false)
                .readBoolean());
        assertEquals(1, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copiedBuffer(new byte[]{1})),
                false).readByte());

        assertEquals(255,
                new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copiedBuffer(new byte[]{-1})),
                        false).readUnsignedByte());


        assertEquals(1, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyShort(1)),
                false).readShort());

        assertEquals(65535,
                new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyShort(-1)), false)
                        .readUnsignedShort());

        assertEquals(1, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.buffer().writeChar(1)),
                false).readChar());

        assertEquals(1, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyInt(1)),
                false).readInt());

        assertEquals(1L, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyLong(1L)),
                false).readLong());

        assertEquals(1.0F, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyFloat(1.0F)),
                false).readFloat());

        assertEquals(1.0D, new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copyDouble(1.0D)),
                false).readDouble());

        assertEquals("hello",
                new ByteBufHttpInputStream(BufferUtil.wrap(Unpooled.copiedBuffer("hello\r\nworld".getBytes())),
                        false).readLine());

        final ByteBufOutputStream out = new ByteBufOutputStream(Unpooled.buffer());
        out.writeUTF("hello");
        assertEquals("hello",
                new ByteBufHttpInputStream(BufferUtil.wrap(out.buffer()),
                        false)
                        .readUTF());
    }

    @Test
    void testSkip() throws IOException {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeShort(1);
        buf.writeInt(2);
        final ByteBufHttpInputStream in = new ByteBufHttpInputStream(BufferUtil.wrap(buf), false);
        assertEquals(2, in.skip(2));
        assertEquals(2, in.readInt());
    }

    @Test
    void testMarkAndReset() throws IOException {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeShort(1);
        buf.writeInt(2);
        final ByteBufHttpInputStream in = new ByteBufHttpInputStream(BufferUtil.wrap(buf), false);
        assertTrue(in.markSupported());
        in.mark(8);
        in.readInt();
        in.reset();
        assertEquals(1, in.readShort());
    }

    @Test
    void testCloseAndRelease() throws IOException {
        final ByteBuf buf = Unpooled.buffer();
        final ByteBufHttpInputStream in = new ByteBufHttpInputStream(BufferUtil.wrap(buf), false);
        in.close();
        assertEquals(1, buf.refCnt());
        new ByteBufHttpInputStream(BufferUtil.wrap(buf), true).close();
        assertEquals(0, buf.refCnt());
    }
}
