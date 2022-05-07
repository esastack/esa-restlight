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
package io.esastack.restlight.core.route.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.RouteRegistry;
import io.esastack.restlight.core.route.Router;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;

import java.util.List;

public class RoutableRegistry implements RouteRegistry, Router {

    private final DeployContext context;
    private final AbstractRouteRegistry underlying;

    public RoutableRegistry(DeployContext context, AbstractRouteRegistry underlying) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(underlying, "underlying");
        this.context = context;
        this.underlying = underlying;
    }

    @Override
    public List<Route> routes() {
        return underlying.routes();
    }

    @Override
    public void register(Route route) {
        if (route.scheduler() == null) {
            String defaultScheduler = context.options().getScheduling().getDefaultScheduler();
            if (StringUtils.isNotEmpty(defaultScheduler)) {
                Scheduler scheduler = context.schedulers().get(defaultScheduler);
                if (scheduler == null) {
                    throw new IllegalStateException("Could not find any scheduler named '"
                            + defaultScheduler + "'");
                }
                route = Route.route(route)
                        .scheduler(scheduler);
            } else {
                route = Route.route(route)
                        .scheduler(context.schedulers().get(Schedulers.BIZ));
            }
        } else if (Schedulers.isBiz(route.scheduler())) {
            route = Route.route(route)
                    .scheduler(context.schedulers().get(Schedulers.BIZ));
        }
        underlying.register(route);
    }

    @Override
    public void deRegister(Route route) {
        underlying.deRegister(route);
    }

    @Override
    public Route route(RequestContext context) {
        return underlying.route(context);
    }
}

