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
package io.esastack.httpserver.core;

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.function.Consumer;

/**
 * An interface defines a http server response.
 * <p>
 * !Note: This response is not designed as thread-safe, but it does a special committing control that only one thread is
 * allowed to commit this response, which means you can not commit this response by calling {@link #sendResult(int)}
 * method if another thread had committed this response(such as using the {@link #outputStream()} to write data).
 */
public interface HttpResponse {

    /**
     * Default buffer size of output stream. It means that the chunk size of current response is also equals this
     * value.
     */
    int DEFAULT_BUFFER_SIZE = 4 * 1024;

    /**
     * Set the response code
     *
     * @param code code
     */
    void setStatus(int code);

    /**
     * Get current http response code.
     *
     * @return status
     */
    int status();

    /**
     * Is current request should be keepAlive mode.
     *
     * @return keepalive
     */
    boolean isKeepAlive();

    /**
     * Get output stream of this response. Create a new implementation of HttpOutputStream if it is {@code null}
     * <p>
     * Note: Once you have called this function you should use this output stream to write your response data rather
     * than using other method such as sendResult(...)
     *
     * @return output stream
     */
    HttpOutputStream outputStream();

    /**
     * Set buffer size of the output stream. it will not be effective unless current output stream is null, which means
     * you'd better call this function before {@link #outputStream()}
     * <p>
     * advice: always set the buffer size if you have already known the length or the predication of the response data.
     *
     * @param size size
     */
    void setBufferSize(int size);

    /**
     * Get the buffer size of the output stream.
     *
     * @return size
     */
    int bufferSize();

    /**
     * Obtains the headers.
     *
     * @return  headers.
     */
    HttpHeaders headers();

    /**
     * Write the byte data.
     * <p>
     * Note: Once you have called this function you should use this method call to write your response data rather than
     * using other method such as {@link #outputStream()} sendResult(...)}
     * <p>
     * this function can be called only once. This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param body byte data
     * @param off  off
     * @param len  len
     * @throws IllegalStateException if response has committed
     */
    void sendResult(byte[] body, int off, int len);

    /**
     * @throws IllegalStateException if response has committed
     * @see #sendResult(byte[], int, int)
     */
    default void sendResult(int code, byte[] body, int off, int len) {
        setStatus(code);
        sendResult(body, off, len);
    }

    /**
     * write response
     *
     * @see #sendResult(int, byte[], int, int)
     */
    default void sendResult(byte[] body) {
        sendResult(body, 0, body == null ? 0 : body.length);
    }

    /**
     * @see #sendResult(int, byte[], int, int)
     */
    default void sendResult(int code, byte[] body) {
        setStatus(code);
        sendResult(body);
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param code code
     * @see #sendResult(int, byte[], int, int)
     */
    default void sendResult(int code) {
        sendResult(code, (byte[]) null);
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @see #sendResult(int, byte[], int, int)
     */
    default void sendResult() {
        sendResult((byte[]) null);
    }

    /**
     * Reads and sends given {@code length} data from the given {@code body} and release the
     * underlying {@link io.netty.buffer.ByteBuf} if the {@link Buffer} is wrapped by {@link io.netty.buffer.ByteBuf}.
     *
     * @param body  body
     * @param len   length to read, if the len greater body.readableBytes(), only the
     * @param autoRelease   auto release the underlying {@link io.netty.buffer.ByteBuf} or not.
     */
    void sendResult(Buffer body, int len, boolean autoRelease);

    /**
     * Write the ByteBuf data.
     * <p>
     * Note: Once you have called this function you should use this method call to write your response data rather than
     * using other method such as {@link #outputStream()} sendResult(...)}
     * <p>
     * this function can be called only once. This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param code        code
     * @param body        byte data
     * @param len         len
     * @param autoRelease is automatically release this body ByteBuf even though this method call is retuning with a
     *                    Exception
     * @throws IllegalStateException if response has committed
     */
    default void sendResult(int code, Buffer body, int len, boolean autoRelease) {
        setStatus(code);
        sendResult(body, len, autoRelease);
    }

    /**
     * Reads and sends all data in the given {@code body} and release the underlying {@link io.netty.buffer.ByteBuf}
     * if the {@link Buffer} is wrapped by {@link io.netty.buffer.ByteBuf}.
     *
     * @param body body
     * @param autoRelease auto release the underlying {@link io.netty.buffer.ByteBuf} or not.
     * @see #sendResult(Buffer, int, boolean)
     */
    default void sendResult(Buffer body, boolean autoRelease) {
        if (body == null) {
            sendResult(null, 0, autoRelease);
        } else {
            sendResult(body, body.capacity(), autoRelease);
        }
    }

    /**
     * Reads and sends all data in the given {@code body} and release the underlying {@link io.netty.buffer.ByteBuf}
     * if the {@link Buffer} is wrapped by {@link io.netty.buffer.ByteBuf}.
     *
     * @param code code
     * @param body body
     * @param autoRelease auto release the underlying {@link io.netty.buffer.ByteBuf} or not.
     * @see #sendResult(int, Buffer, boolean)
     */
    default void sendResult(int code, Buffer body, boolean autoRelease) {
        setStatus(code);
        sendResult(body, autoRelease);
    }

    /**
     * Reads and sends all data in the given {@code body} and release the underlying {@link io.netty.buffer.ByteBuf}
     * if the {@link Buffer} is wrapped by {@link io.netty.buffer.ByteBuf}.
     *
     * @param code code
     * @param body body
     * @see #sendResult(int, Buffer)
     */
    default void sendResult(int code, Buffer body) {
        sendResult(code, body, true);
    }

    /**
     * Reads and sends all data in the given {@code body} and release the underlying {@link io.netty.buffer.ByteBuf}
     * if the {@link Buffer} is wrapped by {@link io.netty.buffer.ByteBuf}.
     *
     * @param body body
     * @see #sendResult(Buffer, boolean)
     */
    default void sendResult(Buffer body) {
        sendResult(body, true);
    }

    /**
     * Sends a temporary redirect response to the client using the given redirect uri.
     * <p>
     * If the new url is relative without a leading '/' it will be regarded as a relative path to the current request
     * URI. otherwise it will be regarded as a relative path to root path.
     *
     * @param newUri target uri
     */
    default void sendRedirect(String newUri) {
        Checks.checkNotEmptyArg(newUri);
        headers().set(HttpHeaderNames.LOCATION, newUri);
        sendResult(HttpResponseStatus.FOUND.code());
    }

    /**
     * @see #sendFile(File, long, long)
     */
    default void sendFile(File file) {
        sendFile(file, 0L);
    }

    /**
     * @see #sendFile(File, long, long)
     */
    default void sendFile(File file, long offset) {
        sendFile(file, offset, Long.MAX_VALUE);
    }

    /**
     * Sends a file to client.
     * <p>
     * Note that this is an asynchronous method but we do not provides any promise or callback for user.
     *
     * @param file   target file to send.
     * @param offset start index to send
     * @param length length.
     */
    void sendFile(File file, long offset, long length);

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     */
    void addCookie(Cookie cookie);

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param name  cookie name
     * @param value value
     */
    void addCookie(String name, String value);

    /**
     * Obtains the trailer headers.
     *
     * @return  trailers.
     */
    HttpHeaders trailers();

    /**
     * Is current response has been write.
     *
     * @return isCommitted
     */
    boolean isCommitted();

    /**
     * Reset this response to origin status, which means the values you have changed will be reset such headers, status
     * and so on.
     */
    void reset();

    /**
     * Add a listener to this response, this listener will be called after current response has been write.
     *
     * @param listener listener
     */
    void onEnd(Consumer<HttpResponse> listener);

}
