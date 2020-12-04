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
package esa.restlight.springmvc.spi;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.springmvc.resolver.exception.SpringMvcExceptionResolverFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringMvcExceptionResolverFactoryProviderTest {

    @Test
    void testFactory() {
        final SpringMvcExceptionResolverFactoryProvider provider = new SpringMvcExceptionResolverFactoryProvider();
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        when(ctx.exceptionMappers()).thenReturn(Optional.of(Collections.emptyList()));
        when(ctx.advices()).thenReturn(Optional.of(Collections.emptyList()));
        when(ctx.routeHandlerLocator()).thenReturn(Optional.of(mock(RouteHandlerLocator.class)));
        when(ctx.resolverFactory()).thenReturn(Optional.of(mock(HandlerResolverFactory.class)));

        final ExceptionResolverFactory factory = provider.factory(ctx);
        assertTrue(factory instanceof SpringMvcExceptionResolverFactory);

    }

}
