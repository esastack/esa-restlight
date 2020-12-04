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

import com.google.common.util.concurrent.ListenableFuture;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.util.FutureUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link esa.restlight.server.route.RouteExecution} that always cast the {@code return value} to
 * {@link ListenableFuture} and then convert the casted {@link ListenableFuture} to {@link CompletableFuture}.
 */
public class ListenableFutureRouteExecution extends DefaultRouteExecution {

    public ListenableFutureRouteExecution(RouteHandlerAdapter routeHandler,
                                          List<InternalInterceptor> interceptors) {
        super(routeHandler, interceptors);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CompletableFuture<Object> transferToFuture(Object returnValue) {
        return FutureUtils.transferListenableFuture((ListenableFuture<Object>) returnValue);
    }
}
