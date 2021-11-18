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
package io.esastack.restlight.core.context.impl;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.impl.HttpInputStreamImpl;
import io.esastack.httpserver.impl.HttpRequestProxy;
import io.esastack.restlight.core.context.RoutedRequest;

import java.io.InputStream;

public class RoutedRequestImpl extends HttpRequestProxy implements RoutedRequest {

    private Buffer bufferBody;
    private InputStream ins;

    public RoutedRequestImpl(HttpRequest underlying) {
        super(underlying);
    }

    @Override
    public void body(byte[] body) {
        this.bufferBody = BufferUtil.buffer(body);
    }

    @Override
    public void body(Buffer bufferBody) {
        this.bufferBody = bufferBody;
    }

    @Override
    public Buffer bufferBody() {
        if (bufferBody == null) {
            return super.bufferBody();
        } else {
            return bufferBody;
        }
    }

    @Override
    public void inputStream(HttpInputStream ins) {
        this.ins = ins;
    }

    @Override
    public HttpInputStream inputStream() {
        if (ins == null) {
            return super.inputStream();
        } else {
            return new HttpInputStreamImpl(ins);
        }
    }
}

