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
package io.esastack.restlight.server.route.impl;

import esa.commons.Checks;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.ExecutionFactory;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.predicate.RequestPredicate;
import io.esastack.restlight.server.route.predicate.RoutePredicate;
import io.esastack.restlight.server.schedule.Scheduler;

class RouteWrap implements Route, RequestPredicate {

    final Route route;
    final RequestPredicate predicate;
    private String strVal;

    RouteWrap(Route route) {
        Checks.checkNotNull(route, "route");
        this.route = route;
        this.predicate = RoutePredicate.parseFrom(route.mapping());
    }

    @Override
    public ExecutionFactory executionFactory() {
        return route.executionFactory();
    }

    @Override
    public boolean test(RequestContext context) {
        return predicate.test(context);
    }

    @Override
    public Scheduler scheduler() {
        return route.scheduler();
    }

    @Override
    public Mapping mapping() {
        return route.mapping();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Route) {
            return mapping().equals(((Route) other).mapping());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (strVal == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Route(mapping(").append(mapping()).append(")");
            if (scheduler() != null) {
                sb.append(",scheduler(").append(scheduler().name()).append(')');
            }
            handler().ifPresent(h -> sb.append(",handler(").append(handler()).append(")"));
            sb.append(")");
            strVal = sb.toString();
        }
        return strVal;
    }
}
