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
package io.esastack.restlight.core.handler;

import esa.commons.Checks;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.handler.LinkedFilterChain;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class LinkedRouteFilterChain implements RouteFilterChain {

    private final RouteFilter current;
    private final RouteFilterChain next;

    private LinkedRouteFilterChain(RouteFilter current, RouteFilterChain next) {
        Checks.checkNotNull(current, "current");
        Checks.checkNotNull(next, "next");
        this.current = current;
        this.next = next;
    }

    /**
     * Return a immutable filter chain, all the instances of {@link LinkedFilterChain} will be instantiated ahead.
     *
     * @param filters filters
     * @param action  action
     * @return filter chain
     */
    public static LinkedRouteFilterChain immutable(RouteFilter[] filters,
                                                   Function<RouteContext, CompletionStage<Void>> action) {
        Checks.checkNotEmptyArg(filters, "filters must not be empty");
        // link all the filter and the given action(last)
        RouteFilterChain next = (mp, ctx) -> action.apply(ctx);
        LinkedRouteFilterChain chain;
        int i = filters.length - 1;
        do {
            chain = new LinkedRouteFilterChain(filters[i], next);
            next = chain;
        } while (--i >= 0);
        return chain;
    }

    @Override
    public CompletionStage<Void> doNext(HandlerMapping mapping, RouteContext context) {
        return current.routed(mapping, context, next);
    }

}

