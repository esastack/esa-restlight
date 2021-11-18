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

import esa.commons.NetworkUtils;
import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.CookieUtil;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpserver.core.Attributes;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.Request;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link HttpRequest} that wraps the {@link Request} as delegate.
 */
@Internal
public class HttpRequestImpl implements io.esastack.httpserver.core.HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestImpl.class);

    private static final HttpVersion HTTP2 = HttpVersion.HTTP_2;

    private final Attributes attributes;
    private final Request req;
    private final HttpMethod method;
    private final HttpHeaders headers;
    private final HttpHeaders trailers;
    private Map<String, List<String>> params;
    private HttpInputStream is;
    private Set<Cookie> cookies;

    public HttpRequestImpl(Request req, Attributes attributes) {
        this.req = req;
        this.method = HttpMethod.valueOf(req.rawMethod());
        this.headers = req.headers();
        this.trailers = req.aggregated().trailers();
        this.attributes = attributes;
    }

    @Override
    public HttpVersion httpVersion() {
        if (req.version() == HttpVersion.HTTP_1_1) {
            return HttpVersion.HTTP_1_1;
        } else if (req.version() == HttpVersion.HTTP_1_0) {
            return HttpVersion.HTTP_1_0;
        }
        return HTTP2;
    }

    @Override
    public String scheme() {
        return req.scheme();
    }

    @Override
    public String uri() {
        return req.uri();
    }

    @Override
    public String path() {
        return req.path();
    }

    @Override
    public String query() {
        return req.query();
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public MediaType contentType() {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (StringUtils.isEmpty(contentType)) {
            return null;
        }

        MediaType mediaType = null;
        try {
            mediaType = MediaTypeUtil.valueOf(contentType);
        } catch (Exception e) {
            logger.warn("Error while parsing content type: " + contentType, e);
        }

        return mediaType;
    }

    @Override
    public List<MediaType> accepts() {
        final List<MediaType> accepts = new LinkedList<>();
        MediaTypeUtil.valuesOf(headers.get(HttpHeaderNames.ACCEPT), accepts);
        return accepts;
    }

    @Override
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public byte[] body() {
        Buffer bufferBody = bufferBody();
        if (bufferBody.readableBytes() == 0) {
            return new byte[0];
        } else {
            byte[] body = new byte[bufferBody.readableBytes()];
            bufferBody.copy().readBytes(body);
            return body;
        }
    }

    @Override
    public Buffer bufferBody() {
        return BufferUtil.wrap(req.aggregated().body());
    }

    @Override
    public HttpInputStream inputStream() {
        if (is == null) {
            this.is = new ByteBufHttpInputStream(bufferBody(), false);
        }
        return is;
    }

    @Override
    public String remoteAddr() {
        return getHostAddress(req.remoteAddress());
    }

    @Override
    public String tcpSourceAddr() {
        return getHostAddress(req.tcpSourceAddress());
    }

    @Override
    public int remotePort() {
        return NetworkUtils.getPort(req.remoteAddress());
    }

    @Override
    public String localAddr() {
        return getHostAddress(req.localAddress());
    }

    @Override
    public int localPort() {
        return NetworkUtils.getPort(req.localAddress());
    }

    @Override
    public Map<String, List<String>> parameterMap() {
        if (params == null) {
            // merge parameters of application/x-www-form-urlencoded
            Map<String, List<String>> decoded = req.paramMap();
            if (HttpMethod.POST.equals(req.method()) && bufferBody().readableBytes() > 0) {
                String contentType = req.headers().get(HttpHeaderNames.CONTENT_TYPE);
                if (contentType != null
                        && contentType.length() >= MediaTypeUtil.APPLICATION_FORM_URLENCODED_VALUE.length()
                        && contentType.charAt(0) == MediaTypeUtil.APPLICATION_FORM_URLENCODED_VALUE.charAt(0)) {
                    MediaType mediaType = contentType();

                    if (mediaType != null && mediaType.isCompatibleWith(MediaTypeUtil.APPLICATION_FORM_URLENCODED)) {
                        Charset charset = mediaType.charset();
                        if (charset == null) {
                            charset = StandardCharsets.UTF_8;
                        }
                        String body = bufferBody().string(charset);
                        final QueryStringDecoder decoder = new QueryStringDecoder(body, false);
                        Map<String, List<String>> bodyParam = null;
                        try {
                            bodyParam = decoder.parameters();
                        } catch (Exception e) {
                            logger.warn("Error while parsing body parameter, body: " + body, e);
                        }
                        if (bodyParam != null && !bodyParam.isEmpty()) {
                            bodyParam.putAll(decoded);
                            decoded = bodyParam;
                        }
                    }
                }
            }
            params = decoded;
        }
        return params;
    }

    @Override
    public Set<Cookie> cookies() {
        if (cookies == null) {
            String value = headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                Set<io.netty.handler.codec.http.cookie.Cookie> underlying = ServerCookieDecoder.STRICT.decode(value);
                cookies = new LinkedHashSet<>(underlying.size());
                underlying.forEach(c -> cookies.add(CookieUtil.wrap(c)));
            }
        }
        return cookies;
    }

    @Override
    public ByteBufAllocator alloc() {
        return req.alloc();
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.setAttribute(name, value);
    }

    @Override
    public Object removeAttribute(String name) {
        return attributes.removeAttribute(name);
    }

    @Override
    public String[] attributeNames() {
        return attributes.attributeNames();
    }

    @Override
    public String toString() {
        return req.toString();
    }

    private static String getHostAddress(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            InetSocketAddress socketAddress = (InetSocketAddress) address;
            InetAddress inet = socketAddress.getAddress();
            if (inet != null) {
                String addr = inet.getHostAddress();
                if (addr != null) {
                    return addr;
                }
            }
        } else if (address instanceof DomainSocketAddress) {
            return NetworkUtils.parseAddress(address);
        }
        return StringUtils.empty();
    }
}
