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
package esa.restlight.test.mock;

import esa.commons.Checks;
import esa.commons.ExceptionUtils;
import esa.commons.StringUtils;
import esa.commons.http.MimeMappings;
import esa.commons.io.IOUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.commons.netty.http.Http1HeadersAdaptor;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.internal.MathUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;

public class MockAsyncResponse implements AsyncResponse {

    private static final Logger logger = LoggerFactory.getLogger(MockAsyncResponse.class);

    private static final AtomicIntegerFieldUpdater<MockAsyncResponse> COMMITTED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(MockAsyncResponse.class, "committed");
    private int status = 200;
    private int bufferSize = AsyncResponse.DEFAULT_BUFFER_SIZE;
    private MockHttpOutputStream os;
    private List<Consumer<AsyncResponse>> endListeners = new ArrayList<>(1);
    private HttpHeaders headers = new DefaultHttpHeaders();
    private HttpHeaders trailingHeaders = new DefaultHttpHeaders();
    private volatile int committed;
    final ByteBuf result = Unpooled.buffer();

    public static Builder aMockResponse() {
        return new Builder();
    }

    public ByteBuf getSentData() {
        return result;
    }

    @Override
    public String getHeader(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public void addHeader(CharSequence headerName, String value) {
        if (StringUtils.isEmpty(headerName) || value == null || isCommitted()) {
            return;
        }
        headers.add(headerName, value);
    }

    @Override
    public void setHeader(CharSequence headerName, String value) {
        if (StringUtils.isEmpty(headerName) || value == null || isCommitted()) {
            return;
        }
        headers.set(headerName, value);
    }

    @Override
    public void setHeaders(CharSequence headerName, List<String> values) {
        if (StringUtils.isEmpty(headerName) || values == null || isCommitted()) {
            return;
        }
        headers.set(headerName, values);
    }

    @Override
    public void setIntHeader(CharSequence name, int value) {
        if (StringUtils.isEmpty(name) || isCommitted()) {
            return;
        }
        headers.setInt(name, value);
    }

    @Override
    public void addIntHeader(CharSequence name, int value) {
        if (StringUtils.isEmpty(name) || isCommitted()) {
            return;
        }
        headers.addInt(name, value);
    }

    @Override
    public void setShortHeader(CharSequence name, short value) {
        if (StringUtils.isEmpty(name) || isCommitted()) {
            return;
        }
        headers.setShort(name, value);
    }

    @Override
    public void addShortHeader(CharSequence name, short value) {
        if (StringUtils.isEmpty(name) || isCommitted()) {
            return;
        }
        headers.addShort(name, value);
    }

    @Override
    public Collection<String> headerNames() {
        return headers.names();
    }

    @Override
    public Collection<String> getHeaders(CharSequence name) {
        return headers.getAll(name);
    }

    @Override
    public boolean containsHeader(CharSequence name) {
        return headers.contains(name);
    }

    @Override
    public void addTrailer(CharSequence headerName, String value) {
        if (StringUtils.isEmpty(headerName) || value == null || isCommitted()) {
            return;
        }
        trailingHeaders.add(headerName, value);
    }

    @Override
    public void setTrailer(CharSequence headerName, String value) {
        if (StringUtils.isEmpty(headerName) || value == null || isCommitted()) {
            return;
        }
        trailingHeaders.set(headerName, value);
    }

    @Override
    public void setTrailers(CharSequence name, List<String> values) {
        if (StringUtils.isEmpty(name) || values == null || isCommitted()) {
            return;
        }
        trailingHeaders.set(name, values);
    }

    @Override
    public void addCookie(Cookie cookie) {
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    @Override
    public void addCookie(String name, String value) {
        addCookie(new DefaultCookie(name, value));
    }

    @Override
    public int status() {
        return this.status;
    }

    @Override
    public void setStatus(int code) {
        if (isCommitted()) {
            return;
        }
        this.status = code;
    }

    @Override
    public boolean isKeepAlive() {
        return false;
    }

    @Override
    public HttpOutputStream outputStream() {
        if (os == null) {
            os = new MockHttpOutputStream(this);
        }
        return os;
    }

    @Override
    public int bufferSize() {
        return this.bufferSize;
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public boolean isCommitted() {
        return committed == 1;
    }

    @Override
    public void reset() {
        checkCommitted();
        reset0();
    }

    @Override
    public void sendResult(byte[] body, int off, int len) {
        // only one of output stream and sendXX() is allowed.
        if (os != null) {
            throw new IllegalStateException("OutputStream has already opened. use it please.");
        }
        if (body != null) {
            if (MathUtil.isOutOfBounds(off, len, body.length)) {
                throw new IndexOutOfBoundsException();
            }
        }
        ensureCommittedExclusively();
        if (body != null) {
            result.writeBytes(body, off, len);
        }
        // Note: Nothing to write
        callEndListener();
    }

    @Override
    public void sendResult(ByteBuf body, int off, int len, boolean autoRelease) {
        boolean release = autoRelease;
        try {
            // only one of output stream and sendXX() is allowed.
            if (os != null) {
                throw new IllegalStateException("OutputStream has already opened. use it please.");
            }
            if (body == null) {
                len = 0;
            } else {
                int max = body.readerIndex() + body.readableBytes();
                if ((off < 0) || (off < body.readerIndex()) || (off > max) || (len < 0) ||
                        ((off + len) > max) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                }
            }
            ensureCommittedExclusively();

            if (len != 0) {
                if (off == body.readerIndex() && len == body.readableBytes()) {
                    if (release) {
                        // body ByteBuf will be release automatically in Outbound handler
                        // we do not need do the retain it
                        release = false;
                        result.writeBytes(body, off, len);
                        body.skipBytes(len);
                        body.release();
                    } else {
                        result.writeBytes(body, off, len);
                        body.skipBytes(len);
                    }
                } else {
                    if (release) {
                        // body ByteBuf will be release automatically in Outbound handler
                        // we do not need do the retain it
                        result.writeBytes(body, off, len);
                        body.skipBytes(len);
                        body.release();
                        release = false;
                    } else {
                        result.writeBytes(body, off, len);
                        body.skipBytes(len);
                    }
                }
            }
            callEndListener();
        } finally {
            if (release && body != null) {
                body.release();
            }
        }
    }

    @Override
    public void sendFile(File file, long offset, long length) {
        Checks.checkNotNull(file, "file");
        Checks.checkArg(offset >= 0L, "negative offset");
        Checks.checkArg(length >= 0L, "negative length");

        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            ExceptionUtils.throwException(new FileNotFoundException(file.getName()));
        }
        ensureCommittedExclusively();
        if (!headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            String contentType = MimeMappings.getMimeTypeOrDefault(file.getPath());
            headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        }
        long len = Math.min(length, file.length() - offset);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, len);
        try {
            result.writeBytes(IOUtils.toByteArray(file));
        } catch (IOException e) {
            ExceptionUtils.throwException(e);
        }
    }

    @Override
    public ByteBufAllocator alloc() {
        return UnpooledByteBufAllocator.DEFAULT;
    }


    @Override
    public void onEnd(Consumer<AsyncResponse> listener) {
        if (isCommitted()) {
            return;
        }
        endListeners.add(listener);
    }

    public esa.commons.http.HttpHeaders trailers() {
        return new Http1HeadersAdaptor(this.trailingHeaders);
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
    }

    private void reset0() {
        status = 200;
        bufferSize = AsyncResponse.DEFAULT_BUFFER_SIZE;
        headers.clear();
        trailingHeaders.clear();
        endListeners.clear();
        IOUtils.closeQuietly(os);
        os = null;
    }

    boolean casSetCommitted() {
        return COMMITTED_UPDATER.compareAndSet(this, 0, 1);
    }

    private void ensureCommittedExclusively() {
        if (!casSetCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
    }

    void callEndListener() {
        for (Consumer<AsyncResponse> endListener : endListeners) {
            try {
                endListener.accept(this);
            } catch (Throwable e) {
                logger.error("Error while calling end listener: " + endListener, e);
            }
        }
    }

    public static final class Builder {
        private List<Consumer<AsyncResponse>> endListeners = new ArrayList<>();

        private Builder() {
        }

        public Builder withEndListeners(List<Consumer<AsyncResponse>> endListeners) {
            Checks.checkNotNull(endListeners, "endListeners");
            for (Consumer<AsyncResponse> listener : endListeners) {
                withEndListener(listener);
            }
            return this;
        }

        public Builder withEndListener(Consumer<AsyncResponse> endListener) {
            Checks.checkNotNull(endListener, "endListener");
            this.endListeners.add(endListener);
            return this;
        }

        public MockAsyncResponse build() {
            MockAsyncResponse mockAsyncResponse = new MockAsyncResponse();
            endListeners.forEach(mockAsyncResponse::onEnd);
            return mockAsyncResponse;
        }
    }

}
