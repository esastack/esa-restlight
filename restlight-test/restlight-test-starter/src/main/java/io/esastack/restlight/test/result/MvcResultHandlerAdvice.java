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

import esa.commons.annotation.Internal;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.test.context.DefaultMockMvc;
import io.esastack.restlight.test.mock.MockHttpRequest;

@Internal
public class MvcResultHandlerAdvice implements HandlerAdvice {

    @Override
    public Object invoke(RequestContext context, Object[] args, HandlerInvoker invoker) throws Throwable {
        Object result = invoker.invoke(context, args);
        if (context.request() instanceof MockHttpRequest) {
            context.request().setAttribute(DefaultMockMvc.RETURN_VALUE_KEY, result);
        }
        return result;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
