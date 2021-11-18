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

import io.esastack.restlight.test.mock.MockHttpRequest;
import io.esastack.restlight.test.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultMvcResultTest {

    @Test
    void testProps() {
        final MockHttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final Object ret = new Object();
        final DefaultMvcResult result = new DefaultMvcResult(request, response, ret);

        assertSame(request, result.request());
        assertSame(response, result.response());
        assertSame(ret, result.result());
    }

}
