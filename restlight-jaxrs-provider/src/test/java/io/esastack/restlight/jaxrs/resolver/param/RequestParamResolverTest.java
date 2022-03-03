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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.MethodParamImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestParamResolverTest {

    @Test
    void testAll() throws Throwable {
        final RequestParamResolver factory = new RequestParamResolver();
        assertTrue(factory.supports(new FieldParamImpl(Subject.class.getDeclaredField("request"))));
        assertTrue(factory.supports(new ConstructorParamImpl(Subject.class.getConstructor(Request.class),
                0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("request",
                Request.class), 0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("setRequest",
                Request.class), 0)));
        assertFalse(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("request0",
                Request.class), 0)));

        final Param param = new FieldParamImpl(Subject.class.getDeclaredField("request"));
        ParamResolver resolver = factory.createResolver(param, ResolverUtils.defaultConverterFunc(), null);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);

        final Request resolved = (Request) resolver.resolve(param, context);
        assertEquals(HttpMethod.GET.name(), resolved.getMethod());
    }

    private static class Subject {

        @Context
        private Request request;

        public Subject(@Context Request request) {
        }

        public void request(@Context Request request) {
        }

        public void request0(Request request) {
        }

        @Context
        public void setRequest(Request request) {

        }
    }

}

