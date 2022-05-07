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

import io.esastack.restlight.core.context.HttpOutputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpOutputStreamImplTest {

    @Test
    void testWrite() throws IOException {
        AtomicInteger writeCount = new AtomicInteger(0);
        OutputStream underlying = new OutputStream() {
            @Override
            public void write(int b) {
                writeCount.incrementAndGet();
            }
        };

        final HttpOutputStream out = new HttpOutputStreamImpl(underlying);

        out.write(1);
        assertEquals(1, writeCount.get());
        writeCount.set(0);

        final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        out.write(bytes, 0, 8);
        assertEquals(8, writeCount.get());
        writeCount.set(0);

        out.writeBoolean(true);
        assertEquals(1, writeCount.get());
        writeCount.set(0);

        out.writeByte(1);
        assertEquals(1, writeCount.get());
        writeCount.set(0);

        out.writeShort(2);
        assertEquals(2, writeCount.get());
        writeCount.set(0);

        out.writeChar(3);
        assertEquals(2, writeCount.get());
        writeCount.set(0);

        out.writeLong(5L);
        assertEquals(8, writeCount.get());
        writeCount.set(0);

        out.writeFloat(6.0f);
        assertEquals(4, writeCount.get());
        writeCount.set(0);

        out.writeInt(6);
        assertEquals(4, writeCount.get());
        writeCount.set(0);

        out.writeDouble(7.0D);
        assertEquals(8, writeCount.get());
        writeCount.set(0);

        out.writeChars("hello");
        assertEquals(10, writeCount.get());
        writeCount.set(0);

        out.writeUTF("foo");
        assertEquals(5, writeCount.get());
        writeCount.set(0);

        out.close();
        assertTrue(out.isClosed());
    }
}
