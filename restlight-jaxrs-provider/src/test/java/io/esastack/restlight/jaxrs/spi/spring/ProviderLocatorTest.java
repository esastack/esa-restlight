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
package io.esastack.restlight.jaxrs.spi.spring;

import io.esastack.restlight.core.DeployContext;
import jakarta.ws.rs.ext.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderLocatorTest {

    @Test
    void testGetExtensions() {
        final ProviderLocator locator = new ProviderLocator();
        final ApplicationContext ctx = mock(ApplicationContext.class);

        Collection<Object> extensions = locator.getExtensions(ctx, mock(DeployContext.class));
        assertTrue(extensions.isEmpty());

        final Object p1 = new Object();
        final Object p2 = new Object();
        Map<String, Object> beans = new HashMap<>();
        beans.put("p1", p1);
        beans.put("p2", p2);
        when(ctx.getBeansWithAnnotation(Provider.class)).thenReturn(beans);
        extensions = locator.getExtensions(ctx, mock(DeployContext.class));
        assertEquals(2, extensions.size());
        assertTrue(extensions.contains(p1));
        assertTrue(extensions.contains(p2));
    }

}

