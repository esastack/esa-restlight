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
package esa.httpserver.core;

import esa.commons.Checks;
import esa.commons.annotation.Beta;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * An interface defines a http server response.
 * <p>
 * !Note: This response is not designed as thread-safe, but it does a special committing control that only one thread is
 * allowed to commit this response, which means you can not commit this response by calling {@link #sendResult(int)}
 * method if another thread had committed this response(such as using the {@link #outputStream()} to write data).
 */
public interface AsyncResponse {

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
     * @see #sendResult(int, ByteBuf, int, int, boolean)
     */
    void sendResult(ByteBuf body, int off, int len, boolean autoRelease);

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
     * @param off         off
     * @param len         len
     * @param autoRelease is automatically release this body ByteBuf even though this method call is retuning with a
     *                    Exception
     * @throws IllegalStateException if response has committed
     */
    default void sendResult(int code, ByteBuf body, int off, int len, boolean autoRelease) {
        setStatus(code);
        sendResult(body, off, len, autoRelease);
    }

    /**
     * @see #sendResult(int, ByteBuf, int, int, boolean)
     */
    default void sendResult(int code, ByteBuf body, int off, int len) {
        sendResult(code, body, off, len, true);
    }

    /**
     * @see #sendResult(int, ByteBuf, int, int, boolean)
     */
    default void sendResult(ByteBuf body, int off, int len) {
        sendResult(body, off, len, true);
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param body body
     * @see #sendResult(int, ByteBuf, int, int)
     */
    default void sendResult(ByteBuf body, boolean autoRelease) {
        if (body == null) {
            sendResult(body, 0, 0, autoRelease);
        } else {
            sendResult(body, body.readerIndex(), body.capacity(), autoRelease);
        }
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param code code
     * @param body body
     * @see #sendResult(int, ByteBuf, int, int)
     */
    default void sendResult(int code, ByteBuf body, boolean autoRelease) {
        setStatus(code);
        sendResult(body, autoRelease);
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param code code
     * @param body body
     * @see #sendResult(int, ByteBuf, int, int)
     */
    default void sendResult(int code, ByteBuf body) {
        sendResult(code, body, true);
    }

    /**
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param body body
     * @see #sendResult(int, ByteBuf, int, int)
     */
    default void sendResult(ByteBuf body) {
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
        setHeader(HttpHeaderNames.LOCATION, newUri);
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
     * Get current ByteBufAllocator
     *
     * @return allocator
     */
    @Beta
    ByteBufAllocator alloc();

    /**
     * Return the value for the specified header, or {@code null} if this header has not been set. If more than one
     * value was added for this name, only the first is returned; use {@link #getHeaders(String)} to retrieve all of
     * them.
     *
     * @param name healder name
     * @return value
     */
    default String getHeader(String name) {
        return getHeader((CharSequence) name);
    }

    /**
     * Return the value for the specified header, or {@code null} if this header has not been set. If more than one
     * value was added for this name, only the first is returned; use {@link #getHeaders(String)} to retrieve all of
     * them.
     *
     * @param name healder name
     * @return value
     */
    String getHeader(CharSequence name);

    /**
     * Get the header names set for this HTTP response.
     *
     * @return names
     */
    Collection<String> headerNames();

    /**
     * Return a Collection of all the header values associated with the specified header name.
     *
     * @param name header name
     * @return value
     */
    Collection<String> getHeaders(CharSequence name);


    /**
     * @return value
     * @see #getHeaders(CharSequence)
     */
    default Collection<String> getHeaders(String name) {
        return getHeaders((CharSequence) name);
    }

    /**
     * Returns a boolean indicating whether the named response header has already been set.
     *
     * @param name name
     * @return is contains
     */
    boolean containsHeader(CharSequence name);

    /**
     * @see #containsHeader(CharSequence)
     */
    default boolean containsHeader(String name) {
        return containsHeader((CharSequence) name);
    }

    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple
     * values.
     *
     * @param name  header name
     * @param value value
     */
    void addHeader(CharSequence name, String value);

    /**
     * @see #addHeader(CharSequence, String)
     */
    default void addHeader(String name, String value) {
        addHeader((CharSequence) name, value);
    }

    /**
     * Sets a response header with the given name and value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  name
     * @param value value
     */
    void setHeader(CharSequence name, String value);

    /**
     * @see #setHeader(CharSequence, String)
     */
    default void setHeader(String name, String value) {
        setHeader((CharSequence) name, value);
    }

    /**
     * Sets a response headers with the given name and value.
     *
     * @param name   name
     * @param values values
     */
    void setHeaders(CharSequence name, List<String> values);

    /**
     * Sets a response headers with the given name and value.
     *
     * @param name   name
     * @param values values
     */
    default void setHeaders(String name, List<String> values) {
        setHeaders((CharSequence) name, values);
    }

    /**
     * Sets a response header with the given name and integer value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  name
     * @param value value
     */
    void setIntHeader(CharSequence name, int value);

    /**
     * @see #setIntHeader(CharSequence, int)
     */
    default void setIntHeader(String name, int value) {
        setIntHeader((CharSequence) name, value);
    }

    /**
     * Adds a response header with the given name and integer value. This method allows response headers to have
     * multiple values.
     *
     * @param name  name
     * @param value value
     */
    void addIntHeader(CharSequence name, int value);

    /**
     * @see #addIntHeader(CharSequence, int)
     */
    default void addIntHeader(String name, int value) {
        addIntHeader((CharSequence) name, value);
    }

    /**
     * Sets a response header with the given name and short value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  name
     * @param value value
     */
    void setShortHeader(CharSequence name, short value);

    /**
     * @see #setShortHeader(CharSequence, short)
     */
    default void setShortHeader(String name, short value) {
        setShortHeader((CharSequence) name, value);
    }

    /**
     * Adds a response header with the given name and short value. This method allows response headers to have multiple
     * values.
     *
     * @param name  name
     * @param value value
     */
    void addShortHeader(CharSequence name, short value);

    /**
     * @see #addShortHeader(CharSequence, short)
     */
    default void addShortHeader(String name, short value) {
        addShortHeader((CharSequence) name, value);
    }

    /**
     * Adds a trailing header with the given name and value. This method allows response headers to have multiple
     * values.
     *
     * @param name  name
     * @param value value
     */
    void addTrailer(CharSequence name, String value);

    /**
     * @see #addTrailer(CharSequence, String)
     */
    default void addTrailer(String name, String value) {
        addTrailer((CharSequence) name, value);
    }

    /**
     * Sets a trailing header with the given name and value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  name
     * @param value value
     */
    void setTrailer(CharSequence name, String value);

    /**
     * @see #setTrailer(CharSequence, String)
     */
    default void setTrailer(String name, String value) {
        setTrailer((CharSequence) name, value);
    }

    /**
     * Sets a trailing headers with the given name and value.
     *
     * @param name   name
     * @param values values
     */
    void setTrailers(CharSequence name, List<String> values);

    /**
     * @see #setTrailers(CharSequence, List)
     */
    default void setTrailers(String name, List<String> values) {
        setTrailers((CharSequence) name, values);
    }

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
     * Add a listener to this response, this listener will be called after current response has been write.
     *
     * @param listener listener
     */
    void onEnd(Consumer<AsyncResponse> listener);

    /**
     * Add a listener to this response, this listener will be called after current response has been write.
     *
     * @param endListener listener
     * @deprecated use {@link #onEnd(Consumer)}
     */
    @Deprecated
    default void addEndListener(EndListener endListener) {
        onEnd(endListener::onEnd);
    }

    @Deprecated
    interface EndListener {
        void onEnd(AsyncResponse asyncResponse);
    }

    /**
     * Get current http response code.
     *
     * @return status
     * @deprecated use {@link #status()}
     */
    @Deprecated
    default int getStatus() {
        return status();
    }

    /**
     * @return output stream
     * @deprecated use {@link #outputStream()}
     */
    @Deprecated
    default HttpOutputStream getOutputStream() {
        return outputStream();
    }

    /**
     * @deprecated use {@link #bufferSize()}
     */
    @Deprecated
    default int getBufferSize() {
        return bufferSize();
    }

    /**
     * @return names
     * @deprecated use {@link #headerNames()}
     */
    @Deprecated
    default Collection<String> getHeaderNames() {
        return headerNames();
    }
}
