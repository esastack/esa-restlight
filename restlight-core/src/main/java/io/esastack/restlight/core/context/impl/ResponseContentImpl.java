/*
 * Copyright 2022 OPPO ESA Stack Project
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

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.httpserver.core.Response;
import io.esastack.restlight.core.context.ResponseContent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;

public class ResponseContentImpl implements ResponseContent {

    private final Response response;

    public ResponseContentImpl(Response response) {
        Checks.checkNotNull(response, "response");
        this.response = response;
    }

    @Override
    public void write(byte[] data) {
        response.write(data);
    }

    @Override
    public void end(byte[] data) {
        response.end(data);
    }

    @Override
    public void write(Buffer buffer) {
        if (buffer == null) {
            return;
        }
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            response.write((ByteBuf) unwrap);
            return;
        }
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        write(data);
    }

    @Override
    public void end(Buffer buffer) {
        if (buffer == null) {
            return;
        }
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            response.end((ByteBuf) unwrap);
            return;
        }
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        end(data);
    }

    @Override
    public void end(File file) {
        response.sendFile(file);
        response.end();
    }

    @Override
    public void end() {
        response.end();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public boolean isEnded() {
        return response.isEnded();
    }

    @Override
    public ByteBufAllocator alloc() {
        return response.alloc();
    }
}

