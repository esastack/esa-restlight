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
import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.impl.ByteBufHttpInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MockAsyncRequest implements AsyncRequest {

    private HttpScheme scheme;
    private long asyncTimeout;
    private ByteBuf body;
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
    private HttpVersion protocol;
    private String tcpSourceIp;
    private Map<String, Object> attributes = new ConcurrentHashMap<>(16);
    private Set<Cookie> cookies;

    public static Builder aMockRequest() {
        return new Builder();
    }

    @Override
    public HttpVersion httpVersion() {
        return protocol;
    }

    @Override
    public String scheme() {
        return scheme.toString().toUpperCase();
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
    public ByteBuf byteBufBody() {
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
    public ByteBufAllocator alloc() {
        return UnpooledByteBufAllocator.DEFAULT;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public Set<Cookie> cookies() {
        if (cookies == null) {
            String value = headers.get(HttpHeaderNames.COOKIE);
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
        private HttpScheme scheme = HttpScheme.HTTP;
        private long asyncTimeout = -1;
        private ByteBuf byteBuf;
        private HttpHeaders headers = new DefaultHttpHeaders();
        private HttpHeaders trailers = new DefaultHttpHeaders();
        private HttpMethod httpMethod = HttpMethod.GET;
        private String remoteAddr;
        private int remotePort = -1;
        private String localAddr;
        private int localPort = -1;
        private Map<String, List<String>> parameters = new HashMap<>();
        private String uri;
        private HttpVersion protocol = HttpVersion.HTTP_1_1;
        private String tcpSourceIp;
        private Map<String, Object> attributes = new HashMap<>(16);

        private Builder() {
        }

        public Builder withSchema(HttpScheme schema) {
            Checks.checkNotNull(schema);
            this.scheme = schema;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.byteBuf = Unpooled.buffer(body.length).writeBytes(body);
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
            return this.withCookie(new DefaultCookie(name, value));
        }

        public Builder withCookie(Cookie cookie) {
            String pre = this.headers.get(HttpHeaderNames.COOKIE);
            if (pre == null) {
                this.headers.set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            } else {
                this.headers.set(HttpHeaderNames.COOKIE, pre + ";" + ServerCookieEncoder.STRICT.encode(cookie));
            }
            return this;
        }

        public Builder withProtocol(HttpVersion protocol) {
            this.protocol = protocol;
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

        public MockAsyncRequest build() {
            MockAsyncRequest mockRequest = new MockAsyncRequest();
            mockRequest.scheme = scheme;
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
            mockRequest.body = this.byteBuf == null ? Unpooled.EMPTY_BUFFER : this.byteBuf;
            mockRequest.in = new ByteBufHttpInputStream(mockRequest.body, false);
            mockRequest.httpMethod = httpMethod;
            mockRequest.localAddr = this.localAddr;
            mockRequest.protocol = this.protocol;
            mockRequest.tcpSourceIp = this.tcpSourceIp;
            mockRequest.attributes = this.attributes;
            return mockRequest;
        }
    }
}
