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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterChain;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.container.AbstractContainerRequestContext;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.ContainerRequestFilter;

import java.util.concurrent.CompletableFuture;

public class PostMatchRequestFilters implements RouteFilter {

    private final ContainerRequestFilter[] filters;

    public PostMatchRequestFilters(ContainerRequestFilter[] filters) {
        Checks.checkNotNull(filters, "filters");
        this.filters = filters;
    }

    @Override
    public CompletableFuture<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
        AbstractContainerRequestContext ctx = JaxrsContextUtils.getRequestContext(context);
        try {
            for (ContainerRequestFilter filter : filters) {
                filter.filter(ctx);
                if (ctx.isAborted()) {
                    break;
                }
            }
        } catch (Throwable th) {
            return Futures.completedExceptionally(th);
        }
        return next.doNext(mapping, context);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

