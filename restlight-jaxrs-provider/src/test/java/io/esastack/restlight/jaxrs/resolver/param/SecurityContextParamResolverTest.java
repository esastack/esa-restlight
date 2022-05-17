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

import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityContextParamResolverTest {

    @Test
    void testAll() throws Throwable {
        final SecurityContextParamResolver factory = new SecurityContextParamResolver();

        assertTrue(factory.supports(new FieldParamImpl(Subject.class.getDeclaredField("securityContext"))));
        assertTrue(factory.supports(new ConstructorParamImpl(Subject.class.getConstructor(SecurityContext.class),
                0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("securityContext",
                SecurityContext.class), 0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("securityContext",
                SecurityContext.class), 0)));
        assertFalse(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("securityContext0",
                SecurityContext.class), 0)));

        final Param param = new FieldParamImpl(Subject.class.getDeclaredField("securityContext"));
        ParamResolver resolver = factory.createResolver(param, ResolverUtils.defaultConverters(param), null);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext ctx = new RequestContextImpl(request, response);

        final SecurityContext securityContext = mock(SecurityContext.class);
        JaxrsContextUtils.setSecurityContext(ctx, securityContext);
        assertSame(securityContext, resolver.resolve(new ParamResolverContextImpl(ctx, param)));
    }

    private static class Subject {

        @Context
        private SecurityContext securityContext;

        public Subject(@Context SecurityContext securityContext) {
        }

        public void securityContext(@Context SecurityContext securityContext) {
        }

        public void securityContext0(SecurityContext securityContext) {
        }

        @Context
        public void setSecurityContext(SecurityContext securityContext) {

        }
    }

}

