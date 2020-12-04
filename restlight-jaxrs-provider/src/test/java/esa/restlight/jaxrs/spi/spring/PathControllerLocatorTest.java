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
package esa.restlight.jaxrs.spi.spring;

import esa.restlight.core.DeployContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class PathControllerLocatorTest {

    @Test
    void testGetControllers() {
        final PathControllerLocator locator = new PathControllerLocator();
        final ApplicationContext ctx = mock(ApplicationContext.class);


        Collection<Object> controllers = locator.getControllers(ctx, mock(DeployContext.class));
        assertTrue(controllers.isEmpty());

        final Object c1 = new Object();

        when(ctx.getBeansWithAnnotation(Path.class)).thenReturn(Collections.singletonMap("foo", c1));
        controllers = locator.getControllers(ctx, mock(DeployContext.class));
        assertFalse(controllers.isEmpty());
        assertTrue(controllers.contains(c1));
        final Object c2 = new Object();
        when(ctx.getBeansWithAnnotation(Path.class)).thenReturn(Collections.emptyMap());
        when(ctx.getBeansWithAnnotation(Controller.class)).thenReturn(Collections.singletonMap("foo", c2));
        assertFalse(controllers.isEmpty());
        assertTrue(controllers.contains(c1));
    }

}
