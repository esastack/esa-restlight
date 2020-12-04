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
package esa.restlight.server.route.impl;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.route.predicate.RequestPredicate;
import esa.restlight.server.route.predicate.RoutePredicate;
import esa.restlight.server.schedule.Scheduler;

import java.util.Optional;

class RouteWrap implements Route, RequestPredicate {
    final Route route;
    final RequestPredicate predicate;

    RouteWrap(Route route) {
        Checks.checkNotNull(route);
        this.route = route;
        this.predicate = RoutePredicate.parseFrom(route.mapping());
    }

    @Override
    public Mapping mapping() {
        return route.mapping();
    }

    @Override
    public RouteExecution toExecution(AsyncRequest request) {
        return route.toExecution(request);
    }

    @Override
    public Optional<Object> handler() {
        return route.handler();
    }

    @Override
    public boolean test(AsyncRequest request) {
        return predicate.test(request);
    }

    @Override
    public Scheduler scheduler() {
        return route.scheduler();
    }

    @Override
    public String toString() {
        return route.toString();
    }
}
