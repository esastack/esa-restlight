/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContextImpl;
import io.esastack.restlight.core.spi.ResponseEntityChannelFactory;
import io.esastack.restlight.core.spi.impl.RouteTracking;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.server.bootstrap.DispatcherHandlerImpl;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.schedule.AbstractRestlightHandler;
import io.esastack.restlight.server.util.Futures;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class RestlightHandlerImpl extends AbstractRestlightHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestlightHandlerImpl.class);

    private final HandlerResolverFactory resolverFactory;
    private final ResponseEntityChannelFactory channelFactory;
    private final HandlerContextProvider handlerContexts;

    public RestlightHandlerImpl(RestlightHandler underlying,
                                ExceptionHandlerChain handlerChain,
                                ResponseEntityChannelFactory channelFactory,
                                DeployContext context) {
        super(underlying, handlerChain);
        Checks.checkNotNull(channelFactory, "channelFactory");
        Checks.checkNotNull(context, "context");
        this.channelFactory = channelFactory;
        this.resolverFactory = context.resolverFactory().orElseThrow(() ->
                new IllegalStateException("HandlerResolverFactory is absent"));
        this.handlerContexts = context.handlerContexts().orElseThrow(() ->
                new IllegalStateException("HandlerContextProvider is absent"));
    }

    @Override
    public CompletionStage<Void> process(RequestContext context) {
        return super.process(context).whenComplete((v, th) -> {
            if (th != null) {
                DispatcherHandlerImpl.handleException(context, Futures.unwrapCompletionException(th));
            }

            final HandlerMethod method = RouteTracking.matchedMethod(context);
            final List<MediaType> mediaTypes = ResponseEntityUtils.getMediaTypes(context);
            final ResponseEntity entity = new ResponseEntityImpl(method, context.response(),
                    mediaTypes.isEmpty() ? null : mediaTypes.get(0));
            HandlerResolverFactory resolverFactory = getResolverFactory(method);
            final ResponseEntityResolverContext rspCtx = new ResponseEntityResolverContextImpl(context,
                    entity, channelFactory.create(context), resolverFactory.getResponseEntityResolvers(method),
                    resolverFactory.getResponseEntityResolverAdvices(method));

            setEntityTypeIfNecessary(rspCtx, context.response());
            final HttpRequest request = context.request();
            try {
                rspCtx.proceed();
                if (!rspCtx.channel().isCommitted()) {
                    logger.error("The response entity({}) of request(url={}, method={}) still haven't been committed"
                            + " after all ResponseEntity advices, maybe the write operation was terminated by"
                            + " an advice?", entity, request.path(), request.method());
                    rspCtx.context().response().status(HttpStatus.INTERNAL_SERVER_ERROR.code());
                    rspCtx.channel().end();
                }
            } catch (Throwable ex) {
                if (!rspCtx.channel().isCommitted()) {
                    logger.error("Error occurred when writing response entity({}) of request(url={}, method={})",
                            entity, request.path(), request.method(), ex);
                    final HttpStatus status;
                    if (ex instanceof WebServerException) {
                        status = ((WebServerException) ex).status();
                    } else {
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                    }
                    rspCtx.context().response().status(status.code());
                    rspCtx.channel().end();
                } else {
                    logger.error("Unexpected error occurred after committing response entity({})" +
                            " of request(url={}, method={})", entity, request.path(), request.method(), ex);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Request(url={}, method={}) completed. {}",
                        request.path(), request.method(), context.response().status());
            }
        });
    }

    /**
     * The exception occurred before routing should be handled by the {@link ExceptionHandlerChain} which uses
     * custom {@link ExceptionHandler}s as the last of the chain, such as the exception thrown by {@link Filter}
     * should be handled. When the route has matched, all exception should be handled
     * at {@link DispatcherHandlerImpl#service(RequestContext, CompletionStage, Route)} and there is no necessary
     * to be handled here again.
     *
     * @param context context
     * @param th      th
     * @return        whether current request context should be handled or not.
     */
    @Override
    protected boolean isHandleable(RequestContext context, Throwable th) {
        return RouteTracking.matchedMethod(context) == null;
    }

    private void setEntityTypeIfNecessary(ResponseEntityResolverContext rspCtx, HttpResponse response) {
        if (response.entity() != null && rspCtx.httpEntity().type() == null) {
            Class<?> entityType = ClassUtils.getUserType(response.entity());
            rspCtx.httpEntity().type(entityType);
            rspCtx.httpEntity().genericType(ClassUtils.getRawType(entityType));
        }
    }

    private HandlerResolverFactory getResolverFactory(HandlerMethod method) {
        if (method == null) {
            return resolverFactory;
        }

        HandlerContext context = handlerContexts.getContext(method);
        if (context != null) {
            return context.resolverFactory().orElseThrow(() ->
                    new IllegalStateException("HandlerResolverFactory is absent"));
        } else {
            return resolverFactory;
        }
    }

}

