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
package io.esastack.restlight.core.handler;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.handler.impl.HandlerMethodResolver;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class HandlerMethodResolverTest {

    @Test
    void testHandle() {
        HandlerMethodInfo methodInfo = mock(HandlerMethodInfo.class);
        when(methodInfo.customStatus()).thenReturn(HttpStatus.BAD_GATEWAY);

        HttpResponse response = mock(HttpResponse.class);
        RequestContext context = new RequestContextImpl(mock(HttpRequest.class), response);
        HandlerValueResolver resolver = new HandlerMethodResolver(methodInfo);
        Object returnValue = mock(Object.class);
        resolver.handle(returnValue, context);
        Mockito.verify(response, times(1)).entity(returnValue);
        Mockito.verify(response, times(1)).status(HttpStatus.BAD_GATEWAY.code());
    }

}
