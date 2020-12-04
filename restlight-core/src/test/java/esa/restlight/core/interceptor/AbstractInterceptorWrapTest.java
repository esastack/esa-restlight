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
package esa.restlight.core.interceptor;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractInterceptorWrapTest {

    @Test
    void testWrap() throws Exception {
        final InterceptorPredicate p = request -> true;
        final InternalInterceptor mock = mock(InternalInterceptor.class);
        final AbstractInterceptorWrap<InternalInterceptor> wrap =
                new AbstractInterceptorWrap<InternalInterceptor>(mock) {
                    @Override
                    public InterceptorPredicate predicate() {
                        return p;
                    }
                };

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Object handler = new Object();

        when(mock.preHandle(request, response, handler)).thenReturn(true);
        when(mock.preHandle0(request, response, handler)).thenReturn(CompletableFuture.completedFuture(false));
        assertTrue(wrap.preHandle(request, response, handler));
        assertFalse(wrap.preHandle0(request, response, handler).join());
        verify(mock, times(1)).preHandle(request, response, handler);
        verify(mock, times(1)).preHandle0(request, response, handler);

        wrap.postHandle(request, response, handler);
        wrap.postHandle0(request, response, handler);
        verify(mock, times(1)).postHandle(request, response, handler);
        verify(mock, times(1)).postHandle0(request, response, handler);

        final Exception e = new Exception();
        wrap.afterCompletion(request, response, handler, e);
        wrap.afterCompletion0(request, response, handler, e);
        verify(mock, times(1)).afterCompletion(request, response, handler, e);
        verify(mock, times(1)).afterCompletion0(request, response, handler, e);

        when(mock.getOrder()).thenReturn(1);
        assertEquals(1, wrap.getOrder());
        verify(mock, times(1)).getOrder();

        when(mock.toString()).thenReturn("foo");
        assertEquals("foo", wrap.toString());
    }

}
