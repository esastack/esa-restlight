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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.restlight.server.core.HttpInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.charset.Charset;

public class ByteBufHttpInputStream extends HttpInputStream {

    private final ByteBufInputStream delegate;
    private final ByteBuf byteBuf;

    public ByteBufHttpInputStream(Buffer buffer, boolean releaseOnClose) {
        this.byteBuf = toByteBuf(buffer);
        this.delegate = new ByteBufInputStream(byteBuf, releaseOnClose);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        delegate.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        delegate.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return delegate.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return delegate.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return delegate.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return delegate.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return delegate.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return delegate.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return delegate.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return delegate.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return delegate.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return delegate.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return delegate.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return delegate.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return delegate.readUTF();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public String readString(Charset charset) {
        return byteBuf.toString(charset);
    }

    public int readBytes() {
        return delegate.readBytes();
    }

    static ByteBuf toByteBuf(Buffer buffer) {
        Object underlying = BufferUtil.unwrap(buffer);
        if (underlying instanceof ByteBuf) {
            return (ByteBuf) underlying;
        } else {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return Unpooled.wrappedBuffer(bytes);
        }
    }
}
