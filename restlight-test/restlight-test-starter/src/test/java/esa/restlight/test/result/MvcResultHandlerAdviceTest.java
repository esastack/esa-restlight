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
package esa.restlight.test.result;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import static esa.restlight.test.context.DefaultMockMvc.RETURN_VALUE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MvcResultHandlerAdviceTest {

    @Test
    void testInvoke() throws Throwable {
        final MvcResultHandlerAdvice advice = new MvcResultHandlerAdvice();
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        assertEquals("foo", advice.invoke(request,
                MockAsyncResponse.aMockResponse().build(),
                null,
                (req, res, args) -> "foo"));

        assertEquals("foo", request.getAttribute(RETURN_VALUE_KEY));
    }

}
