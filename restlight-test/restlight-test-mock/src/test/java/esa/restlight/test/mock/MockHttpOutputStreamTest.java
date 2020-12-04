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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockHttpOutputStreamTest {

    @Test
    void testWrite() {
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final MockHttpOutputStream out = new MockHttpOutputStream(response);
        assertFalse(out.isClosed());

        ByteBuf buf = Unpooled.buffer();

        out.write(1);
        buf.writeByte(1);


        out.write(new byte[]{1, 2, 3}, 0, 3);
        buf.writeBytes(new byte[]{1, 2, 3}, 0, 3);

        out.writeBoolean(true);
        buf.writeBoolean(true);

        out.writeByte(1);
        buf.writeByte(1);

        out.writeShort(1);
        buf.writeShort(1);

        out.writeChar(1);
        buf.writeChar(1);

        out.writeInt(1);
        buf.writeInt(1);

        out.writeLong(1L);
        buf.writeLong(1L);

        out.writeFloat(1.0f);
        buf.writeFloat(1.0f);

        out.writeDouble(1.0D);
        buf.writeDouble(1.0D);

        out.writeBytes("foo");
        ByteBufUtil.writeAscii(buf, "foo");

        out.writeChars("bar");
        "bar".chars().forEach(buf::writeChar);

        out.writeUTF("baz");
        ByteBufUtil.writeUtf8(buf, "baz");

        out.flush();
        out.close();

        assertTrue(out.isClosed());
        assertTrue(response.isCommitted());
        assertArrayEquals(ByteBufUtil.getBytes(buf), ByteBufUtil.getBytes(response.getSentData()));

        assertThrows(IllegalStateException.class, () -> out.write(1));
        assertThrows(IllegalStateException.class, () -> out.write(new byte[]{1, 2, 3}, 0, 3));
        assertThrows(IllegalStateException.class, () -> out.writeBoolean(true));
        assertThrows(IllegalStateException.class, () -> out.writeByte(1));
        assertThrows(IllegalStateException.class, () -> out.writeShort(1));
        assertThrows(IllegalStateException.class, () -> out.writeChar(1));
        assertThrows(IllegalStateException.class, () -> out.writeInt(1));
        assertThrows(IllegalStateException.class, () -> out.writeLong(1L));
        assertThrows(IllegalStateException.class, () -> out.writeDouble(1D));
        assertThrows(IllegalStateException.class, () -> out.writeFloat(1f));
        assertThrows(IllegalStateException.class, () -> out.writeBytes("foo"));
        assertThrows(IllegalStateException.class, () -> out.writeChars("bar"));
        assertThrows(IllegalStateException.class, () -> out.writeUTF("baz"));
    }

}
