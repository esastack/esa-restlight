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
package io.esastack.restlight.jaxrs.configure;

import esa.commons.collection.AttributeKey;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.context.RouteContext;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterChain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RouteTracking implements RouteFilter {

    private static final AttributeKey<List<HandlerMapping>> ROUTE_TRACKING_KEY = AttributeKey
            .valueOf("$jakarta.route.tracking");
    private static final AttributeKey<Boolean> HANDLER_METHOD_MATCHED = AttributeKey
            .valueOf("$jakarta.handler.matched");

    private static final RouteTracking SINGLETON = new RouteTracking();

    private RouteTracking() {
    }

    public static RouteTracking singleton() {
        return SINGLETON;
    }

    @Override
    public CompletableFuture<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
        if (!mapping.methodInfo().isLocator()) {
            context.attr(HANDLER_METHOD_MATCHED).set(true);
        }
        List<HandlerMapping> mappings = context.attr(ROUTE_TRACKING_KEY).get();
        if (mappings == null) {
            mappings = new LinkedList<>();
            context.attr(ROUTE_TRACKING_KEY).set(mappings);
        }
        mappings.add(mapping);
        return next.doNext(mapping, context);
    }

    public static List<HandlerMapping> tracking(RequestContext context) {
        List<HandlerMapping> mappings = context.attr(ROUTE_TRACKING_KEY).get();
        if (mappings == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(mappings);
        }
    }

    public static boolean isMethodMatched(RequestContext context) {
        return context.attr(HANDLER_METHOD_MATCHED).get();
    }

}

