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
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.impl.HttpInputStreamImpl;
import io.esastack.httpserver.impl.HttpRequestProxy;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;

import java.io.InputStream;

public class RequestEntityImpl extends HttpEntityImpl implements RequestEntity {

    private final HttpRequest request;
    private final Param param;
    private byte[] byteData;
    private InputStream ins;

    public RequestEntityImpl(HandlerMethod handler, Param param, HttpRequest request) {
        super(handler, request.contentType());
        Checks.checkNotNull(handler, "handler");
        Checks.checkNotNull(param, "param");
        Checks.checkNotNull(request, "request");
        this.request = request;
        this.param = param;
        this.type = param.type();
        this.genericType = param.genericType();
        this.annotations = param.annotations();
    }

    @Override
    public byte[] byteData() {
        if (byteData == null) {
            return request.body().getBytes();
        } else {
            return byteData;
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
    public void byteData(byte[] data) {
        this.byteData = data;
    }

    @Override
    public void inputStream(InputStream ins) {
        IOUtils.closeQuietly(this.ins);
        this.ins = ins;
    }

    @Override
    public Param param() {
        return param;
    }

    @Override
    public HttpRequest request() {
        if (ins != null) {
            return new HttpRequestProxy(request) {
                @Override
                public HttpInputStream inputStream() {
                    return new HttpInputStreamImpl(ins);
                }
            };
        }
        return request;
    }
}

