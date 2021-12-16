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
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.impl.FilterContextImpl;
import io.esastack.restlight.server.core.impl.FilteringRequestImpl;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.spi.Filter;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.esastack.restlight.server.schedule.ScheduledRestlightHandler.handleException;

public class FilteredHandler implements RestlightHandler {

    private final RestlightHandler delegate;
    private final ExceptionHandlerChain exceptionHandler;
    private final FilterChain filterChain;

    public FilteredHandler(RestlightHandler delegate,
                           List<Filter> filters,
                           ExceptionHandlerChain exceptionHandler) {
        Checks.checkNotNull(delegate, "delegate");
        Checks.checkNotNull(filters, "filters");
        Checks.checkNotNull(exceptionHandler, "exceptionHandler");
        this.delegate = delegate;
        this.exceptionHandler = exceptionHandler;
        this.filterChain = LinkedFilterChain.immutable(filters, (delegate::process));
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
    public CompletableFuture<Void> process(RequestContext context) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        filterChain.doFilter(new FilterContextImpl(context, new FilteringRequestImpl(context.request()),
                context.response()))
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
