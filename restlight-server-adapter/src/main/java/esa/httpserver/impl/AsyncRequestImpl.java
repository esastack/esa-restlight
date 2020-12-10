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
package esa.httpserver.impl;

import esa.commons.NetworkUtils;
import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.core.Request;
import esa.restlight.core.util.MediaType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link AsyncRequest} that wraps the {@link Request} as delegate.
 */
@Internal
public class AsyncRequestImpl implements AsyncRequest {

    private static final Logger logger = LoggerFactory.getLogger(AsyncRequestImpl.class);

    private static final HttpVersion HTTP2 =
            new HttpVersion("HTTP", 2, 0, true);
    private final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();
    private final Request req;
    private final HttpMethod method;
    private final HttpHeaders headers;
    private final HttpHeaders trailers;
    private Map<String, List<String>> params;
    private HttpInputStream is;
    private Set<Cookie> cookies;

    public AsyncRequestImpl(Request req) {
        this.req = req;
        this.method = HttpMethod.valueOf(req.rawMethod());
        if (req.headers() instanceof HttpHeaders) {
            this.headers = (HttpHeaders) req.headers();
        } else {
            this.headers = new DefaultHttpHeaders();
            Iterator<Map.Entry<CharSequence, CharSequence>> it =
                    req.headers().iteratorCharSequence();
            while (it.hasNext()) {
                Map.Entry<CharSequence, CharSequence> entry = it.next();
                this.headers.add(entry.getKey(), entry.getValue());
            }
        }

        if (req.aggregated().trailers().isEmpty()) {
            this.trailers = EmptyHttpHeaders.INSTANCE;
        } else if (req.aggregated().trailers() instanceof HttpHeaders) {
            this.trailers = (HttpHeaders) req.aggregated().trailers();
        } else {
            this.trailers = new DefaultHttpHeaders();
            Iterator<Map.Entry<CharSequence, CharSequence>> it =
                    req.aggregated().trailers().iteratorCharSequence();
            while (it.hasNext()) {
                Map.Entry<CharSequence, CharSequence> entry = it.next();
                this.trailers.add(entry.getKey(), entry.getValue());
            }
        }

    }

    @Override
    public HttpVersion httpVersion() {
        if (req.version() == esa.commons.http.HttpVersion.HTTP_1_1) {
            return HttpVersion.HTTP_1_1;
        } else if (req.version() == esa.commons.http.HttpVersion.HTTP_1_0) {
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
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public ByteBuf byteBufBody() {
        return req.aggregated().body();
    }

    @Override
    public HttpInputStream inputStream() {
        if (is == null) {
            this.is = new ByteBufHttpInputStream(byteBufBody(), false);
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
            if (esa.commons.http.HttpMethod.POST.equals(req.method()) && byteBufBody().isReadable()) {
                String contentType = req.headers().get(HttpHeaderNames.CONTENT_TYPE);
                if (contentType != null
                        && contentType.length() >= MediaType.APPLICATION_FORM_URLENCODED_VALUE.length()
                        && contentType.charAt(0) == MediaType.APPLICATION_FORM_URLENCODED_VALUE.charAt(0)) {
                    MediaType mediaType = null;
                    try {
                        mediaType = MediaType.valueOf(contentType);
                    } catch (Exception e) {
                        logger.warn("Error while parsing content type: " + contentType, e);
                    }

                    if (mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                        Charset charset = mediaType.charset();
                        if (charset == null) {
                            charset = StandardCharsets.UTF_8;
                        }
                        String body = byteBufBody().toString(charset);
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
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
        }
        return cookies;
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return this.attributes.remove(name);
    }

    @Override
    public String[] attributeNames() {
        return this.attributes.keySet().toArray(new String[0]);
    }

    @Override
    public ByteBufAllocator alloc() {
        return req.alloc();
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
