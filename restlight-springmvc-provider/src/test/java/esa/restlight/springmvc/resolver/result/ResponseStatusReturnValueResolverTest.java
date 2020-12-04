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
package esa.restlight.springmvc.resolver.result;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.ResponseStatus0;
import esa.restlight.springmvc.resolver.Pojo;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ResponseStatusReturnValueResolverTest {

    private static ResponseStatusReturnValueResolver resolverFactory;

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(ResponseStatus0.shadedClass().getName().startsWith("org.springframework"));
        resolverFactory = new ResponseStatusReturnValueResolver();
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final InvocableMethod absent = handlerMethods.get("none");
        assertFalse(resolverFactory.supports(absent));

        final InvocableMethod absent1 = handlerMethods.get("responseBody");
        assertFalse(resolverFactory.supports(absent1));

        final InvocableMethod noneResponseReason = handlerMethods.get("defaultResponseStatus");
        assertFalse(resolverFactory.supports(noneResponseReason));

        final InvocableMethod support = handlerMethods.get("responseWithReason");
        assertTrue(resolverFactory.supports(support));
    }

    @Test
    void testResolve() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createResolverAndResolve(pojo, request, response, "responseWithReason");
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), resolved);
    }

    @Test
    void testResolveFromClass() throws Exception {
        Map<String, HandlerMethod> handlerMethods = ResolverUtils.extractHandlerMethods(new Subject1());
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createResolverAndResolve(pojo, request, response, "none", handlerMethods);
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), resolved);
    }

    private static byte[] createResolverAndResolve(Object returnValue,
                                                   AsyncRequest request,
                                                   AsyncResponse response,
                                                   String method) throws Exception {
        return createResolverAndResolve(returnValue, request, response, method, handlerMethods);
    }

    private static byte[] createResolverAndResolve(Object returnValue,
                                                   AsyncRequest request,
                                                   AsyncResponse response,
                                                   String method,
                                                   Map<String, HandlerMethod> handlerMethods) throws Exception {
        final InvocableMethod invocableMethod = handlerMethods.get(method);
        assertTrue(resolverFactory.supports(invocableMethod));
        final ReturnValueResolver resolver = resolverFactory.createResolver(invocableMethod,
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return resolver.resolve(returnValue, request, response);
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
