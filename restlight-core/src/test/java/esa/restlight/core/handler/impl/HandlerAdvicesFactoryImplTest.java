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
package esa.restlight.core.handler.impl;

import esa.restlight.core.DeployContext;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerAdvicesFactory;
import esa.restlight.core.spi.HandlerAdviceFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class HandlerAdvicesFactoryImplTest {

    @Test
    void testGetHandlerAdvicesWithEmptyAdvice() {
        final HandlerAdvicesFactory factory = new HandlerAdvicesFactoryImpl(mock(DeployContext.class), null);
        final HandlerAdvice[] advices = factory.getHandlerAdvices(null);
        assertNotNull(advices);
        assertEquals(0, advices.length);
    }

    @Test
    void testGetHandlerAdvices() {
        final HandlerAdvice advice0 = mock(HandlerAdvice.class);
        final HandlerAdvice advice1 = mock(HandlerAdvice.class);

        final HandlerAdviceFactory f0 = mock(HandlerAdviceFactory.class);
        final HandlerAdviceFactory f1 = mock(HandlerAdviceFactory.class);
        final HandlerAdviceFactory f2 = mock(HandlerAdviceFactory.class);

        when(f0.handlerAdvice(any(), any())).thenReturn(Optional.of(advice0));
        when(f1.handlerAdvice(any(), any())).thenReturn(Optional.of(advice1));
        when(f2.handlerAdvice(any(), any())).thenReturn(Optional.empty());


        final HandlerAdvicesFactory factory =
                new HandlerAdvicesFactoryImpl(mock(DeployContext.class), Arrays.asList(f0, f1));
        final HandlerAdvice[] advices = factory.getHandlerAdvices(null);
        assertNotNull(advices);
        assertEquals(2, advices.length);
        assertSame(advice0, advices[0]);
        assertSame(advice1, advices[1]);
    }

}
