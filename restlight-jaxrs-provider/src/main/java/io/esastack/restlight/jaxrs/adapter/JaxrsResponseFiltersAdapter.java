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
import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterChain;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.container.ContainerResponseContextImpl;
import io.esastack.restlight.jaxrs.impl.container.ResponseContainerContext;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class JaxrsResponseFiltersAdapter implements Filter {

    private static final AttributeKey<ContainerResponseFilter[]> HAS_BOUND_FILTERS =
            AttributeKey.valueOf("$bound_filters");

    private final ContainerResponseFilter[] filters;

    public JaxrsResponseFiltersAdapter(ContainerResponseFilter[] filters) {
        Checks.checkNotNull(filters, "filters");
        this.filters = filters;
    }

    @Override
    public CompletionStage<Void> doFilter(FilterContext context, FilterChain chain) {
        return chain.doFilter(context).thenCompose(v -> applyResponseFilters(context, getBoundFilters(context)));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private ContainerResponseFilter[] getBoundFilters(RequestContext context) {
        ContainerResponseFilter[] bound;
        if ((bound = context.attrs().attr(HAS_BOUND_FILTERS).getAndRemove()) != null) {
            return bound;
        } else {
            return filters;
        }
    }

    private CompletableFuture<Void> applyResponseFilters(RequestContext context, ContainerResponseFilter[] filters) {
        if (!isSuccess(context) || filters == null || filters.length == 0) {
            return Futures.completedFuture();
        }
        ResponseImpl rsp = JaxrsContextUtils.getResponse(context);
        RuntimeDelegateUtils.addMetadataToJakarta(context.response(), rsp);
        final ContainerRequestContext reqCtx = new ResponseContainerContext(JaxrsContextUtils
                .getRequestContext(context));
        final ContainerResponseContextImpl rspCtx = new ContainerResponseContextImpl(
                ResponseEntityStreamClose.getNonClosableOutputStream(context), rsp);
        for (ContainerResponseFilter filter : filters) {
            try {
                filter.filter(reqCtx, rspCtx);
            } catch (Throwable ex) {
                RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
                return Futures.completedExceptionally(ex);
            }
        }
        RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
        return Futures.completedFuture();
    }

    private boolean isSuccess(RequestContext context) {
        return context.response().status() < 400;
    }

    static class ContainerResponseFilterBinder implements RouteFilter {

        private final ContainerResponseFilter[] filters;

        ContainerResponseFilterBinder(ContainerResponseFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
            context.attrs().attr(HAS_BOUND_FILTERS).set(filters);
            return next.doNext(mapping, context);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}

