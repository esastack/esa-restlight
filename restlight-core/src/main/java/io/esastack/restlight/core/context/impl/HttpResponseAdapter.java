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

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.httpserver.core.HttpOutputStream;
import io.esastack.restlight.core.context.HttpResponse;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class HttpResponseAdapter implements HttpResponse {

    private final io.esastack.httpserver.core.HttpResponse underlying;

    public HttpResponseAdapter(io.esastack.httpserver.core.HttpResponse underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public void entity(Object entity) {
    }

    @Override
    public Object entity() {
        return null;
    }

    @Override
    public void outputStream(OutputStream os) {
    }

    @Override
    public HttpHeaders headers() {
        return underlying.headers();
    }

    @Override
    public HttpHeaders trailers() {
        return underlying.trailers();
    }

    @Override
    public void setStatus(int code) {
        underlying.setStatus(code);
    }

    @Override
    public int status() {
        return underlying.status();
    }

    @Override
    public boolean isKeepAlive() {
        return underlying.isKeepAlive();
    }

    @Override
    public HttpOutputStream outputStream() {
        return underlying.outputStream();
    }

    @Override
    public void setBufferSize(int size) {
        underlying.setBufferSize(size);
    }

    @Override
    public int bufferSize() {
        return underlying.bufferSize();
    }

    @Override
    public boolean isCommitted() {
        return underlying.isCommitted();
    }

    @Override
    public void reset() {
        underlying.reset();
    }

    @Override
    public void sendResult(byte[] body, int off, int len) {
        underlying.sendResult(body, off, len);
    }

    @Override
    public void sendResult(Buffer body, int len, boolean autoRelease) {
        underlying.sendResult(body, len, autoRelease);
    }

    @Override
    public void sendFile(File file, long offset, long length) {
        underlying.sendFile(file, offset, length);
    }

    @Override
    public String getHeader(CharSequence name) {
        return underlying.getHeader(name);
    }

    @Override
    public Collection<String> headerNames() {
        return underlying.headerNames();
    }

    @Override
    public Collection<String> getHeaders(CharSequence name) {
        return underlying.getHeaders(name);
    }

    @Override
    public boolean containsHeader(CharSequence name) {
        return underlying.containsHeader(name);
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        underlying.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        underlying.setHeader(name, value);
    }

    @Override
    public void setHeaders(CharSequence name, List<String> values) {
        underlying.setHeaders(name, values);
    }

    @Override
    public void setIntHeader(CharSequence name, int value) {
        underlying.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(CharSequence name, int value) {
        underlying.addIntHeader(name, value);
    }

    @Override
    public void setShortHeader(CharSequence name, short value) {
        underlying.setShortHeader(name, value);
    }

    @Override
    public void addShortHeader(CharSequence name, short value) {
        underlying.addShortHeader(name, value);
    }

    @Override
    public void addCookie(Cookie cookie) {
        underlying.addCookie(cookie);
    }

    @Override
    public void addCookie(String name, String value) {
        underlying.addCookie(name, value);
    }

    @Override
    public void onEnd(Consumer<io.esastack.httpserver.core.HttpResponse> listener) {
        underlying.onEnd(listener);
    }
}
