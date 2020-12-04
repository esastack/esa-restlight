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
package esa.restlight.springmvc.bootstrap;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.springmvc.annotation.shaded.CookieValue0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

class SpringMvcDispatcherHandlerTest {

    @Test
    void testHandlerUserDispatcherException() {
        final SpringMvcDispatcherHandler handler = new SpringMvcDispatcherHandler(mock(RouteRegistry.class));
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Throwable t = new RuntimeException("foo");
        assertFalse(handler.handleUserDispatchException(request, response, t));
        assumeTrue(CookieValue0.shadedClass().getName().startsWith("org.springframework"));
        assertTrue(handler.handleUserDispatchException(request, response, new Ex()));

        assertEquals(MediaType.TEXT_PLAIN.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.status());
        assertEquals("foo",
                response.getSentData().toString(StandardCharsets.UTF_8));


    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "foo")
    private static final class Ex extends RuntimeException {
    }

}
