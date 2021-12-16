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
import esa.commons.ClassUtils;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.httpserver.core.Response;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.core.HttpResponse;
import io.netty.buffer.ByteBuf;

import java.io.File;

public class ResponseEntityImpl extends HttpEntityImpl implements ResponseEntity {

    private final HttpResponse response;
    private final Response rsp;

    public ResponseEntityImpl(HandlerMethod handler, HttpResponse response, Response rsp) {
        super(handler, parseMediaType(response.headers().get(HttpHeaderNames.CONTENT_TYPE)));
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(rsp, "rsp");
        this.response = response;
        this.type = handler == null ? ClassUtils.getUserType(response.entity()) : handler.method().getReturnType();
        this.genericType = handler == null ? null : handler.method().getGenericReturnType();
        this.annotations = handler == null ? null : handler.method().getAnnotations();
        this.rsp = rsp;
    }

    @Override
    public void sendResult(Buffer buffer) {
        if (rsp.isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
        Object unwrapped;
        if ((unwrapped = BufferUtil.unwrap(buffer)) instanceof ByteBuf) {
            rsp.end((ByteBuf) unwrapped);
        } else {
            rsp.end(buffer.getBytes());
        }
    }

    @Override
    public void sendFile(File file) {
        if (rsp.isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
        rsp.sendFile(file);
    }

    @Override
    public HttpResponse response() {
        return this.response;
    }
}

