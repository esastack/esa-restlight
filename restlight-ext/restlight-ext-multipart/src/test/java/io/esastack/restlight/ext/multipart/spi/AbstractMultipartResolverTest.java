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
package io.esastack.restlight.ext.multipart.spi;

import esa.commons.ClassUtils;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.core.method.RouteHandlerMethod;
import io.esastack.restlight.core.method.RouteHandlerMethodImpl;
import io.esastack.restlight.ext.multipart.core.MultipartConfig;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.schedule.Schedulers;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractMultipartResolverTest {

    private static final String CONTENT_TYPE = "multipart/form-data; boundary=---1234";
    private static final ResolverSubject SUBJECT = new ResolverSubject();
    private static final MultipartConfig config = new MultipartConfig(false);
    static MultipartAttrParamResolver attrResolver = new MultipartAttrParamResolver();
    static MultipartFileParamResolver fileResolver = new MultipartFileParamResolver();
    {
        attrResolver.initFactory(config);
        fileResolver.initFactory(config);
    }

    static Map<String, RouteHandlerMethod> handlerMethods;

    @BeforeAll
    public static void setUp() {
        handlerMethods = ClassUtils.userDeclaredMethods(ResolverSubject.class)
                .stream()
                .map(method -> RouteHandlerMethodImpl.of(HandlerMethodImpl.of(ClassUtils.getUserType(SUBJECT), method),
                        false, Schedulers.BIZ))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    static HttpRequest build(String body) {
        return build(body.getBytes(CharsetUtil.UTF_8));
    }

    static HttpRequest build(byte[] bytes) {
        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                "http://localhost");
        httpRequest.setDecoderResult(DecoderResult.SUCCESS);
        httpRequest.headers().add(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        httpRequest.content().writeBytes(bytes);

        return MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE)
                .withBody(bytes)
                .build();
    }
}
