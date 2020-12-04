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
package esa.restlight.server.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilteredHandlerTest {

    @Test
    void testDelegate() {
        final RestlightHandler delegate = mock(RestlightHandler.class);
        assertThrows(IllegalArgumentException.class, () -> new FilteredHandler(delegate, Collections.emptyList()));

        final Filter filter1 = mock(Filter.class);
        final Filter filter2 = mock(Filter.class);

        final FilteredHandler handler = new FilteredHandler(delegate, Arrays.asList(filter1, filter2));

        when(delegate.executor()).thenReturn(r -> {
        });
        when(delegate.schedulers()).thenReturn(Collections.emptyList());
        assertSame(delegate.executor(), handler.executor());
        assertSame(delegate.schedulers(), handler.schedulers());

        handler.onStart();
        verify(delegate).onStart();


        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(filter1.onConnected(same(ctx))).thenReturn(true);
        when(filter2.onConnected(same(ctx))).thenReturn(true);
        handler.onConnected(ctx);
        verify(filter1, times(1)).onConnected(same(ctx));
        verify(filter2, times(1)).onConnected(same(ctx));
        verify(delegate, times(1)).onConnected(same(ctx));

        when(delegate.process(any(), any())).thenReturn(Futures.completedFuture());
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        when(filter1.doFilter(same(request), same(response), any()))
                .then(mock -> mock.getArgument(2, FilterChain.class)
                        .doFilter(mock.getArgument(0), mock.getArgument(1)));
        when(filter2.doFilter(same(request), same(response), any()))
                .then(mock -> mock.getArgument(2, FilterChain.class)
                        .doFilter(mock.getArgument(0), mock.getArgument(1)));

        assertTrue(handler.process(request, response).isDone());
        verify(filter1, times(1)).doFilter(same(request), same(response), any());
        verify(filter2, times(1)).doFilter(same(request), same(response), any());
        verify(delegate, times(1)).process(same(request), same(response));

        handler.shutdown();
        verify(filter1, times(1)).shutdown();
        verify(filter2, times(1)).shutdown();
        verify(delegate, times(1)).shutdown();
    }

    @Test
    void testOnConnectReturnsFalseByFilter() {
        final RestlightHandler delegate = mock(RestlightHandler.class);
        final Filter filter1 = mock(Filter.class);
        final Filter filter2 = mock(Filter.class);

        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(filter1.onConnected(same(ctx))).thenReturn(false);
        when(filter2.onConnected(same(ctx))).thenReturn(true);

        final FilteredHandler handler = new FilteredHandler(delegate, Arrays.asList(filter1, filter2));
        handler.onConnected(ctx);

        verify(filter1, times(1)).onConnected(same(ctx));
        verify(filter2, never()).onConnected(same(ctx));
        verify(delegate, never()).onConnected(same(ctx));
    }

    @Test
    void testProcessBreakByFilter() {
        final RestlightHandler delegate = mock(RestlightHandler.class);
        final Filter filter = mock(Filter.class);

        final FilteredHandler handler = new FilteredHandler(delegate, Collections.singletonList(filter));


        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(filter.doFilter(same(request), same(response), any())).thenReturn(Futures.completedFuture());

        assertTrue(handler.process(request, response).isDone());

        verify(filter, times(1)).doFilter(same(request), same(response), any());
        verify(delegate, never()).process(same(request), same(response));
    }

    @Test
    void testProcessBreakByCommittedResponse() {
        final RestlightHandler delegate = mock(RestlightHandler.class);
        final Filter filter = mock(Filter.class);

        final FilteredHandler handler = new FilteredHandler(delegate, Collections.singletonList(filter));


        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        response.sendResult();

        when(filter.doFilter(same(request), same(response), any()))
                .then(mock -> mock.getArgument(2, FilterChain.class)
                        .doFilter(mock.getArgument(0), mock.getArgument(1)));

        assertTrue(handler.process(request, response).isDone());

        verify(filter, times(1)).doFilter(same(request), same(response), any());
        verify(delegate, never()).process(same(request), same(response));
    }

}
