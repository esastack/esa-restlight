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
import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.container.ContainerResponseContextImpl;
import io.esastack.restlight.jaxrs.impl.container.ResponseContainerContext;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.spi.ExceptionHandler;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class GlobalResponseFiltersAdapter implements ExceptionHandler {

    private final ContainerResponseFilter[] filters;

    public GlobalResponseFiltersAdapter(ContainerResponseFilter[] filters) {
        Checks.checkNotNull(filters, "filters");
        this.filters = filters;
    }

    @Override
    public CompletableFuture<Void> handle(RequestContext context, Throwable th,
                                          ExceptionHandlerChain next) {
        ResponseImpl rsp = JaxrsContextUtils.getResponse(context);
        RuntimeDelegateUtils.addMetadataToJakarta(context.response(), rsp);
        final ContainerRequestContext reqCtx = new ResponseContainerContext(JaxrsContextUtils
                .getRequestContext(context));
        final ContainerResponseContextImpl rspCtx = new ContainerResponseContextImpl(context, rsp);
        for (ContainerResponseFilter filter : filters) {
            try {
                filter.filter(reqCtx, rspCtx);
            } catch (Throwable ex) {
                RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
                return next.handle(context, ex);
            }
        }
        RuntimeDelegateUtils.addMetadataToNetty(rsp, context.response(), true);
        return next.handle(context, th);
    }
}

