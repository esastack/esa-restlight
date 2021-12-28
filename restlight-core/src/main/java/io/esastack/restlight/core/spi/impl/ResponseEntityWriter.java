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
package io.esastack.restlight.core.spi.impl;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContextImpl;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.util.ErrorDetail;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.esastack.restlight.server.util.ErrorDetail.getMessage;

class ResponseEntityWriter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseEntityWriter.class);

    private final DeployContext<? extends RestlightOptions> context;
    private final HandlerContextProvider handlerContextProvider;

    ResponseEntityWriter(DeployContext<? extends RestlightOptions> context) {
        Checks.checkNotNull(context, "context");
        this.handlerContextProvider = context.handlerContextProvider().orElseThrow(() ->
                new IllegalStateException("HandlerContextProvider is absent"));
        this.context = context;
    }

    @Override
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain chain) {
        return chain.doFilter(context).whenComplete((v, th) -> {
            if (th != null) {
                handleException(context, th);
            }

            final HandlerMethod method = ResponseEntityUtils.getHandledMethod(context);
            final List<MediaType> mediaTypes = ResponseEntityUtils.getMediaTypes(context);
            final ResponseEntity entity = new ResponseEntityImpl(method, context.response(), mediaTypes.isEmpty()
                    ? null : mediaTypes.get(0));
            HandlerResolverFactory resolverFactory = getResolverFactory(method);
            final ResponseEntityResolverContext rspCtx = new ResponseEntityResolverContextImpl(context,
                    entity, resolverFactory.getResponseEntityResolvers(),
                    resolverFactory.getResponseEntityResolverAdvices(entity)
                            .toArray(new ResponseEntityResolverAdvice[0]));

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

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void handleException(RequestContext context, Throwable th) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        context.response().status(status.code());
        context.response().entity(new ErrorDetail<>(context.request().path(), getMessage(status, th)));
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
            return this.context.resolverFactory().orElseThrow(() ->
                    new IllegalStateException("HandlerResolverFactory is absent"));
        }

        HandlerContext<?> context = handlerContextProvider.getContext(method);
        if (context != null) {
            return context.resolverFactory().orElseThrow(() ->
                    new IllegalStateException("HandlerResolverFactory is absent"));
        } else {
            return this.context.resolverFactory().orElseThrow(() ->
                    new IllegalStateException("HandlerResolverFactory is absent"));
        }
    }
}
