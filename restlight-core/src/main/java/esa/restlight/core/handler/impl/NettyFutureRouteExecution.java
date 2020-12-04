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
package esa.restlight.core.handler.impl;

import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.util.FutureUtils;
import io.netty.util.concurrent.Future;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link esa.restlight.server.route.RouteExecution} that always cast the {@code return value} to
 * {@link Future} and then convert the casted {@link Future} to {@link CompletableFuture}.
 */
public class NettyFutureRouteExecution extends DefaultRouteExecution {

    public NettyFutureRouteExecution(RouteHandlerAdapter routeHandler,
                                     List<InternalInterceptor> interceptors) {
        super(routeHandler, interceptors);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CompletableFuture<Object> transferToFuture(Object returnValue) {
        return FutureUtils.transferNettyFuture((Future<Object>) returnValue);
    }
}
