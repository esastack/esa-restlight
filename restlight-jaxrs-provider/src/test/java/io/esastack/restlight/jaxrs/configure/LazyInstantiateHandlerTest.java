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
package io.esastack.restlight.jaxrs.configure;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.HandlerFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LazyInstantiateHandlerTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new LazyInstantiateHandler(null, Object.class));
        assertThrows(NullPointerException.class, () -> new LazyInstantiateHandler(mock(DeployContext.class),
                null));
        assertDoesNotThrow(() -> new LazyInstantiateHandler(mock(DeployContext.class), Object.class));
    }

    @Test
    void testGetInstanceThenInit() {
        final DeployContext deployContext = mock(DeployContext.class);
        final HandlerFactory factory = mock(HandlerFactory.class);
        LazyInstantiateHandler handler = new LazyInstantiateHandler(deployContext, Object.class);
        assertThrows(IllegalStateException.class, handler::getInstanceThenInit);
        when(deployContext.handlerFactory()).thenReturn(Optional.of(factory));
        final List<Object> instantiated = new LinkedList<>();
        final AtomicInteger count = new AtomicInteger();
        when(factory.instantiate(any(), any())).thenAnswer(invocationOnMock -> {
            instantiated.add(invocationOnMock.getArgument(0));
            return new Object();
        });
        doAnswer(invocationOnMock -> {
            count.incrementAndGet();
            return count;
        }).when(factory).doInit(any(), any());


        final Object obj1 = handler.getInstanceThenInit();
        assertEquals(1, instantiated.size());
        assertEquals(Object.class, instantiated.get(0));
        assertEquals(1, count.intValue());

        final Object obj2 = handler.getInstanceThenInit();
        assertEquals(1, instantiated.size());
        assertEquals(Object.class, instantiated.get(0));
        assertEquals(1, count.intValue());

        assertSame(obj1, obj2);
    }

    @Test
    void testGetInstance() {
        final DeployContext deployContext = mock(DeployContext.class);
        final HandlerFactory factory = mock(HandlerFactory.class);
        LazyInstantiateHandler handler = new LazyInstantiateHandler(deployContext, Object.class);
        assertThrows(IllegalStateException.class, handler::getInstance);

        final Object obj = new Object();
        final List<Object> instantiated = new LinkedList<>();
        when(deployContext.handlerFactory()).thenReturn(Optional.of(factory));
        when(factory.instantiate(any(), any())).thenAnswer(invocationOnMock -> {
            instantiated.add(obj);
            return obj;
        });
        assertSame(obj, handler.getInstance());
        assertEquals(1, instantiated.size());
        assertSame(obj, instantiated.get(0));

        assertSame(obj, handler.getInstance());
        assertEquals(1, instantiated.size());
    }

}

