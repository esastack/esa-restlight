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

import esa.commons.annotation.Internal;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.test.mock.MockAsyncRequest;

import static esa.restlight.test.context.DefaultMockMvc.RETURN_VALUE_KEY;

/**
 */
@Internal
public class MvcResultHandlerAdvice implements HandlerAdvice {
    @Override
    public Object invoke(AsyncRequest request, AsyncResponse response, Object[] args,
                         HandlerInvoker invoker) throws Throwable {
        Object result = invoker.invoke(request, response, args);
        if (request instanceof MockAsyncRequest) {
            request.setAttribute(RETURN_VALUE_KEY, result);
        }
        return result;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
