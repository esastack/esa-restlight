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
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequestProxy implements HttpRequest {

    private final HttpRequest underlying;

    public HttpRequestProxy(HttpRequest underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public Object getAttribute(String name) {
        return underlying.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        underlying.setAttribute(name, value);
    }

    @Override
    public Object removeAttribute(String name) {
        return underlying.removeAttribute(name);
    }

    @Override
    public String[] attributeNames() {
        return underlying.attributeNames();
    }

    @Override
    public HttpVersion httpVersion() {
        return underlying.httpVersion();
    }

    @Override
    public String scheme() {
        return underlying.scheme();
    }

    @Override
    public String uri() {
        return underlying.uri();
    }

    @Override
    public String path() {
        return underlying.path();
    }

    @Override
    public String query() {
        return underlying.query();
    }

    @Override
    public HttpMethod method() {
        return underlying.method();
    }

    @Override
    public HttpInputStream inputStream() {
        return underlying.inputStream();
    }

    @Override
    public byte[] body() {
        return underlying.body();
    }

    @Override
    public Buffer bufferBody() {
        return underlying.bufferBody();
    }

    @Override
    public String remoteAddr() {
        return underlying.remoteAddr();
    }

    @Override
    public String tcpSourceAddr() {
        return underlying.tcpSourceAddr();
    }

    @Override
    public int remotePort() {
        return underlying.remotePort();
    }

    @Override
    public String localAddr() {
        return underlying.localAddr();
    }

    @Override
    public int localPort() {
        return underlying.localPort();
    }

    @Override
    public Map<String, List<String>> parameterMap() {
        return underlying.parameterMap();
    }

    @Override
    public HttpHeaders headers() {
        return underlying.headers();
    }

    @Override
    public MediaType contentType() {
        return underlying.contentType();
    }

    @Override
    public List<MediaType> accepts() {
        return underlying.accepts();
    }

    @Override
    public HttpHeaders trailers() {
        return underlying.trailers();
    }

    @Override
    public Set<Cookie> cookies() {
        return underlying.cookies();
    }

    @Override
    public Object alloc() {
        return underlying.alloc();
    }
}

