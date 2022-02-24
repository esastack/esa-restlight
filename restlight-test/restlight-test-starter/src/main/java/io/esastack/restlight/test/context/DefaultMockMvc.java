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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.mock.MockResponseContent;
import io.esastack.restlight.server.schedule.AbstractRestlightHandler;
import io.esastack.restlight.test.result.DefaultMvcResult;
import io.esastack.restlight.test.result.MvcResult;
import io.esastack.restlight.test.result.ResultActions;
import io.esastack.restlight.test.result.ResultHandler;
import io.esastack.restlight.test.result.ResultMatcher;

public class DefaultMockMvc implements MockMvc {

    private final AbstractRestlightHandler handler;

    public DefaultMockMvc(AbstractRestlightHandler handler) {
        this.handler = handler;
    }

    @Override
    public ResultActions perform(MockHttpRequest request) {
        Buffer buffer = Buffer.defaultAlloc().buffer();

        final MockHttpResponse response = MockHttpResponse.aMockResponse(buffer).build();
        RequestContext context = new RequestContextImpl(request, response);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(new MockResponseContent(buffer));

        handler.process(context).toCompletableFuture().join();
        return new DefaultResultActions(new DefaultMvcResult(request, response));
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
