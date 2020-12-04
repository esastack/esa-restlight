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
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.springmvc.annotation.shaded.RequestMapping0;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringMvcRouteHandlerLocatorFactoryTest {

    @Test
    void testLocator() throws NoSuchMethodException {
        final SpringMvcRouteHandlerLocatorFactory factory = new SpringMvcRouteHandlerLocatorFactory();
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        when(ctx.options()).thenReturn(RestlightOptionsConfigure.defaultOpts());
        RouteHandlerLocator locator = factory.locator(ctx);
        assertNotNull(locator);
        assertEquals(SpringMvcRouteHandlerLocatorFactory.HandlerLocator.class, locator.getClass());

        final InvocableMethod method = mock(InvocableMethod.class);
        when(method.beanType()).thenReturn((Class) SpringMvcRouteHandlerLocatorFactoryTest.class);
        when(method.method())
                .thenReturn(SpringMvcRouteHandlerLocatorFactoryTest.class.getDeclaredMethod("forTest"));

        final HttpResponseStatus ret = ((SpringMvcRouteHandlerLocatorFactory.HandlerLocator) locator)
                .getCustomResponse(method);

        assumeTrue(RequestMapping0.shadedClass().getName().startsWith("org.springframework"));
        assertNotNull(ret);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), ret.code());
        assertEquals("foo", ret.reasonPhrase());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "foo")
    private void forTest() {

    }

}
