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

import esa.httpserver.core.HttpOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.MathUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

class MockHttpOutputStream extends HttpOutputStream {

    private final MockAsyncResponse response;
    private final ByteBuf buffer;
    private final AtomicBoolean closed;

    MockHttpOutputStream(MockAsyncResponse response) {
        this.response = response;
        this.buffer = Unpooled.buffer();
        this.closed = new AtomicBoolean(response.isCommitted());
    }

    @Override
    public void write(int b) {
        checkCloseState();
        ensureSpace(1);
        buffer.writeByte(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException("b");
        }
        if (MathUtil.isOutOfBounds(off, len, b.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        checkCloseState();
        write0(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) {
        checkCloseState();
        ensureSpace(1);
        buffer.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) {
        checkCloseState();
        ensureSpace(1);
        buffer.writeByte(v);
    }

    @Override
    public void writeShort(int v) {
        checkCloseState();
        ensureSpace(2);
        buffer.writeShort(v);
    }

    @Override
    public void writeChar(int v) {
        checkCloseState();
        ensureSpace(2);
        buffer.writeChar(v);
    }

    @Override
    public void writeInt(int v) {
        checkCloseState();
        ensureSpace(4);
        buffer.writeInt(v);
    }

    @Override
    public void writeLong(long v) {
        checkCloseState();
        ensureSpace(8);
        buffer.writeLong(v);
    }

    @Override
    public void writeFloat(float v) {
        checkCloseState();
        ensureSpace(4);
        buffer.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) {
        checkCloseState();
        ensureSpace(8);
        buffer.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) {
        checkNullAndCloseState(s);
        int len = s.length();
        int writable;
        int off = 0;
        while ((writable = buffer.maxWritableBytes()) < len) {
            writeAscii0(s, off, writable);
            len -= writable;
            off += writable;
            flush(false);
        }

        if (len > 0) {
            writeAscii0(s, off, len);
        }
    }

    @Override
    public void writeChars(String s) {
        checkNullAndCloseState(s);
        int len = s.length();
        int writable;
        int off = 0;
        //Sets the specified 2-byte UTF-16 character
        //The 16 high-order bits of the specified value are ignored.
        while ((writable = buffer.maxWritableBytes() / 2) < len) {
            writeChars0(s, off, writable);
            len -= writable;
            off += writable;
            flush(false);
        }

        if (len > 0) {
            writeChars0(s, off, len);
        }
    }

    @Override
    public void writeUTF(String s) {
        checkNullAndCloseState(s);
        int len = ByteBufUtil.utf8MaxBytes(s);
        if (len > buffer.maxCapacity()) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            write0(bytes, 0, bytes.length);
        } else {
            ensureSpace(len);
            buffer.writeCharSequence(s, CharsetUtil.UTF_8);
        }
    }

    @Override
    public void flush() {
        if (isClosed()) {
            return;
        }
        flush(false);
    }

    private void flush(boolean isLast) {
        if (buffer.readableBytes() == 0) {
            if (isLast) {
                buffer.release();
            }
            return;
        }
        response.casSetCommitted();
        response.result.writeBytes(buffer);
        if (isLast) {
            buffer.release();
        } else {
            buffer.clear();
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        flush(true);
        response.callEndListener();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    private void checkNullAndCloseState(String s) {
        if (s == null) {
            throw new NullPointerException("s");
        }
        checkCloseState();
    }

    private void checkCloseState() {
        if (isClosed()) {
            throw new IllegalStateException("Output stream already closed");
        }
    }

    private void write0(byte[] b, int off, int len) {
        // if current buf's writable space is less than len
        // write in the left space of the current buf and flush it
        int writable;
        while ((writable = buffer.maxWritableBytes()) < len) {
            buffer.writeBytes(b, off, writable);
            len -= writable;
            off += writable;
            flush(false);
        }

        if (len > 0) {
            buffer.writeBytes(b, off, len);
        }
    }

    private void ensureSpace(int space) {
        if (buffer.maxWritableBytes() < space && buffer.capacity() > 0) {
            flush(false);
        }
    }

    private void writeAscii0(CharSequence seq, int off, int len) {
        int max = off + len;
        for (int i = off; i < max; i++) {
            buffer.writeByte(AsciiString.c2b(seq.charAt(i)));
        }
    }

    private void writeChars0(CharSequence seq, int off, int len) {
        int max = off + len;
        for (int i = off; i < max; i++) {
            buffer.writeChar(seq.charAt(i));
        }
    }
}
