/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.springmvc.resolver.rspentity;

import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolver;
import io.esastack.restlight.core.serialize.FastJsonHttpBodySerializer;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseStatus0;
import io.esastack.restlight.springmvc.resolver.Pojo;
import io.esastack.restlight.springmvc.resolver.ResolverUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ResponseStatusEntityResolverFactoryTest {

    private static final ResponseStatusEntityResolverFactory resolverFactory
            = new ResponseStatusEntityResolverFactory();
    private static final Subject SUBJECT = new Subject();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(ResponseStatus0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final HandlerMethod absent = handlerMethods.get("none");
        assertFalse(resolverFactory.supports(absent));

        final HandlerMethod absent1 = handlerMethods.get("responseBody");
        assertFalse(resolverFactory.supports(absent1));

        final HandlerMethod noneResponseReason = handlerMethods.get("defaultResponseStatus");
        assertFalse(resolverFactory.supports(noneResponseReason));

        final HandlerMethod support = handlerMethods.get("responseWithReason");
        assertTrue(resolverFactory.supports(support));

        assertFalse(resolverFactory.alsoApplyWhenMissingHandler());
    }

    @Test
    void testResolve() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createResolverAndResolve(pojo, request, response, "responseWithReason");
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), resolved);
    }

    @Test
    void testResolveFromClass() throws Exception {
        Map<String, HandlerMethod> handlerMethods = ResolverUtils.extractHandlerMethods(new Subject1());
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createResolverAndResolve(pojo, request, response, "none", handlerMethods);
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), resolved);
    }

    private static byte[] createResolverAndResolve(Object returnValue,
                                                   HttpRequest request,
                                                   HttpResponse response,
                                                   String method) throws Exception {
        return createResolverAndResolve(returnValue, request, response, method, handlerMethods);
    }

    private static byte[] createResolverAndResolve(Object returnValue,
                                                   HttpRequest request,
                                                   HttpResponse response,
                                                   String method,
                                                   Map<String, HandlerMethod> handlerMethods) throws Exception {
        final HandlerMethod handlerMethod = handlerMethods.get(method);
        final ResponseEntityResolver resolver = resolverFactory.createResolver(handlerMethod,
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return ResolverUtils.writtenContent(request, response, returnValue, handlerMethod, resolver);
    }

    private static class Subject {

        public Pojo none() {
            return null;
        }

        @ResponseBody
        public Pojo responseBody() {
            return null;
        }

        @ResponseBody
        @ResponseStatus
        public Pojo defaultResponseStatus() {
            return null;
        }

        @ResponseBody
        @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "foo")
        public Pojo responseWithReason() {
            return null;
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "foo")
    private static class Subject1 {

        public Pojo none() {
            return null;
        }
    }

}

