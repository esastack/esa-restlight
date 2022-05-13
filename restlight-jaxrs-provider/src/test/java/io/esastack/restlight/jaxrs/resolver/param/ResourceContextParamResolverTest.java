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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.jaxrs.impl.container.ResourceContextImpl;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceContextParamResolverTest {

    @Test
    void testAll() throws Throwable {
        assertThrows(NullPointerException.class, () -> new ResourceContextParamResolver(null));
        final DeployContext context = mock(DeployContext.class);
        final ResourceContextParamResolver factory = new ResourceContextParamResolver(context);

        assertTrue(factory.supports(new FieldParamImpl(Subject.class.getDeclaredField("resourceContext"))));
        assertTrue(factory.supports(new ConstructorParamImpl(Subject.class.getConstructor(ResourceContext.class),
                0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("resourceContext",
                ResourceContext.class), 0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("setResourceContext",
                ResourceContext.class), 0)));
        assertFalse(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("resourceContext0",
                ResourceContext.class), 0)));

        final Param param = new FieldParamImpl(Subject.class.getDeclaredField("resourceContext"));
        ParamResolver resolver = factory.createResolver(param, ResolverUtils.defaultConverters(param), null);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext ctx = new RequestContextImpl(request, response);
        final ParamResolverContext resolverContext = new ParamResolverContextImpl(ctx);

        when(context.handlerFactory()).thenReturn(Optional.of(mock(HandlerFactory.class)));
        assertNotNull(resolver.resolve(resolverContext));
        assertTrue(resolver.resolve(resolverContext) instanceof ResourceContextImpl);
    }

    private static class Subject {

        @Context
        private ResourceContext resourceContext;

        public Subject(@Context ResourceContext resourceContext) {
        }

        public void resourceContext(@Context ResourceContext resourceContext) {
        }

        public void resourceContext0(ResourceContext resourceContext) {
        }

        @Context
        public void setResourceContext(ResourceContext request) {

        }
    }


}

