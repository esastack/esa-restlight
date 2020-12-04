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
package esa.restlight.jaxrs.spi;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.method.InvocableMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsRouteHandlerLocatorFactoryTest {

    @Test
    void testLocator() {
        final JaxrsRouteHandlerLocatorFactory factory = new JaxrsRouteHandlerLocatorFactory();
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        when(ctx.options()).thenReturn(RestlightOptionsConfigure.defaultOpts());
        RouteHandlerLocator locator = factory.locator(ctx);
        assertNotNull(locator);
        assertEquals(JaxrsRouteHandlerLocatorFactory.HandlerLocator.class, locator.getClass());
        assertNull(((JaxrsRouteHandlerLocatorFactory.HandlerLocator) locator)
                .getCustomResponse(mock(InvocableMethod.class)));
    }

}
