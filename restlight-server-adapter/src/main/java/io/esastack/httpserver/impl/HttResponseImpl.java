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
package io.esastack.httpserver.impl;

import esa.commons.Checks;
import esa.commons.ExceptionUtils;
import esa.commons.annotation.Internal;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.httpserver.core.HttpOutputStream;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.httpserver.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.MathUtil;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Default implementation of {@link HttpResponse} that wraps the {@link Response} as delegate.
 */
@Internal
public class HttResponseImpl implements HttpResponse {

    private final Response res;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private HttpOutputStream os;

    public HttResponseImpl(Response res) {
        this.res = res;
    }

    @Override
    public void setStatus(int code) {
        res.setStatus(code);
    }

    @Override
    public int status() {
        return res.status();
    }

    @Override
    public boolean isKeepAlive() {
        return res.isKeepAlive();
    }

    @Override
    public HttpHeaders headers() {
        return res.headers();
    }

    @Override
    public HttpHeaders trailers() {
        return res.trailers();
    }

    @Override
    public HttpOutputStream outputStream() {
        if (res.isEnded()) {
            throw new IllegalStateException("Already ended");
        }
        if (os == null) {
            checkCommitted();
            os = new ByteBufHttpOutputStream(bufferSize, res);
        }
        return os;
    }

    @Override
    public void setBufferSize(int size) {
        if (isCommitted() || os != null) {
            return;
        }
        Checks.checkArg(size > 0, "buffer size must be over than 0. actual: " + size);
        bufferSize = size;
    }

    @Override
    public int bufferSize() {
        return bufferSize;
    }

    @Override
    public boolean isCommitted() {
        return res.isCommitted();
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
        res.setStatus(200);
        setBufferSize(DEFAULT_BUFFER_SIZE);
        res.headers().clear();
        res.trailers().clear();
    }

    @Override
    public void sendResult(byte[] body, int off, int len) {
        checkOutputStream();
        res.end(body, off, len);
    }

    @Override
    public void sendResult(byte[] body) {
        checkOutputStream();
        res.end(body);
    }

    @Override
    public void sendResult(Buffer body, int len, boolean autoRelease) {
        checkOutputStream();
        if (body == null) {
            sendByteBuf(Unpooled.EMPTY_BUFFER, len, autoRelease);
            return;
        }

        Object unwrap;
        if ((unwrap = BufferUtil.unwrap(body)) instanceof ByteBuf) {
            sendByteBuf((ByteBuf) unwrap, len, autoRelease);
            return;
        }

        if (len > body.readableBytes()) {
            throw new IndexOutOfBoundsException("You expect to read '" + len + "' byte, but only '"
                    + body.readableBytes() + "' is readable");
        }

        byte[] bytes = new byte[len];
        body.readBytes(bytes);
        sendResult(bytes);
    }

    @Override
    public void sendFile(File file, long offset, long length) {
        checkOutputStream();
        res.sendFile(file, offset, length);
    }

    @Override
    public void sendRedirect(String newUri) {
        checkOutputStream();
        res.sendRedirect(newUri);
    }

    @Override
    public String getHeader(CharSequence name) {
        return res.headers().get(name);
    }

    @Override
    public Collection<String> headerNames() {
        return res.headers().names();
    }

    @Override
    public Collection<String> getHeaders(CharSequence name) {
        return res.headers().getAll(name);
    }

    @Override
    public boolean containsHeader(CharSequence name) {
        return res.headers().contains(name);
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        res.headers().add(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        res.headers().set(name, value);
    }

    @Override
    public void setHeaders(CharSequence name, List<String> values) {
        res.headers().set(name, values);
    }

    @Override
    public void setIntHeader(CharSequence name, int value) {
        res.headers().setInt(name, value);
    }

    @Override
    public void addIntHeader(CharSequence name, int value) {
        res.headers().addInt(name, value);
    }

    @Override
    public void setShortHeader(CharSequence name, short value) {
        res.headers().setShort(name, value);
    }

    @Override
    public void addShortHeader(CharSequence name, short value) {
        res.headers().addShort(name, value);
    }

    @Override
    public void addTrailer(CharSequence name, String value) {
        res.trailers().add(name, value);
    }

    @Override
    public void setTrailer(CharSequence name, String value) {
        res.trailers().set(name, value);
    }

    @Override
    public void setTrailers(CharSequence name, List<String> values) {
        res.trailers().set(name, values);
    }

    @Override
    public void addCookie(Cookie cookie) {
        res.addCookie(cookie);
    }

    @Override
    public void addCookie(String name, String value) {
        res.addCookie(name, value);
    }

    @Override
    public void onEnd(Consumer<HttpResponse> listener) {
        res.onEndFuture().addListener(f -> listener.accept(this));
    }

    @Override
    public String toString() {
        return res.toString();
    }

    private void sendByteBuf(ByteBuf body, int len, boolean autoRelease) {
        if (body == null) {
            body = Unpooled.EMPTY_BUFFER;
        }

        int off = body.readerIndex();
        if (MathUtil.isOutOfBounds(off, len, body.capacity())) {
            throw new IndexOutOfBoundsException();
        }

        if (autoRelease) {
            res.end(body.slice(off, len));
        } else {
            try {
                res.end(body.retainedSlice(off, len));
            } catch (Exception e) {
                body.release();
                ExceptionUtils.throwException(e);
            }
        }
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
    }

    private void checkOutputStream() {
        // only one of output stream and sendXX() is allowed.
        if (os != null) {
            throw new IllegalStateException("OutputStream has already opened. use it please.");
        }
    }
}
