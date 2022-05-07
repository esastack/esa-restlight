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
package io.esastack.restlight.starter.actuator.adapt;

import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class JaxrsHandlerMappingProviderTest {

    @Test
    void testJaxrs() throws NoSuchMethodException {
        final WebEndpointsSupplier webEndpointsSupplier = mock(WebEndpointsSupplier.class);
        final WebEndpointProperties webEndpointProperties = mock(WebEndpointProperties.class);
        final JaxrsHandlerMappingProvider provider =
                new JaxrsHandlerMappingProvider(webEndpointsSupplier, webEndpointProperties);

        final WebOperation operation = mock(WebOperation.class);
        final HandlerMethod handlerMethod = provider.getHandler(operation, Schedulers.BIZ);
        assertEquals(OperationHandler.class, handlerMethod.beanType());
        assertEquals(OperationHandler.class.getDeclaredMethod("handle",
                RequestContext.class, Map.class), handlerMethod.method());
        assertEquals("Jaxrs Endpoint Handler Proxy",
                provider.getHandler(operation, Schedulers.BIZ).toString());
    }

}

