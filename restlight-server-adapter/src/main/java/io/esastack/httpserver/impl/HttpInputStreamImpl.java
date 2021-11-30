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
package io.esastack.httpserver.impl;

import esa.commons.Checks;
import esa.commons.io.IOUtils;
import io.esastack.httpserver.core.HttpInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpInputStreamImpl extends HttpInputStream {

    private final InputStream underlying;
    private final DataInputStream dataIns;

    public HttpInputStreamImpl(InputStream underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
        this.dataIns = new DataInputStream(underlying);
    }

    @Override
    public int readBytes() {
        try {
            return underlying.available();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read from given InputStream", ex);
        }
    }

    @Override
    public String readString(Charset charset) {
        try {
            return IOUtils.toString(underlying);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read string from given InputStream");
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        dataIns.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        dataIns.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return dataIns.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataIns.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return dataIns.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataIns.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return dataIns.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return dataIns.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return dataIns.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return dataIns.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return dataIns.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return dataIns.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return dataIns.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return dataIns.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return dataIns.readUTF();
    }

    @Override
    public int read() throws IOException {
        return dataIns.read();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(underlying);
        IOUtils.closeQuietly(dataIns);
    }
}

