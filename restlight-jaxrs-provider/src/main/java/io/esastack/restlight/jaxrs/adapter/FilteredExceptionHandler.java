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
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.container.ContainerResponseContextImpl;
import io.esastack.restlight.jaxrs.impl.container.ResponseContainerContext;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.util.concurrent.CompletableFuture;

public class FilteredExceptionHandler implements IExceptionHandler {

    private final ContainerResponseFilter[] filters;

    public FilteredExceptionHandler(ContainerResponseFilter[] filters) {
        Checks.checkNotNull(filters, "filters");
        this.filters = filters;
    }

    @Override
    public CompletableFuture<Void> handle(RequestContext context,
                                          Throwable th,
                                          ExceptionHandlerChain next) {
        return next.handle(context, th).thenCompose(v -> applyResponseFilters(context, filters));
    }

    @Override
    public int getOrder() {
        return 1000;
    }

    static CompletableFuture<Void> applyResponseFilters(RequestContext context, ContainerResponseFilter[] filters) {
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
}

