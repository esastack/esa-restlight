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
package io.esastack.restlight.server.handler;

import esa.commons.Checks;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.internal.FilterContextFactory;
import io.esastack.restlight.server.internal.InternalFilter;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.util.Futures;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.esastack.restlight.server.schedule.ScheduledRestlightHandler.handleException;

public class FilteredHandler<CTX extends RequestContext, FCTX extends FilterContext> implements RestlightHandler<CTX> {

    private final RestlightHandler<CTX> delegate;
    private final ExceptionHandlerChain<CTX> exceptionHandler;
    private final FilterChain<FCTX> filterChain;
    private final FilterContextFactory<CTX, FCTX> filterContext;

    @SuppressWarnings("unchecked")
    public FilteredHandler(RestlightHandler<CTX> delegate,
                           List<InternalFilter<FCTX>> filters,
                           FilterContextFactory<CTX, FCTX> filterContext,
                           ExceptionHandlerChain<CTX> exceptionHandler) {
        Checks.checkNotNull(delegate, "delegate");
        Checks.checkNotNull(filters, "filters");
        Checks.checkNotNull(filterContext, "filterContext");
        Checks.checkNotNull(exceptionHandler, "exceptionHandler");
        this.delegate = delegate;
        this.filterContext = filterContext;
        this.exceptionHandler = exceptionHandler;
        this.filterChain = LinkedFilterChain.immutable(filters, (context -> {
            if (!context.response().isCommitted()) {
                return delegate.process((CTX) context);
            } else {
                return Futures.completedFuture();
            }
        }));
    }

    @Override
    public void onStart() {
        delegate.onStart();
    }

    @Override
    public List<Scheduler> schedulers() {
        return delegate.schedulers();
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        filterChain.doFilter(filterContext.create(context))
                .whenComplete((v, th) -> {
                    if (th != null) {
                        handleException(exceptionHandler, context, th, promise);
                    } else {
                        promise.complete(v);
                    }
                });
        return promise;
    }

    @Override
    public void onConnected(Channel channel) {
        delegate.onConnected(channel);
    }

    @Override
    public void onDisconnected(Channel channel) {
        delegate.onDisconnected(channel);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }
}
