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
import io.esastack.restlight.core.configure.ExtensionsHandler;
import io.esastack.restlight.core.configure.MiniConfigurableDeployments;
import io.esastack.restlight.jaxrs.configure.JaxrsExtensionsHandler;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsExtensionsHandlerFactoryTest {

    @Test
    void testHandler() {
        final JaxrsExtensionsHandlerFactory factory = new JaxrsExtensionsHandlerFactory();
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        when(deployments.deployContext()).thenReturn(mock(DeployContext.class));

        Optional<ExtensionsHandler> rst = factory.handler(deployments);
        assertTrue(rst.isPresent());
        assertEquals(JaxrsExtensionsHandler.class, rst.get().getClass());
    }
}

