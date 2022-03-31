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
package io.esastack.restlight.core.spi.impl;

import esa.commons.annotation.Beta;
import esa.commons.annotation.Internal;
import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterChain;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.RouteContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Internal
@Beta
public class RouteTracking implements RouteFilter {

    private static final AttributeKey<List<HandlerMapping>> ROUTE_TRACKING_KEY = AttributeKey
            .valueOf("internal.route.tracking");

    private static final AttributeKey<HandlerMethod> HANDLED_METHOD =
            AttributeKey.valueOf("$internal.handled.method");

    @Override
    public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
        if (!mapping.methodInfo().isLocator()) {
            context.attrs().attr(HANDLED_METHOD).set(mapping.methodInfo().handlerMethod());
        }
        List<HandlerMapping> mappings = context.attrs().attr(ROUTE_TRACKING_KEY).get();
        if (mappings == null) {
            mappings = new LinkedList<>();
            context.attrs().attr(ROUTE_TRACKING_KEY).set(mappings);
        }
        mappings.add(mapping);
        return next.doNext(mapping, context);
    }

    public int getOrder() {
        return Ordered.MIDDLE_PRECEDENCE;
    }

    public static List<HandlerMapping> tracking(RequestContext context) {
        List<HandlerMapping> mappings = context.attrs().attr(ROUTE_TRACKING_KEY).get();
        if (mappings == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(mappings);
        }
    }

    public static HandlerMethod matchedMethod(RequestContext context) {
        return context.attrs().attr(HANDLED_METHOD).get();
    }

}

