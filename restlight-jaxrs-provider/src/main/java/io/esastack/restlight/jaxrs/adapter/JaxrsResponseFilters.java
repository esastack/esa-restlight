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
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterChain;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.container.ContainerResponseContextImpl;
import io.esastack.restlight.jaxrs.impl.container.ResponseContainerContext;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.resolver.ResponseEntityStreamChannelImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.util.concurrent.CompletableFuture;

public class JaxrsResponseFilters implements RouteFilter {

    private final ContainerResponseFilter[] filters;

    public JaxrsResponseFilters(ContainerResponseFilter[] filters) {
        Checks.checkNotNull(filters, "filters");
        this.filters = filters;
    }

    @Override
    public CompletableFuture<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
        return next.doNext(mapping, context).thenCompose(v -> {
            final ResponseImpl rsp = JaxrsContextUtils.getResponse(context);
            RuntimeDelegateUtils.addMetadataToJakarta(context.response(), rsp);
            final ContainerRequestContext reqCtx = new ResponseContainerContext(JaxrsContextUtils
                    .getRequestContext(context));
            final ContainerResponseContextImpl rspCtx = new ContainerResponseContextImpl(
                    ResponseEntityStreamChannelImpl.get(context).outputStream(), rsp);
            for (ContainerResponseFilter filter : filters) {
                try {
                    filter.filter(reqCtx, rspCtx);
                } catch (Throwable th) {
                    RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
                    return Futures.completedExceptionally(th);
                }
            }
            RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
            return Futures.completedFuture();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1000;
    }
}

