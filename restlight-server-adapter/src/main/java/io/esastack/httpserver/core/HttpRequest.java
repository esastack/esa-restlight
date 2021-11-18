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

import esa.commons.StringUtils;
import esa.commons.annotation.Beta;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface defines a http server request.
 */
public interface HttpRequest extends Attributes {

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
    byte[] body();

    /**
     * Get {@link Buffer} result of http body.
     *
     * @return body
     */
    Buffer bufferBody();

    /**
     * Body length.
     *
     * @return len
     */
    default long contentLength() {
        String strContentLength = getHeader(HttpHeaderNames.CONTENT_LENGTH);
        if (StringUtils.isNotEmpty(strContentLength)) {
            try {
                return Long.parseLong(strContentLength);
            } catch (Throwable ignore) {
                return bufferBody().readableBytes();
            }
        }

        return bufferBody().readableBytes();
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
     * Get header of "content-type".
     *
     * @return content-type
     */
    MediaType contentType();

    /**
     * Obtains {@link MediaType}s parsed from "accept" header.
     *
     * @return mediaTypes
     */
    List<MediaType> accepts();

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
     * Return the size(byte) of all the header names and values.
     *
     * @return size
     * @deprecated compute by yourself please
     */
    @Deprecated
    default int headerSize() {
        int size = 0;
        Iterator<Map.Entry<CharSequence, CharSequence>> iterator = headers().iteratorCharSequence();
        while (iterator.hasNext()) {
            Map.Entry<CharSequence, CharSequence> item = iterator.next();
            CharSequence headerName = item.getKey();
            CharSequence headerValue = item.getValue();
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
     * Get current allocator, if the {@link Buffer} is wrapped by {@link ByteBuf}, the {@link ByteBufAllocator}
     * to allocate {@link ByteBuf} will be returned, otherwise the {@code null} will be returned.
     *
     * @return allocator if the {@link Buffer} is wrapped by {@link ByteBuf}, otherwise {@code null}.
     */
    @Beta
    Object alloc();
}
