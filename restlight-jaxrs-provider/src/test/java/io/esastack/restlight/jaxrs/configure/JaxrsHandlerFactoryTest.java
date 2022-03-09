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
package io.esastack.restlight.jaxrs.configure;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.jaxrs.impl.container.ResourceContextImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.impl.ext.ProvidersImpl;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsHandlerFactoryTest {

    @Test
    void testDoInstantiateAndInit0() {
        final DeployContext deployContext = mock(DeployContext.class);
        final Handlers handlers = mock(Handlers.class);

        final HandlerContext handlerContext = mock(HandlerContext.class);
        when(deployContext.handlerContexts()).thenReturn(Optional.of(method -> handlerContext));
        final JaxrsHandlerFactory factory = new JaxrsHandlerFactory(deployContext, handlers);

        when(handlerContext.resolverFactory()).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> factory
                .doInstantiate(handlerContext, HelloResource.class, null));

        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        when(handlerContext.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(handlerContext.paramPredicate()).thenReturn(Optional.of(param -> true));

        final ConfigurationImpl configuration = new ConfigurationImpl();
        final Providers providers = new ProvidersImpl(new ProvidersFactoryImpl(deployContext, configuration));
        final Application application = new Application();
        final ResourceContext resourceContext = new ResourceContextImpl(mock(HandlerFactory.class),
                mock(RequestContext.class));
        when(resolverFactory.getContextResolver(any())).thenAnswer(invocationOnMock ->
                (ContextResolver) (context) -> {
                    Param param = invocationOnMock.getArgument(0);
                    if (param.type().equals(Configuration.class)) {
                        return configuration;
                    } else if (param.type().equals(Providers.class)) {
                        return providers;
                    } else if (param.type().equals(ResourceContext.class)) {
                        return resourceContext;
                    } else if (param.type().equals(Application.class)) {
                        return application;
                    }
                    return null;
                });
        HelloResource resource = (HelloResource) factory
                .doInstantiate(handlerContext, HelloResource.class, null);
        assertNotNull(resource);
        assertSame(configuration, resource.configuration);
        assertSame(providers, resource.providers);

        factory.doInit0(handlerContext, resource, HelloResource.class, null);
        assertSame(application, resource.application);
        assertSame(resourceContext, resource.context);
    }

    private static final class HelloResource {

        private final Configuration configuration;
        private final Providers providers;

        @Context
        private Application application;

        private ResourceContext context;

        public HelloResource(@Context Configuration configuration,
                             @Context Providers providers) {
            this.configuration = configuration;
            this.providers = providers;
        }

        @Context
        private void setResourceContext(ResourceContext context) {
            this.context = context;
        }
    }

}

