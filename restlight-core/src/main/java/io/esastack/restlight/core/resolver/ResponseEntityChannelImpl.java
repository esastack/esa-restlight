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

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.httpserver.core.Response;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.netty.buffer.ByteBuf;

import java.io.File;

public class ResponseEntityChannelImpl implements ResponseEntityChannel {

    protected final Response response;

    public ResponseEntityChannelImpl(RequestContext context) {
        Checks.checkNotNull(context, "context");
        this.response = Checks.checkNotNull(context.attr(RequestContextImpl.UNDERLYING_RESPONSE).get(),
                "response");
    }

    @Override
    public void write(byte[] data) {
        response.write(data);
    }

    @Override
    public void write(Buffer buffer) {
        if (buffer == null) {
            return;
        }
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            response.write((ByteBuf) unwrap);
        }
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        write(data);
    }

    @Override
    public void writeThenEnd(byte[] data) {
        response.end(data);
    }

    @Override
    public void writeThenEnd(Buffer buffer) {
        if (buffer == null) {
            end();
            return;
        }
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            response.end((ByteBuf) unwrap);
        }
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        writeThenEnd(data);
    }

    @Override
    public void writeThenEnd(File file) {
        response.sendFile(file);
    }

    @Override
    public void end() {
        response.end();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

}

