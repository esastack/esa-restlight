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
import io.esastack.httpserver.core.HttpOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class HttpOutputStreamImpl extends HttpOutputStream {

    private final OutputStream underlying;
    private final DataOutputStream dataOs;

    private volatile int closed;

    private static final AtomicIntegerFieldUpdater<HttpOutputStreamImpl> CLOSED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(HttpOutputStreamImpl.class, "closed");

    public HttpOutputStreamImpl(OutputStream underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
        this.dataOs = new DataOutputStream(underlying);
    }

    @Override
    public boolean isClosed() {
        return CLOSED_UPDATER.get(this) == 1;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        dataOs.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        dataOs.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        dataOs.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        dataOs.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        dataOs.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        dataOs.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        dataOs.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        dataOs.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        dataOs.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        dataOs.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        dataOs.writeUTF(s);
    }

    @Override
    public void write(int b) throws IOException {
        underlying.write(b);
    }

    @Override
    public void close() throws IOException {
        if (!CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            return;
        }
        IOUtils.closeQuietly(underlying);
        IOUtils.closeQuietly(dataOs);
    }
}

