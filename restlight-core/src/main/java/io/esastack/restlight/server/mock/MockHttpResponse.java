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
package io.esastack.restlight.server.mock;

import esa.commons.Checks;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.server.core.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MockHttpResponse implements HttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(MockHttpResponse.class);
    private final HttpHeaders headers = new Http1HeadersImpl();
    private final HttpHeaders trailingHeaders = new Http1HeadersImpl();
    private final Buffer content;
    private int status = 200;
    private Object entity;

    private MockHttpResponse(Buffer content) {
        this.content = content;
    }

    public static Builder aMockResponse(Buffer content) {
        Checks.checkNotNull(content, "content");
        return new Builder(content);
    }

    public static Builder aMockResponse() {
        return aMockResponse(Buffer.defaultAlloc().empty());
    }

    @Override
    public void addCookie(Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, cookie.encode(true));
    }

    @Override
    public void addCookie(String name, String value) {
        addCookie(new CookieImpl(name, value));
    }

    @Override
    public int status() {
        return this.status;
    }

    @Override
    public void status(int code) {
        this.status = code;
    }

    @Override
    public void entity(Object entity) {
        this.entity = entity;
    }

    @Override
    public Object entity() {
        return this.entity;
    }

    @Override
    public boolean isKeepAlive() {
        return false;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public HttpHeaders trailers() {
        return this.trailingHeaders;
    }

    public Buffer content() {
        return content;
    }

    private void reset0() {
        status = 200;
        headers.clear();
        trailingHeaders.clear();
    }

    public static final class Builder {

        private final Buffer content;

        private Builder(Buffer content) {
            this.content = content;
        }

        public MockHttpResponse build() {
            return new MockHttpResponse(content);
        }
    }

}
