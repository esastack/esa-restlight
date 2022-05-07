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
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.jaxrs.configure.JaxrsHandlerFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsHandlerFactoryProviderTest {

    @Test
    void testFactoryBean() {
        final JaxrsHandlerFactoryProvider provider = new JaxrsHandlerFactoryProvider();
        final DeployContext context = mock(DeployContext.class);
        when(context.handlers()).thenReturn(Optional.of(mock(Handlers.class)));
        when(context.handlerContexts()).thenReturn(Optional.of(mock(HandlerContextProvider.class)));

        Optional<HandlerFactory> rst = provider.factoryBean(context);
        assertTrue(rst.isPresent());
        assertEquals(JaxrsHandlerFactory.class, rst.get().getClass());
    }

}

