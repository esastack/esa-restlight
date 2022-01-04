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

import esa.commons.Checks;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;

public class DefaultMvcResult implements MvcResult {

    private final MockHttpRequest request;
    private final MockHttpResponse response;

    public DefaultMvcResult(MockHttpRequest request, MockHttpResponse response) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        this.request = request;
        this.response = response;
    }

    @Override
    public MockHttpRequest request() {
        return request;
    }

    @Override
    public MockHttpResponse response() {
        return response;
    }

}
