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

import esa.commons.Checks;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;

public class DefaultMvcResult implements MvcResult {

    private final MockAsyncRequest request;
    private final MockAsyncResponse response;
    private final Object result;

    public DefaultMvcResult(MockAsyncRequest request, MockAsyncResponse response, Object result) {
        Checks.checkNotNull(request, "MockAsyncRequest must not be null!");
        Checks.checkNotNull(response, "MockAsyncResponse must not be null!");
        this.request = request;
        this.response = response;
        this.result = result;
    }

    @Override
    public MockAsyncRequest request() {
        return request;
    }

    @Override
    public MockAsyncResponse response() {
        return response;
    }

    @Override
    public Object result() {
        return result;
    }
}
