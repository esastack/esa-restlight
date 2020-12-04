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
package esa.restlight.core.handler;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

/**
 * Implementation of {@link HandlerInvoker} which maintains a reference of {@link HandlerAdvice} and a
 * reference of the next {@link HandlerInvoker} which would be passed to the
 * {@link HandlerAdvice#invoke(AsyncRequest, AsyncResponse, Object[], HandlerInvoker)} function of
 * {@link #current} as the third argument.
 */
public class LinkedHandlerInvoker implements HandlerInvoker {

    private final HandlerAdvice current;
    private final HandlerInvoker next;

    private LinkedHandlerInvoker(HandlerAdvice current,
                                 HandlerInvoker next) {
        Checks.checkNotNull(next);
        this.current = current;
        this.next = next;
    }

    public static LinkedHandlerInvoker immutable(HandlerAdvice[] handlerAdvices,
                                                 HandlerInvoker invoker) {
        HandlerInvoker next = invoker;
        LinkedHandlerInvoker chain;
        int i = handlerAdvices.length - 1;
        do {
            chain = new LinkedHandlerInvoker(handlerAdvices[i], next);
            next = chain;
        } while (--i >= 0);
        return chain;
    }

    @Override
    public Object invoke(AsyncRequest request, AsyncResponse response, Object[] args) throws Throwable {
        return current.invoke(request, response, args, next);
    }
}
