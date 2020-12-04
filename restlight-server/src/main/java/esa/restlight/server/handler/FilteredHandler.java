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
package esa.restlight.server.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.util.Futures;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FilteredHandler implements RestlightHandler {

    private final RestlightHandler delegate;
    private final Filter[] filters;
    private final FilterChain filterChain;

    public FilteredHandler(RestlightHandler delegate, List<Filter> filters) {
        Objects.requireNonNull(delegate, "Handler must not be null!");
        Objects.requireNonNull(filters, "Filter must not be null!");
        this.delegate = delegate;
        this.filters = filters.toArray(new Filter[0]);
        this.filterChain = LinkedFilterChain.immutable(this.filters, ((request, response) -> {
            if (!response.isCommitted()) {
                return delegate.process(request, response);
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
    public Executor executor() {
        return delegate.executor();
    }

    @Override
    public List<Scheduler> schedulers() {
        return delegate.schedulers();
    }

    @Override
    public CompletableFuture<Void> process(AsyncRequest request, AsyncResponse response) {
        return filterChain.doFilter(request, response);
    }

    @Override
    public void onConnected(ChannelHandlerContext ctx) {
        for (Filter filter : filters) {
            if (!filter.onConnected(ctx)) {
                return;
            }
        }
        delegate.onConnected(ctx);
    }

    @Override
    public void shutdown() {
        try {
            for (Filter filter : filters) {
                filter.shutdown();
            }
        } finally {
            delegate.shutdown();
        }
    }
}
