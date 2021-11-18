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
import io.esastack.restlight.core.context.FilterContext;
import io.esastack.restlight.core.handler.impl.HandlerMethodValueResolver;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContextImpl;
import io.esastack.restlight.core.spi.Filter;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.handler.FilterChain;

import java.util.concurrent.CompletableFuture;

public class ResponseEntityWriterFilter implements Filter {

    private final HandlerResolverFactory resolverFactory;

    public ResponseEntityWriterFilter(HandlerResolverFactory resolverFactory) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        this.resolverFactory = resolverFactory;
    }

    @Override
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain<FilterContext> chain) {
        return chain.doFilter(context).thenApply(v -> {
            if (!context.response().isCommitted()) {
                HandlerMethod method = context.getUncheckedAttribute(HandlerMethodValueResolver.HANDLER_METHOD);
                ResponseEntity entity = new ResponseEntityImpl(method, context.response());
                ResponseEntityResolverContext rspCtx = new ResponseEntityResolverContextImpl(context,
                        entity, resolverFactory.getResponseEntityResolvers(),
                        resolverFactory.getResponseEntityResolverAdvices(entity)
                                .toArray(new ResponseEntityResolverAdvice[0]));
                try {
                    rspCtx.proceed();
                } catch (Throwable th) {
                    // wrapIfNecessary
                    throw new WebServerException("Error while resolving return value: " + th.getMessage(), th);
                }
            }
            return v;
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

