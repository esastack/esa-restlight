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
package esa.restlight.test.context;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.util.FutureUtils;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import esa.restlight.test.result.DefaultMvcResult;
import esa.restlight.test.result.MvcResult;
import esa.restlight.test.result.ResultActions;
import esa.restlight.test.result.ResultHandler;
import esa.restlight.test.result.ResultMatcher;

public class DefaultMockMvc implements MockMvc {

    public static final String RETURN_VALUE_KEY = "$mock.result";

    private final RestlightHandler handler;

    public DefaultMockMvc(RestlightHandler handler) {
        this.handler = handler;
    }

    @Override
    public ResultActions perform(MockAsyncRequest request) {
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        handler.process(request, response).join();
        return new DefaultResultActions(new DefaultMvcResult(request, response, getResultAndClear(request)));
    }

    private Object getResultAndClear(AsyncRequest request) {
        return FutureUtils.getFutureResult(request.removeAttribute(RETURN_VALUE_KEY));
    }

    private static class DefaultResultActions implements ResultActions {

        private final MvcResult result;

        private DefaultResultActions(MvcResult result) {
            Checks.checkNotNull(result, "MvcResult must not be null");
            this.result = result;
        }

        @Override
        public ResultActions addExpect(ResultMatcher matcher) {
            matcher.match(result);
            return this;
        }

        @Override
        public ResultActions then(ResultHandler handler) {
            handler.handle(result);
            return this;
        }

        @Override
        public MvcResult result() {
            return result;
        }
    }

}
