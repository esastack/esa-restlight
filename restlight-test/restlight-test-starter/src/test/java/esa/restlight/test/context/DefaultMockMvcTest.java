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
package esa.restlight.test.context;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMockMvcTest {

    @Test
    void testPerform() {
        final RestlightHandler handler = mock(RestlightHandler.class);
        final DefaultMockMvc mockMvc = new DefaultMockMvc(handler);

        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        when(handler.process(same(request), any())).then(mock -> {
            final AsyncRequest req = mock.getArgument(0, AsyncRequest.class);
            final AsyncResponse res = mock.getArgument(1, AsyncResponse.class);
            req.setAttribute(DefaultMockMvc.RETURN_VALUE_KEY, "foo");
            res.sendResult(200, "foo".getBytes());
            return Futures.completedFuture();
        });
        mockMvc.perform(request).addExpect(r -> {
            assertEquals("foo", r.result());
            assertSame(request, r.request());
            assertNotNull(r.response());
            assertTrue(r.response().isCommitted());
            assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes(r.response().getSentData()));
        }).then(r -> {
            assertEquals("foo", r.result());
            assertSame(request, r.request());
            assertNotNull(r.response());
            assertTrue(r.response().isCommitted());
            assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes(r.response().getSentData()));
        });
    }

    @Test
    void testPerformAsync() {
        final RestlightHandler handler = mock(RestlightHandler.class);
        final DefaultMockMvc mockMvc = new DefaultMockMvc(handler);

        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        when(handler.process(same(request), any())).then(mock -> {
            final AsyncRequest req = mock.getArgument(0, AsyncRequest.class);
            req.setAttribute(DefaultMockMvc.RETURN_VALUE_KEY, Futures.completedFuture("foo"));
            return Futures.completedFuture();
        });
        mockMvc.perform(request)
                .addExpect(r -> assertEquals("foo", r.result()))
                .then(r -> assertEquals("foo", r.result()));
    }


}
