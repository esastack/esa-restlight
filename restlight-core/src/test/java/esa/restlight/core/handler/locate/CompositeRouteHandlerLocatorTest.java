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
package esa.restlight.core.handler.locate;

import esa.restlight.core.handler.RouteHandler;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompositeRouteHandlerLocatorTest {

    @Test
    void testWrap() {
        assertNull(CompositeRouteHandlerLocator.wrapIfNecessary(Collections.emptyList()));
        final RouteHandlerLocator l0 = mock(RouteHandlerLocator.class);
        assertEquals(l0, CompositeRouteHandlerLocator.wrapIfNecessary(Collections.singleton(l0)));

        final RouteHandlerLocator l1 = mock(RouteHandlerLocator.class);
        final RouteHandlerLocator l2 = mock(RouteHandlerLocator.class);
        final RouteHandler h0 = mock(RouteHandler.class);
        final RouteHandler h1 = mock(RouteHandler.class);
        when(l0.getRouteHandler(any(), any(), any())).thenReturn(Optional.of(h0));
        when(l1.getRouteHandler(any(), any(), any())).thenReturn(Optional.of(h1));
        when(l2.getRouteHandler(any(), any(), any())).thenReturn(Optional.empty());

        final RouteHandlerLocator wrapped = CompositeRouteHandlerLocator.wrapIfNecessary(Arrays.asList(l0, l1, l2));
        assertNotNull(wrapped);
        final Optional<RouteHandler> r = wrapped.getRouteHandler(null, null, null);
        assertTrue(r.isPresent());
        assertSame(h0, r.get());

        final RouteHandlerLocator wrapped1 = CompositeRouteHandlerLocator.wrapIfNecessary(Arrays.asList(l2, l1, l0));
        assertNotNull(wrapped1);
        final Optional<RouteHandler> r1 = wrapped1.getRouteHandler(null, null, null);
        assertTrue(r1.isPresent());
        assertSame(h1, r1.get());
    }

}
