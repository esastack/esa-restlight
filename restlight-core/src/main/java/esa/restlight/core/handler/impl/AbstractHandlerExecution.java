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

import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.server.route.Execution;
import esa.restlight.server.util.Futures;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract implementation of {@link Execution} which handles a request by resolve arguments, invoke, handle return
 * value order.
 *
 * @param <H> Handler type
 */
public abstract class AbstractHandlerExecution<H extends HandlerAdapter> implements Execution {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractRouteExecution.class);

    final H handlerAdapter;

    AbstractHandlerExecution(H handlerAdapter) {
        this.handlerAdapter = handlerAdapter;
    }

    @Override
    public CompletableFuture<Void> handle(AsyncRequest request, AsyncResponse response) {
        try {
            final Object[] args = resolveArguments(request, response);
            return invoke(request, response, args)
                    .thenAccept(returnValue ->
                            handleReturnValue(returnValue, request, response));
        } catch (Throwable t) {
            return Futures.completedExceptionally(t);
        }
    }

    protected Object[] resolveArguments(AsyncRequest request, AsyncResponse response) {
        HandlerAdapter.ResolvableParam[] params = handlerAdapter.params();
        Object[] args = new Object[params.length];
        //resolve parameters one by one
        for (int i = 0; i < params.length; i++) {
            HandlerAdapter.ResolvableParam resolvable = params[i];
            args[i] = resolveFixedArg(resolvable.param, request, response);
            //resolve args with resolver
            if (args[i] == null) {
                if (resolvable.resolver != null) {
                    //it may return a null value
                    try {
                        args[i] = resolvable.resolver.resolve(request, response);
                    } catch (Exception e) {
                        //wrap exception
                        throw WebServerException.wrap(e);
                    }
                    continue;
                }
                if (args[i] == null) {
                    throw WebServerException.badRequest(
                            StringUtils.concat("Could not resolve method parameter at index ",
                                    String.valueOf(resolvable.param.index()), " in ",
                                    resolvable.param.method() + ": No suitable resolver for argument of type '",
                                    resolvable.param.type().getName(), "'"));
                }
            }
        }
        return args;
    }

    protected Object resolveFixedArg(MethodParam parameter,
                                     AsyncRequest request,
                                     AsyncResponse response) {
        return null;
    }


    protected CompletableFuture<Object> invoke(AsyncRequest request, AsyncResponse response, Object[] args) {
        CompletableFuture<Object> future;
        try {
            final Object returnValue = handlerAdapter.invoke(request, response, args);
            if (handlerAdapter.isConcurrent() && returnValue == null) {
                // null return value in handler controller is not allowed
                logger.error(getDetailedMessage("Unexpected null return value of concurrent handler."));
                if (!response.isCommitted()) {
                    response.sendResult(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                }
                return Futures.completedFuture();
            } else {
                future = transferToFuture(returnValue);
            }
        } catch (Throwable throwable) {
            logger.error(getDetailedMessage("Error while invoking handler method."), throwable);
            // transfer error thrown by the controller to a CompletableFuture whatever this controller is an
            // asynchronous handler or not('cause error is not expected in an asynchronous implementation).
            future = Futures.completedExceptionally(throwable);
        }
        return future;
    }

    protected abstract CompletableFuture<Object> transferToFuture(Object returnValue);


    protected void handleReturnValue(Object returnValue, AsyncRequest request, AsyncResponse response) {

        if (this.handlerAdapter.hasCustomResponse()) {
            response.setStatus(handlerAdapter.customResponse().code());
        }

        if (!response.isCommitted()) {
            byte[] result;
            try {
                result = handlerAdapter.returnValueResolver()
                        .resolve(returnValue, request, response);
            } catch (Exception e) {
                // wrapIfNecessary
                throw new WebServerException("Error while resolving return value: " + e.getMessage(), e);
            }
            if (!Serializers.alreadyWrite(result)) {
                if (response.isCommitted()) {
                    logger.warn(getDetailedMessage("Ignore the non-null return value '{}', because response is " +
                            "not writable."), returnValue);
                }
                response.sendResult(result);
            }
        }
    }

    String getDetailedMessage(String text) {
        return StringUtils.concat(text, "\n", handlerAdapter.toString());
    }
}
