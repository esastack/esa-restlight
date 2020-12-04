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

import com.google.common.util.concurrent.ListenableFuture;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.util.FutureUtils;
import io.netty.util.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Implementation of {@link esa.restlight.server.route.Execution} for error scenes.
 */
public class ExceptionHandlerExecution extends AbstractHandlerExecution<HandlerAdapter> {

    private final Function<Object, CompletableFuture<Object>> futureConverter;
    private final Throwable t;

    @SuppressWarnings("unchecked")
    public ExceptionHandlerExecution(HandlerAdapter handlerAdapter, Throwable ex) {
        super(handlerAdapter);
        this.t = ex;
        final Class<?> type = handlerAdapter.handler().method().getReturnType();
        if (CompletableFuture.class.isAssignableFrom(type)) {
            futureConverter = obj -> (CompletableFuture) obj;
        } else if (FutureUtils.hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(type)) {
            futureConverter = obj -> FutureUtils.transferListenableFuture((ListenableFuture) obj);
        } else if (Future.class.isAssignableFrom(type)) {
            futureConverter = obj -> FutureUtils.transferNettyFuture((Future) obj);
        } else {
            futureConverter = CompletableFuture::completedFuture;
        }
    }

    @Override
    protected Object resolveFixedArg(MethodParam parameter,
                                     AsyncRequest request,
                                     AsyncResponse response) {
        if (parameter.type().isInstance(t)) {
            return t;
        }
        return super.resolveFixedArg(parameter, request, response);
    }

    @Override
    protected CompletableFuture<Object> transferToFuture(Object returnValue) {
        return futureConverter.apply(returnValue);
    }
}
