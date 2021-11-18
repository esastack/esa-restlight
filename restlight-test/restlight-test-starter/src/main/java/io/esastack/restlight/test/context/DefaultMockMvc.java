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

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.HttpResponseAdapter;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.util.FutureUtils;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.test.mock.MockHttpRequest;
import io.esastack.restlight.test.mock.MockHttpResponse;
import io.esastack.restlight.test.result.DefaultMvcResult;
import io.esastack.restlight.test.result.MvcResult;
import io.esastack.restlight.test.result.ResultActions;
import io.esastack.restlight.test.result.ResultHandler;
import io.esastack.restlight.test.result.ResultMatcher;

public class DefaultMockMvc implements MockMvc {

    public static final String RETURN_VALUE_KEY = "$mock.result";

    private final RestlightHandler<RequestContext> handler;

    public DefaultMockMvc(RestlightHandler<RequestContext> handler) {
        this.handler = handler;
    }

    @Override
    public ResultActions perform(MockHttpRequest request) {
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        handler.process(new RequestContextImpl(request, new HttpResponseAdapter(response))).join();
        return new DefaultResultActions(new DefaultMvcResult(request, response, getResultAndClear(request)));
    }

    private Object getResultAndClear(HttpRequest request) {
        return FutureUtils.getFutureResult(request.removeAttribute(RETURN_VALUE_KEY));
    }

    private static class DefaultResultActions implements ResultActions {

        private final MvcResult result;

        private DefaultResultActions(MvcResult result) {
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
