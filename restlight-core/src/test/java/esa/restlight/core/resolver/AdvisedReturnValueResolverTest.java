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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdvisedReturnValueResolverTest
 */
class AdvisedReturnValueResolverTest {

    @Test
    void testAdvise() throws Exception {
        final ReturnValueResolver resolver = mock(ReturnValueResolver.class);
        when(resolver.resolve(any(), any(), any()))
                .then(invocationOnMock -> String.valueOf(invocationOnMock.getArguments()[0]).getBytes());

        final ReturnValueResolverAdvice a0 = mock(ReturnValueResolverAdvice.class);
        when(a0.beforeResolve(any(), any(), any())).thenReturn("foo");
        final ReturnValueResolverAdvice a1 = mock(ReturnValueResolverAdvice.class);
        when(a1.beforeResolve(any(), any(), any())).thenReturn("bar");

        final AdvisedReturnValueResolver advise = new AdvisedReturnValueResolver(resolver, Arrays.asList(a0, a1));
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

        assertArrayEquals("bar".getBytes(), advise.resolve("useless", request, response));
        verify(resolver, times(1)).resolve(eq("bar"), same(request), same(response));
        verify(a0, times(1)).beforeResolve(eq("useless"), same(request), same(response));
        verify(a1, times(1)).beforeResolve(eq("foo"), same(request), same(response));
    }

}
