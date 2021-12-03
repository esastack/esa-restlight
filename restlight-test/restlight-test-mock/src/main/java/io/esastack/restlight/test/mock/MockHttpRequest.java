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
package io.esastack.restlight.test.mock;

import esa.commons.Checks;
import esa.commons.StringUtils;
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
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.impl.ByteBufHttpInputStream;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MockHttpRequest implements HttpRequest {

    private long asyncTimeout;
    private String scheme;
    private Buffer body;
    private ByteBufHttpInputStream in;
    private HttpHeaders headers;
    private HttpHeaders trailers;
    private HttpMethod httpMethod;
    private String remoteAddr;
    private int remotePort;
    private String localAddr;
    private int localPort;
    private Map<String, List<String>> parameters;
    private String uri;
    private String path;
    private String query;
    private HttpVersion httpVersion;
    private String tcpSourceIp;
    private Map<String, Object> attributes = new ConcurrentHashMap<>(16);
    private Set<Cookie> cookies;

    public static Builder aMockRequest() {
        return new Builder();
    }

    @Override
    public HttpVersion httpVersion() {
        return httpVersion;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String query() {
        return query;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public Buffer body() {
        return body;
    }

    @Override
    public String remoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public String tcpSourceAddr() {
        return this.tcpSourceIp;
    }

    @Override
    public int remotePort() {
        return remotePort;
    }

    @Override
    public HttpMethod method() {
        return this.httpMethod;
    }

    @Override
    public HttpInputStream inputStream() {
        return in;
    }

    @Override
    public String localAddr() {
        return this.localAddr;
    }

    @Override
    public int localPort() {
        return this.localPort;
    }

    @Override
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public Object alloc() {
        return UnpooledByteBufAllocator.DEFAULT;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public MediaType contentType() {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return StringUtils.isEmpty(contentType) ? null : MediaTypeUtil.parseMediaType(contentType);
    }

    @Override
    public List<MediaType> accepts() {
        List<MediaType> accepts = new LinkedList<>();
        MediaTypeUtil.parseMediaTypes(headers.get(HttpHeaderNames.ACCEPT), accepts);
        return accepts;
    }

    @Override
    public Set<Cookie> cookies() {
        if (cookies == null) {
            String value = headers.get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                Set<io.netty.handler.codec.http.cookie.Cookie> underlying = ServerCookieDecoder.STRICT.decode(value);
                this.cookies = new LinkedHashSet<>();
                underlying.forEach(c -> this.cookies.add(CookieUtil.wrap(c)));
            }
        }
        return cookies;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    @Override
    public String[] attributeNames() {
        return attributes.keySet().toArray(new String[0]);
    }

    @Override
    public String getParameter(String parName) {
        if (parameters == null) {
            return null;
        }

        List<String> params = parameters.get(parName);
        if (params != null && params.size() > 0) {
            return params.get(0);
        }
        return null;
    }

    @Override
    public List<String> getParameters(String parName) {
        if (parameters == null) {
            return null;
        }
        return parameters.get(parName);
    }

    @Override
    public Map<String, List<String>> parameterMap() {
        return parameters;
    }

    @Deprecated
    public long getAsyncTimeout() {
        return asyncTimeout;
    }

    public static final class Builder {
        private long asyncTimeout = -1;
        private Buffer buffer;
        private String scheme = HttpScheme.HTTP.name().toString().toUpperCase();
        private HttpHeaders headers = new Http1HeadersImpl();
        private HttpHeaders trailers = new Http1HeadersImpl();
        private HttpMethod httpMethod = HttpMethod.GET;
        private String remoteAddr;
        private int remotePort = -1;
        private String localAddr;
        private int localPort = -1;
        private Map<String, List<String>> parameters = new HashMap<>();
        private String uri;
        private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        private String tcpSourceIp;
        private final Map<String, Object> attributes = new HashMap<>(16);

        private Builder() {
        }

        public Builder withScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.buffer = BufferUtil.wrap(Unpooled.buffer(body.length).writeBytes(body));
            return this;
        }

        public Builder withHeaders(HttpHeaders headers) {
            Checks.checkNotNull(headers, "headers");
            this.headers = headers;
            return this;
        }

        public Builder withTrailers(HttpHeaders trailers) {
            this.trailers = trailers;
            return this;
        }

        public Builder withHeader(String name, Object value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder withMethod(String method) {
            Checks.checkNotNull(method);
            this.httpMethod = HttpMethod.valueOf(method);
            return this;
        }

        public Builder withMethod(HttpMethod method) {
            Checks.checkNotNull(method);
            this.httpMethod = method;
            return this;
        }

        public Builder withRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder withRemotePort(int remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder withLocalAddr(String localAddr) {
            this.localAddr = localAddr;
            return this;
        }

        public Builder withLocalPort(int localPort) {
            this.localPort = localPort;
            return this;
        }

        public Builder withParameters(Map<String, List<String>> parameters) {
            Checks.checkNotNull(parameters, "parameters");
            this.parameters = parameters;
            return this;
        }

        public Builder withParameter(String name, String value) {
            Checks.checkNotNull(name, "name");
            List<String> values = this.parameters.get(name);
            if (values == null) {
                values = new ArrayList<>(1);
                parameters.put(name, values);
            }
            values.add(value);
            return this;
        }

        public Builder withUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder withCookie(String name, String value) {
            return this.withCookie(new CookieImpl(name, value));
        }

        public Builder withCookie(Cookie cookie) {
            String pre = this.headers.get(HttpHeaderNames.COOKIE);
            if (pre == null) {
                this.headers.set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT
                        .encode(new DefaultCookie(cookie.name(), cookie.value())));
            } else {
                this.headers.set(HttpHeaderNames.COOKIE, pre + ";" + ServerCookieEncoder.STRICT
                        .encode(new DefaultCookie(cookie.name(), cookie.value())));
            }
            return this;
        }

        public Builder withHttpVersion(HttpVersion httpVersion) {
            this.httpVersion = httpVersion;
            return this;
        }

        public Builder withTcpSourceId(String tcpSourceIp) {
            this.tcpSourceIp = tcpSourceIp;
            return this;
        }

        public Builder withAttribute(String name, Object value) {
            this.attributes.put(name, value);
            return this;
        }

        @Deprecated
        public Builder withAsyncTimeOut(long timeout) {
            this.asyncTimeout = timeout;
            return this;
        }

        public MockHttpRequest build() {
            MockHttpRequest mockRequest = new MockHttpRequest();
            mockRequest.scheme = this.scheme;
            mockRequest.asyncTimeout = this.asyncTimeout;
            mockRequest.uri = StringUtils.nonEmptyOrElse(this.uri, "/");
            QueryStringDecoder decoder = new QueryStringDecoder(mockRequest.uri);
            mockRequest.path = decoder.path();
            mockRequest.query = decoder.rawQuery();
            this.parameters.putAll(decoder.parameters());
            mockRequest.parameters = this.parameters;
            mockRequest.headers = this.headers;
            mockRequest.trailers = this.trailers;
            mockRequest.localPort = this.localPort;
            mockRequest.remoteAddr = this.remoteAddr;
            mockRequest.remotePort = this.remotePort;
            mockRequest.body = this.buffer == null ? BufferUtil.wrap(Unpooled.EMPTY_BUFFER) : this.buffer;
            mockRequest.in = new ByteBufHttpInputStream(mockRequest.body, false);
            mockRequest.httpMethod = httpMethod;
            mockRequest.localAddr = this.localAddr;
            mockRequest.httpVersion = this.httpVersion;
            mockRequest.tcpSourceIp = this.tcpSourceIp;
            mockRequest.attributes = this.attributes;
            return mockRequest;
        }
    }
}
