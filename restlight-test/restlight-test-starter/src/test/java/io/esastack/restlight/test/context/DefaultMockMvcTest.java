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

import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.test.mock.MockHttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
            final HttpRequest req = context.request();
            final HttpResponse res = context.response();
            req.setAttribute(DefaultMockMvc.RETURN_VALUE_KEY, "foo");
            res.sendResult(200, "foo".getBytes());
            return Futures.completedFuture();
        });
        mockMvc.perform(request).addExpect(r -> {
            Assertions.assertEquals("foo", r.result());
            assertSame(request, r.request());
            assertNotNull(r.response());
            Assertions.assertTrue(r.response().isCommitted());
            assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes((ByteBuf) BufferUtil.unwrap(
                    r.response().getSentData())));
        }).then(r -> {
            Assertions.assertEquals("foo", r.result());
            assertSame(request, r.request());
            assertNotNull(r.response());
            Assertions.assertTrue(r.response().isCommitted());
            assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes((ByteBuf) BufferUtil.unwrap(
                    r.response().getSentData())));
        });
    }

    @Test
    void testPerformAsync() {
        final RestlightHandler<RequestContext> handler = mock(RestlightHandler.class);
        final DefaultMockMvc mockMvc = new DefaultMockMvc(handler);

        final MockHttpRequest request = MockHttpRequest.aMockRequest().build();
        when(handler.process(any())).then(mock -> {
            final HttpRequest req = mock.getArgument(0, RequestContext.class).request();
            req.setAttribute(DefaultMockMvc.RETURN_VALUE_KEY, Futures.completedFuture("foo"));
            return Futures.completedFuture();
        });
        mockMvc.perform(request)
                .addExpect(r -> Assertions.assertEquals("foo", r.result()))
                .then(r -> Assertions.assertEquals("foo", r.result()));
    }


}
