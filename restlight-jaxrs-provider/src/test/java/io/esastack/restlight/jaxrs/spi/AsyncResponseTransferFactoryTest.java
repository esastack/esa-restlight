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

import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsyncResponseTransferFactoryTest {

    @Test
    void testAll() {
        final AsyncResponseTransferFactory factory = new AsyncResponseTransferFactory();
        final HandlerMethod method = mock(HandlerMethod.class);
        final MethodParam[] params = new MethodParam[1];
        final MethodParam param = mock(MethodParam.class);
        params[0] = param;
        when(method.parameters()).thenReturn(params);

        when(param.hasAnnotation(Suspended.class)).thenReturn(true);
        doReturn(AsyncResponse.class).when(param).type();
        assertTrue(factory.futureTransfer(method).isPresent());

        when(param.hasAnnotation(Suspended.class)).thenReturn(false);
        assertFalse(factory.futureTransfer(method).isPresent());

        when(param.hasAnnotation(Suspended.class)).thenReturn(true);
        doReturn(Object.class).when(param).type();
        assertFalse(factory.futureTransfer(method).isPresent());
    }

}

