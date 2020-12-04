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
package esa.restlight.core.resolver;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdvisedArgumentResolverTest
 */
class AdvisedArgumentResolverTest {

    @Test
    void testAdvice() throws Exception {

        final ArgumentResolver argumentResolver = mock(ArgumentResolver.class);
        when(argumentResolver.resolve(any(), any())).thenReturn("foo");

        final ArgumentResolverAdvice a0 = mock(ArgumentResolverAdvice.class);
        when(a0.afterResolved(any(), any(), any())).thenReturn("bar");
        final ArgumentResolverAdvice a1 = mock(ArgumentResolverAdvice.class);
        when(a1.afterResolved(any(), any(), any())).thenReturn("baz");

        final AdvisedArgumentResolver advise = new AdvisedArgumentResolver(argumentResolver, Arrays.asList(a0, a1));

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Object arg = advise.resolve(request, response);
        assertEquals("baz", arg);

        verify(argumentResolver, times(1)).resolve(same(request), same(response));
        verify(a0, times(1)).beforeResolve(same(request), same(response));
        verify(a1, times(1)).beforeResolve(same(request), same(response));
        verify(a0, times(1)).afterResolved(eq("foo"), same(request), same(response));
        verify(a1, times(1)).afterResolved(eq("bar"), same(request), same(response));
    }

}
