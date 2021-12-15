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
package io.esastack.restlight.test.context;

import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.test.mock.MockHttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMockMvcTest {

    @Test
    void testPerform() {
        final RestlightHandler<RequestContext> handler = mock(RestlightHandler.class);
        final DefaultMockMvc mockMvc = new DefaultMockMvc(handler);

        final MockHttpRequest request = MockHttpRequest.aMockRequest().build();
        when(handler.process(any())).then(mock -> {
            final RequestContext context = mock.getArgument(0, RequestContext.class);
            final HttpResponse res = context.response();
            context.attr(DefaultMockMvc.RETURN_VALUE_KEY).set("foo");
            res.status(200);
            res.entity("foo".getBytes());
            return Futures.completedFuture();
        });
        mockMvc.perform(request).addExpect(r -> {
            assertSame(request, r.request());
            assertNotNull(r.response());
            assertArrayEquals("foo".getBytes(), (byte[]) r.response().entity());
        }).then(r -> {
            assertSame(request, r.request());
            assertNotNull(r.response());
            assertArrayEquals("foo".getBytes(), (byte[]) r.response().entity());
        });
    }

    @Test
    void testPerformAsync() {
        final RestlightHandler<RequestContext> handler = mock(RestlightHandler.class);
        final DefaultMockMvc mockMvc = new DefaultMockMvc(handler);

        final MockHttpRequest request = MockHttpRequest.aMockRequest().build();
        when(handler.process(any())).then(mock -> {
            final RequestContext ctx = mock.getArgument(0, RequestContext.class);
            ctx.attr(DefaultMockMvc.RETURN_VALUE_KEY).set(Futures.completedFuture("foo"));
            return Futures.completedFuture();
        });
        mockMvc.perform(request)
                .addExpect(r -> Assertions.assertEquals("foo", r.result()))
                .then(r -> Assertions.assertEquals("foo", r.result()));
    }

}
