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
import esa.commons.io.IOUtils;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.impl.HttpInputStreamImpl;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.io.InputStream;

public class RequestEntityImpl extends HttpEntityImpl implements RequestEntity {

    private final HttpRequest request;
    private Buffer body;
    private InputStream ins;

    public RequestEntityImpl(Param param, RequestContext context) {
        super(context.request().contentType());
        Checks.checkNotNull(param, "param");
        Checks.checkNotNull(context, "context");
        this.request = context.request();
        type(param.type());
        genericType(param.genericType());
        annotations(param.annotations());
        context.response().onEnd(rsp -> this.safeRelease());
    }

    @Override
    public Buffer body() {
        if (body == null) {
            return request.body();
        } else {
            return body;
        }
    }

    @Override
    public HttpInputStream inputStream() {
        if (ins != null) {
            return new HttpInputStreamImpl(this.ins);
        } else {
            return request.inputStream();
        }
    }

    @Override
    public RequestEntity body(Buffer data) {
        this.body = data;
        return this;
    }

    @Override
    public RequestEntity inputStream(InputStream ins) {
        IOUtils.closeQuietly(this.ins);
        this.ins = ins;
        return this;
    }

    private void safeRelease() {
        if (ins != null) {
            IOUtils.closeQuietly(this.ins);
        }
        if (body != null) {
            Object underlying = BufferUtil.unwrap(body);
            if (underlying instanceof ByteBuf) {
                ReferenceCountUtil.safeRelease(this.ins);
            }
        }
    }
}

