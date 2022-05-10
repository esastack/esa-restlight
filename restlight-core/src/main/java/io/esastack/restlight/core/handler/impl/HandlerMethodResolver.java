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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.handler.method.HandlerMethodInfo;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.util.Futures;

import java.util.concurrent.CompletionStage;

public class HandlerMethodResolver implements HandlerValueResolver {

    private final HandlerMethodInfo methodInfo;

    public HandlerMethodResolver(HandlerMethodInfo methodInfo) {
        Checks.checkNotNull(methodInfo, "methodInfo");
        this.methodInfo = methodInfo;
    }

    @Override
    public CompletionStage<Void> handle(Object value, RequestContext context) {
        final HttpResponse response = context.response();
        HttpStatus status = methodInfo.customStatus();
        if (status != null) {
            response.status(status.code());
        }
        context.response().entity(value);
        return Futures.completedFuture();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

