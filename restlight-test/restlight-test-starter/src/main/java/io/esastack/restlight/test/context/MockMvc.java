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
package io.esastack.restlight.test.context;

import io.esastack.restlight.test.mock.MockHttpRequest;
import io.esastack.restlight.test.result.ResultActions;

public interface MockMvc {

    /**
     * Perform a request and return the type which allows chaining further actions.
     *
     * @param request mockRequest
     * @return actions allow chaining use.
     */
    ResultActions perform(MockHttpRequest request);

}
