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
package io.esastack.restlight.test.result;

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import static io.esastack.restlight.test.context.DefaultMockMvc.RETURN_VALUE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MvcResultHandlerAdviceTest {

    @Test
    void testInvoke() throws Throwable {
        final MvcResultHandlerAdvice advice = new MvcResultHandlerAdvice();
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        RequestContext context = new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build());
        assertEquals("foo", advice.invoke(context,
                null,
                (ctx, args) -> "foo"));

        assertEquals("foo", context.attrs().attr(RETURN_VALUE_KEY).get());
    }

}
