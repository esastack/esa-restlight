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
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriInfoParamResolverTest {

    @Test
    void testAll() throws Throwable {
        final UriInfoParamResolver factory = new UriInfoParamResolver();

        assertTrue(factory.supports(new FieldParamImpl(Subject.class.getDeclaredField("uriInfo"))));
        assertTrue(factory.supports(new ConstructorParamImpl(Subject.class.getConstructor(UriInfo.class),
                0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("uriInfo",
                UriInfo.class), 0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("setUriInfo",
                UriInfo.class), 0)));
        assertFalse(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("uriInfo0",
                UriInfo.class), 0)));

        final Param param = new FieldParamImpl(Subject.class.getDeclaredField("uriInfo"));
        ParamResolver resolver = factory.createResolver(param, ResolverUtils.defaultConverterFunc(), null);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext ctx = new RequestContextImpl(request, response);
        final UriInfo uriInfo1 = (UriInfo) resolver.resolve(param, ctx);
        final UriInfo uriInfo2 = (UriInfo) resolver.resolve(param, ctx);
        assertNotNull(uriInfo1);
        assertSame(uriInfo1, uriInfo2);
    }

    private static class Subject {

        @Context
        private UriInfo uriInfo;

        public Subject(@Context UriInfo uriInfo) {
        }

        public void uriInfo(@Context UriInfo uriInfo) {
        }

        public void uriInfo0(UriInfo uriInfo) {
        }

        @Context
        public void setUriInfo(UriInfo uriInfo) {
        }
    }

}

