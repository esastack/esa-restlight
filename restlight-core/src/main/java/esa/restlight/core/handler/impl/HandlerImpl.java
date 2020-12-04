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
package esa.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.core.method.InvocableMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Default implementation of {@link Handler}.
 */
public class HandlerImpl implements Handler {

    private final InvocableMethod handler;
    private final HandlerInvoker invoker;
    private final HttpResponseStatus customResponse;


    public HandlerImpl(InvocableMethod handler) {
        this(handler, null, new HandlerInvokerImpl(handler));
    }

    public HandlerImpl(InvocableMethod handler,
                       HttpResponseStatus customResponse,
                       HandlerInvoker invoker) {
        Checks.checkNotNull(handler, "handler");
        this.handler = handler;
        this.customResponse = customResponse;
        this.invoker = invoker;
    }

    @Override
    public Object invoke(AsyncRequest request, AsyncResponse response, Object[] args) throws Throwable {
        return invoker.invoke(request, response, args);
    }

    @Override
    public InvocableMethod handler() {
        return handler;
    }

    @Override
    public HttpResponseStatus customResponse() {
        return customResponse;
    }

    @Override
    public String toString() {
        return StringUtils.concat("{Handler => ", handler.beanType().getName(), ", Method => ",
                handler.method().toGenericString(), "}");
    }
}
