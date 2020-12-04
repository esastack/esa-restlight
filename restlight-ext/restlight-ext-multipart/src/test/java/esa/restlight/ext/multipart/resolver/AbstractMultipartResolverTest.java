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
package esa.restlight.ext.multipart.resolver;

import esa.commons.ClassUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.ext.multipart.core.MultipartConfig;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractMultipartResolverTest {

    private static final String CONTENT_TYPE = "multipart/form-data; boundary=---1234";
    private static final ResolverSubject SUBJECT = new ResolverSubject();
    private static final MultipartConfig config = new MultipartConfig(false);

    static MultipartAttrArgumentResolver attrResolver = new MultipartAttrArgumentResolver(config);
    static MultipartFileArgumentResolver fileResolver = new MultipartFileArgumentResolver(config);

    static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    public static void setUp() {
        handlerMethods = ClassUtils.userDeclaredMethods(ResolverSubject.class)
                .stream()
                .map(method -> HandlerMethod.of(method, SUBJECT))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    static AsyncRequest build(String body) {
        return build(body.getBytes(CharsetUtil.UTF_8));
    }

    static AsyncRequest build(byte[] bytes) {
        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                "http://localhost");
        httpRequest.setDecoderResult(DecoderResult.SUCCESS);
        httpRequest.headers().add(HttpHeaderNames.CONTENT_TYPE.toString(), CONTENT_TYPE);
        httpRequest.content().writeBytes(bytes);

        return MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), CONTENT_TYPE)
                .withBody(bytes)
                .build();
    }
}
