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
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContextImpl;
import io.esastack.restlight.core.spi.FilterFactory;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.util.ErrorDetail;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ResponseEntityWriterFilterFactory implements FilterFactory {

    @Override
    public Optional<Filter> filter(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new ResponseEntityWriterFilter(ctx));
    }

    private static class ResponseEntityWriterFilter implements Filter {

        private static final Logger logger = LoggerFactory.getLogger(ResponseEntityWriterFilter.class);

        private final DeployContext<? extends RestlightOptions> ctx;
        private HandlerResolverFactory resolverFactory;

        public ResponseEntityWriterFilter(DeployContext<? extends RestlightOptions> ctx) {
            Checks.checkNotNull(ctx, "ctx");
            this.ctx = ctx;
        }

        @Override
        public CompletableFuture<Void> doFilter(FilterContext context, FilterChain chain) {
            HandlerResolverFactory resolverFactory = getResolverFactory();
            return chain.doFilter(context).whenComplete((v, th) -> {
                if (th != null) {
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    context.response().status(status.code());
                    context.response().entity(new ErrorDetail<>(context.request().path(), status.reasonPhrase()));
                }

                final HandlerMethod method = ResponseEntityUtils.getHandledMethod(context);
                final ResponseEntity entity = new ResponseEntityImpl(method, context.response());
                final ResponseEntityResolverContext rspCtx = new ResponseEntityResolverContextImpl(context,
                        entity, resolverFactory.getResponseEntityResolvers(),
                        resolverFactory.getResponseEntityResolverAdvices(entity)
                                .toArray(new ResponseEntityResolverAdvice[0]));
                final HttpRequest request = context.request();
                try {
                    rspCtx.proceed();
                    if (!rspCtx.channel().isCommitted()) {
                        logger.error("There is no suitable ResponseEntityResolver to write response entity: {}," +
                                        " request(url={}, method={})", entity, request.path(), request.method());
                        rspCtx.context().response().status(HttpStatus.NOT_ACCEPTABLE.code());
                        rspCtx.channel().end();
                    }
                } catch (Throwable ex) {
                    if (!rspCtx.channel().isCommitted()) {
                        logger.error("Error occurred when writing response entity: {}, request(url={}, method={})",
                                entity, request.path(), request.method(), ex);
                        rspCtx.context().response().status(HttpStatus.INTERNAL_SERVER_ERROR.code());
                        rspCtx.channel().end();
                    } else {
                        logger.error("Unexpected error occurred after committing response entity: {}," +
                                " request(url={}, method={})", entity, request.path(), request.method(), th);
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

        private HandlerResolverFactory getResolverFactory() {
            if (resolverFactory == null) {
                assert ctx.resolverFactory().isPresent();
                resolverFactory = ctx.resolverFactory().get();
            }
            return resolverFactory;
        }
    }
}

