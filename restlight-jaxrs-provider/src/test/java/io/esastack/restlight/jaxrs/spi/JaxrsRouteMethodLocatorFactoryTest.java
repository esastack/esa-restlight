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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.method.HandlerMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsRouteMethodLocatorFactoryTest {

    @Test
    void testLocator() {
        final JaxrsRouteMethodLocatorFactory factory = new JaxrsRouteMethodLocatorFactory();
        final DeployContext ctx = mock(DeployContext.class);
        when(ctx.options()).thenReturn(RestlightOptionsConfigure.defaultOpts());
        RouteMethodLocator locator = factory.locator(ctx);
        assertNotNull(locator);
        assertEquals(JaxrsRouteMethodLocatorFactory.HandlerLocator.class, locator.getClass());
        assertNull(((JaxrsRouteMethodLocatorFactory.HandlerLocator) locator)
                .getCustomStatus(mock(HandlerMethod.class)));
    }

}

