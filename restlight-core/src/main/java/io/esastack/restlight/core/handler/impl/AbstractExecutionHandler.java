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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.route.ExecutionHandler;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract implementation of {@link ExecutionHandler} which handles segment {@link Mapping} by instantiating bean
 * (if necessary), resolving arguments, invoking and handling return value step by step.
 *
 * @param <H> Handler type
 */
abstract class AbstractExecutionHandler<H extends HandlerMethodAdapter> implements
        ExecutionHandler<RequestContext> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExecutionHandler.class);

    private final FutureTransfer transfer;
    private final HandlerValueResolver handlerResolver;
    private final H handlerMethod;

    AbstractExecutionHandler(HandlerValueResolver handlerResolver,
                             H handlerMethod) {
        Checks.checkNotNull(handlerResolver, "handlerResolver");
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        assert handlerMethod.context().resolverFactory().isPresent();
        this.handlerResolver = handlerResolver;
        this.transfer = ((HandlerResolverFactory) handlerMethod.context().resolverFactory().get())
                .getFutureTransfer(handlerMethod.handlerMethod());
        this.handlerMethod = handlerMethod;
    }

    @Override
    public CompletableFuture<Void> handle(RequestContext context) {
        try {
            final Object object = resolveBean(handlerMethod.handlerMethod(), context);
            final Object[] args = resolveArgs(context);
            return invoke(context, object, args)
                    .thenCompose(current -> resolveReturnValue(object, context));
        } catch (Throwable th) {
            return Futures.completedExceptionally(th);
        }
    }

    /**
     * Resolves the handler object by given {@link RequestContext} and {@link Object}.
     *
     * @param handler handler
     * @param context context
     * @return resolved object
     */
    protected abstract Object resolveBean(HandlerMethod handler, RequestContext context);

    /**
     * Builds a {@link HandlerInvoker} to handle the {@link RequestContext} by given {@code handler} and {@code bean}.
     *
     * @param handlerMethod handler method
     * @param instance      current instance
     * @return handler invoker
     */
    protected abstract HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance);

    @SuppressWarnings("unchecked")
    protected Object[] resolveArgs(RequestContext context) {
        final ResolvableParam<MethodParam, ResolverWrap>[] params = handlerMethod.paramResolvers();
        final Object[] args = new Object[params.length];
        //resolve parameters one by one
        for (int i = 0; i < params.length; i++) {
            ResolvableParam<MethodParam, ResolverWrap> resolvable = params[i];
            final MethodParam param = resolvable.param();
            args[i] = resolveFixedArg(param, context);
            //resolve args with resolver
            if (args[i] == null) {
                if (resolvable.resolver() != null) {
                    //it may return a null value
                    try {
                        args[i] = resolvable.resolver().resolve(handlerMethod.context(), param, context);
                    } catch (Exception e) {
                        //wrap exception
                        throw WebServerException.wrap(e);
                    }
                    continue;
                }
                if (args[i] == null) {
                    throw WebServerException.badRequest(
                            StringUtils.concat("Could not resolve method parameter at index ",
                                    String.valueOf(param.index()), " in ",
                                    param.method() + ": No suitable resolver for argument of type '",
                                    param.type().getName(), "'"));
                }
            }
        }
        return args;
    }

    protected Object resolveFixedArg(MethodParam parameter,
                                     RequestContext context) {
        return null;
    }

    protected CompletableFuture<Object> invoke(RequestContext context, Object bean, Object[] args) {
        CompletableFuture<Object> future;
        try {
            final Object returnValue = getInvoker(handlerMethod.handlerMethod(), bean).invoke(context, args);
            if (handlerMethod.isConcurrent() && returnValue == null) {
                // null return value in handler controller is not allowed
                logger.error(getDetailedMessage("Unexpected null return value of concurrent handler."));
                HttpResponse response = context.response();
                response.status(HttpStatus.INTERNAL_SERVER_ERROR.code());
                return Futures.completedFuture();
            } else {
                future = transfer.transferTo(context, returnValue);
            }
        } catch (Throwable throwable) {
            if (logger.isDebugEnabled()) {
                logger.debug(getDetailedMessage("Error while invoking handler method."), throwable);
            }
            // transfer error thrown by the controller to a CompletableFuture whatever this controller is an
            // asynchronous handler or not('cause error is not expected in an asynchronous implementation).
            future = Futures.completedExceptionally(throwable);
        }
        return future;
    }

    H handlerMethod() {
        return handlerMethod;
    }

    CompletableFuture<Void> resolveReturnValue(Object value, RequestContext context) {
        return handlerResolver.handle(value, context);
    }

    private String getDetailedMessage(String text) {
        return StringUtils.concat(text, "\n", handlerMethod.toString());
    }
}
