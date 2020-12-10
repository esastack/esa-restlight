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

import esa.commons.annotation.Beta;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface defines a http server request.
 */
public interface AsyncRequest {

    /**
     * Protocol string, such as HTTP/1.1
     *
     * @return http protocol
     */
    default String protocol() {
        return httpVersion().text();
    }

    /**
     * HttpVersion, such as HTTP/1.1
     *
     * @return version
     */
    HttpVersion httpVersion();

    /**
     * HTTP or HTTPS
     *
     * @return scheme
     */
    String scheme();

    /**
     * Http full url. For example '/foo/bar?baz=qux'.
     *
     * @return url
     */
    String uri();

    /**
     * Http path except parameters. For example path of uri '/foo/bar?baz=qux' is '/foo/bar'.
     *
     * @return uri
     */
    String path();

    /**
     * Returns the query part of the uri. For example query of uri '/foo/bar?baz=qux' is 'baz=qux'.
     *
     * @return query string
     */
    String query();

    /**
     * HTTP methodPathMatcher
     *
     * @return method
     */
    HttpMethod method();

    /**
     * HTTP method as String type.
     *
     * @return method
     */
    default String rawMethod() {
        return method().name();
    }

    /**
     * Get input stream
     *
     * @return input stream
     */
    HttpInputStream inputStream();

    /**
     * get body
     *
     * @return body bytes
     */
    default byte[] body() {
        return ByteBufUtil.getBytes(byteBufBody());
    }

    /**
     * Get ByteBuf result of http body.
     *
     * @return body
     */
    ByteBuf byteBufBody();

    /**
     * Body length.
     *
     * @return len
     */
    default int contentLength() {
        return byteBufBody().readableBytes();
    }

    /**
     * Returns the Internet Protocol (IP) address of the client or last proxy that sent the request.
     *
     * @return addr
     */
    String remoteAddr();

    /**
     * Returns the last proxy that sent the request.
     *
     * @return addr
     */
    String tcpSourceAddr();

    /**
     * Returns the Internet Protocol (PORT) address of the client or last proxy that sent the request.
     *
     * @return port or -1
     */
    int remotePort();

    /**
     * Returns the Internet Protocol (IP) address of the interface on which the request was received.
     *
     * @return addr
     */
    String localAddr();

    /**
     * Returns the port of the interface on which the request was received.
     *
     * @return port
     */
    int localPort();

    /**
     * Get parameter This pair of parameter can be from url parameters or body k-v values when Content-Type equals to
     * 'x-www-form-urlencoded'
     *
     * @param parName parameter name
     * @return value
     */
    default String getParameter(String parName) {
        final List<String> params = getParameters(parName);
        if (params != null && params.size() > 0) {
            return params.get(0);
        }
        return null;
    }

    /**
     * Get parameters This pair of parameter can be from url parameters or body k-v values when Content-Type equals to
     * 'x-www-form-urlencoded'
     *
     * @param parName parameter name
     * @return value
     */
    default List<String> getParameters(String parName) {
        return parameterMap().get(parName);
    }

    /**
     * Get parameter map This pair of parameters can be from url parameters or body k-v values when Content-Type equals
     * to 'x-www-form-urlencoded'
     *
     * @return map
     */
    Map<String, List<String>> parameterMap();

    /**
     * Get http headers
     *
     * @return headers
     */
    HttpHeaders headers();

    /**
     * Returns a boolean indicating whether the named response header has already been set.
     *
     * @param name name
     * @return is contains
     */
    default boolean containsHeader(CharSequence name) {
        return headers().contains(name);
    }

    /**
     * @see #containsHeader(CharSequence)
     */
    default boolean containsHeader(String name) {
        return containsHeader((CharSequence) name);
    }

    /**
     * Returns the value of a header with the specified name. If there are more than one values for the specified name,
     * the first value is returned.
     *
     * @param name The name of the header to search
     * @return The first header value or {@code null} if there is no such header
     */
    default String getHeader(CharSequence name) {
        return headers().get(name);
    }

    /**
     * @see #getHeader(CharSequence)
     */
    default String getHeader(String name) {
        return getHeader((CharSequence) name);
    }

    /**
     * Returns the integer value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first header value if the header is found and its value is an integer. {@code null} if there's no
     * such header or its value is not an integer.
     */
    default Integer getIntHeader(CharSequence name) {
        return headers().getInt(name);
    }

    /**
     * @see #getIntHeader(CharSequence)
     */
    default Integer getIntHeader(String name) {
        return getIntHeader((CharSequence) name);
    }

    /**
     * Returns the integer value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first header value if the header is found and its value is an integer. {@code null} if there's no
     * such header or its value is not an integer.
     */
    default Short getShortHeader(CharSequence name) {
        return headers().getShort(name);
    }

    /**
     * @see #getShortHeader(CharSequence)
     */
    default Short getShortHeader(String name) {
        return getShortHeader((CharSequence) name);
    }

    /**
     * Return the size(byte) of all the header names and values.
     *
     * @return size
     * @deprecated compute by yourself please
     */
    @Deprecated
    default int headerSize() {
        int size = 0;
        for (Map.Entry<String, String> header : headers().entries()) {
            String headerName = header.getKey();
            String headerValue = header.getValue();
            if (headerName == null) {
                continue;
            }
            size += headerName.length();
            if (headerValue != null) {
                size += headerValue.length();
            }
        }
        return size;
    }

    /**
     * Gets the trailing headers of this request.
     *
     * @return trailing headers
     */
    HttpHeaders trailers();

    /**
     * Returns a boolean indicating whether the named response header has already been set.
     *
     * @param name name
     * @return is contains
     */
    default boolean containsTrailer(CharSequence name) {
        return trailers().contains(name);
    }

    /**
     * @see #containsHeader(CharSequence)
     */
    default boolean containsTrailer(String name) {
        return containsTrailer((CharSequence) name);
    }

    /**
     * Returns the value of a trailing header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name The name of the trailing header to search
     * @return The first trailing header value or {@code null} if there is no such trailing header
     */
    default String getTrailer(CharSequence name) {
        return trailers().get(name);
    }

    /**
     * @see #getTrailer(CharSequence)
     */
    default String getTrailer(String name) {
        return getTrailer((CharSequence) name);
    }

    /**
     * Returns the integer value of a trailing header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the trailing header to search
     * @return the first trailing header value if the trailing header is found and its value is an integer. {@code null}
     * if there's no such trailing header or its value is not an integer.
     */
    default Integer getIntTrailer(CharSequence name) {
        return trailers().getInt(name);
    }

    /**
     * @see #getIntTrailer(CharSequence)
     */
    default Integer getIntTrailer(String name) {
        return getIntTrailer((CharSequence) name);
    }

    /**
     * Returns the integer value of a trailing header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first trailing header value if the trailing header is found and its value is an integer. {@code null}
     * if there's no such trailing header or its value is not an integer.
     */
    default Short getShortTrailer(CharSequence name) {
        return trailers().getShort(name);
    }

    /**
     * @see #getShortTrailer(CharSequence)
     */
    default Short getShortTrailer(String name) {
        return getShortTrailer((CharSequence) name);
    }

    /**
     * Returns a set containing all of the {@link Cookie} objects the client sent with this request.
     *
     * @return all of the {@link Cookie} objects the client sent with this request, returns an empty set if no cookies
     * were sent.
     */
    Set<Cookie> cookies();

    /**
     * Gets the {@link Cookie} with given name.
     *
     * @param name cookie name
     * @return cookie or {@code null} if did not find.
     */
    default Cookie getCookie(String name) {
        Set<Cookie> cookies = cookies();
        for (Cookie cookie : cookies) {
            if (cookie.name().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Returns the value of the named attribute as an Object, or{@code null} if no attribute of the given name exists.
     *
     * @param name name
     * @return value
     */
    Object getAttribute(String name);

    /**
     * Stores an attribute in this request. Attributes are reset between requests.
     *
     * @param name  name
     * @param value value
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the value of the named attribute as an Object and cast to target type, or{@code null} if no attribute of
     * the given name exists.
     *
     * @param name name
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T getUncheckedAttribute(String name) {
        return (T) getAttribute(name);
    }

    /**
     * Removes an attribute from this request. This method is not generally needed as attributes only persist as long as
     * the request is being handled.
     *
     * @param name name
     * @return value
     */
    Object removeAttribute(String name);

    /**
     * Removes an attribute from this request and cast to target type. This method is not generally needed as attributes
     * only persist as long as the request is being handled.
     *
     * @param name name
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T removeUncheckedAttribute(String name) {
        return (T) removeAttribute(name);
    }

    /**
     * Check whether the given attribute is present in current request.
     *
     * @param name name
     * @return {@code true} if the value of given attribute name is present, otherwise {@code false}
     */
    default boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    /**
     * Returns an {@code String[]} containing the names of the attributes available to this request
     *
     * @return names
     */
    String[] attributeNames();

    /**
     * Get current allocator.
     *
     * @return allocator
     */
    @Beta
    ByteBufAllocator alloc();

    /**
     * @deprecated use {@link #protocol()}
     */
    @Deprecated
    default String getProtocol() {
        return httpVersion().text();
    }

    /**
     * @deprecated use {@link #scheme()}
     */
    @Deprecated
    default String getScheme() {
        return scheme();
    }

    /**
     * @deprecated use {@link #path()}
     */
    @Deprecated
    default String getRequestURI() {
        return path();
    }

    /**
     * @deprecated use {@link #uri()}
     */
    @Deprecated
    default String getURIAndQueryString() {
        return uri();
    }

    /**
     * @deprecated use {@link #rawMethod()}
     */
    @Deprecated
    default String getMethod() {
        return rawMethod();
    }

    /**
     * @deprecated use {@link #headers()}
     */
    @Deprecated
    default HttpHeaders getHttpHeaders() {
        return headers();
    }

    /**
     * @deprecated use {@link #inputStream()}
     */
    @Deprecated
    default HttpInputStream getInputStream() {
        return inputStream();
    }

    /**
     * @deprecated use {@link #body()}
     */
    @Deprecated
    default byte[] getBody() {
        return ByteBufUtil.getBytes(getBodyByteBuf());
    }

    /**
     * @deprecated use {@link #byteBufBody()}
     */
    @Deprecated
    default ByteBuf getBodyByteBuf() {
        return byteBufBody();
    }

    /**
     * @deprecated use {@link #contentLength()}
     */
    @Deprecated
    default int getContentLength() {
        return contentLength();
    }

    /**
     * @return map
     * @deprecated use {@link #parameterMap()}
     */
    @Deprecated
    default Map<String, List<String>> getParameterMap() {
        return parameterMap();
    }

    /**
     * @deprecated use {@link #cookies()}
     */
    @Deprecated
    default Set<Cookie> getCookies() {
        return cookies();
    }

    /**
     * @deprecated use {@link #remoteAddr()}
     */
    @Deprecated
    default String getRemoteAddr() {
        return remoteAddr();
    }

    /**
     * @deprecated use {@link #tcpSourceAddr()}
     */
    @Deprecated
    default String getTcpSourceAddr() {
        return tcpSourceAddr();
    }

    /**
     * @deprecated use {@link #localPort()}
     */
    @Deprecated
    default int getLocalPort() {
        return localPort();
    }

    /**
     * @deprecated use {@link #headerSize()}
     */
    @Deprecated
    default int getHeaderSize() {
        return headerSize();
    }

    /**
     * @deprecated use {@link #localAddr()}
     */
    @Deprecated
    default String getLocalAddr() {
        return localAddr();
    }

    /**
     * @deprecated use {@link #attributeNames()}
     */
    @Deprecated
    default String[] getAttributeNames() {
        return attributeNames();
    }

    /**
     * Sets a header with the specified name and value.
     * <p>
     * If there is an existing header with the same name, it is removed.
     *
     * @param value The value of the header being set
     * @deprecated header should not been modified in a request
     */
    @Deprecated
    default void setHeader(String name, String value) {
        setHeader((CharSequence) name, value);
    }

    /**
     * Sets a header with the specified name and value.
     * <p>
     * If there is an existing header with the same name, it is removed.
     *
     * @param value The value of the header being set
     * @deprecated header should not been modified in a request
     */
    @Deprecated
    default void setHeader(CharSequence name, String value) {
        headers().set(name, value);
    }

    /**
     * Adds a new header with the specified name and value.
     *
     * @param name  name
     * @param value value
     * @deprecated header should not been modified in a request
     */
    @Deprecated
    default void addHeader(String name, String value) {
        addHeader((CharSequence) name, value);
    }

    /**
     * Adds a new header with the specified name and value.
     *
     * @param name  name
     * @param value value
     * @deprecated header should not been modified in a request
     */
    @Deprecated
    default void addHeader(CharSequence name, String value) {
        headers().add(name, value);
    }
}
