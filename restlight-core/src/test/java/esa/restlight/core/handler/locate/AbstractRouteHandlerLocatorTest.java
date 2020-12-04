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

import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerAdvicesFactory;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractRouteHandlerLocatorTest {

    @Test
    void testGetHandler() throws Throwable {
        final HandlerAdvicesFactory factory = mock(HandlerAdvicesFactory.class);
        when(factory.getHandlerAdvices(any()))
                .thenReturn(new HandlerAdvice[]{(request, response, args, invoker) -> "bar"});

        final AbstractRouteHandlerLocator locator = new AbstractRouteHandlerLocator("foo", factory) {
            @Override
            protected HttpResponseStatus getCustomResponse(InvocableMethod handlerMethod) {
                return HttpResponseStatus.OK;
            }
        };

        final Optional<RouteHandler> handler = locator.getRouteHandler(AbstractRouteHandlerLocatorTest.class,
                AbstractRouteHandlerLocatorTest.class.getDeclaredMethod("forTest"),
                new AbstractRouteHandlerLocatorTest());

        assertNotNull(handler);
        assertTrue(handler.isPresent());
        assertEquals("foo", handler.get().scheduler());
        assertTrue(handler.get().hasCustomResponse());
        assertEquals(HttpResponseStatus.OK, handler.get().customResponse());
        assertEquals("bar",
                handler.get().invoke(MockAsyncRequest.aMockRequest().build(),
                        MockAsyncResponse.aMockResponse().build(),
                        null));
    }

    private void forTest() {
    }

}
