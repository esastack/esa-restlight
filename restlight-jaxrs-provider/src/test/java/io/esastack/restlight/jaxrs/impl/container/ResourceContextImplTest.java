/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.impl.container;

import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.context.RequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ResourceContextImplTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new ResourceContextImpl(null,
                mock(RequestContext.class)));
        assertThrows(NullPointerException.class, () -> new ResourceContextImpl(mock(HandlerFactory.class),
                null));

        final HandlerFactory factory = mock(HandlerFactory.class);
        final RequestContext context0 = mock(RequestContext.class);
        final ResourceContextImpl context = new ResourceContextImpl(factory, context0);
        verify(factory, never()).getInstance(Object.class, context0);
        context.getResource(Object.class);
        verify(factory).getInstance(Object.class, context0);

        final Object obj = new Object();
        verify(factory, never()).doInit(obj, context0);
        context.initResource(obj);
        verify(factory).doInit(obj, context0);
    }

}

