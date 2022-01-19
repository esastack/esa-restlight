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
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.route.Router;

import java.util.List;

public class AbstractRouteRegistry implements RouteRegistry, Router {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRouteRegistry.class);

    private final AbstractRouter<? extends RouteWrap> router;

    AbstractRouteRegistry(AbstractRouter<? extends RouteWrap> router) {
        Checks.checkNotNull(router, "router");
        this.router = router;
    }

    @Override
    public void register(Route route) {
        if (route == null) {
            return;
        }
        RouteWrap routeWrap = new RouteWrap(route);
        router.mappingLookups.forEach(registered -> {
            if (registered.predicate.mayAmbiguousWith(routeWrap.predicate)) {
                logger.warn("Found ambiguous route:\n{}\n{}", routeWrap.route, registered);
            }
        });
        router.add(routeWrap);
        logger.debug("Registering route: {}", route);
    }

    @Override
    public void deRegister(Route route) {
        if (route == null) {
            return;
        }
        RouteWrap routeWrap = new RouteWrap(route);
        RouteWrap target = null;
        for (RouteWrap item : router.mappingLookups) {
            if (isEquals(item, routeWrap)) {
                target = item;
                break;
            }
        }

        if (target != null) {
            logger.debug("deRegistering route: {}", route);
            router.remove(target);
        }
    }

    @Override
    public Route route(RequestContext context) {
        return router.route(context);
    }

    @Override
    public List<Route> routes() {
        return router.routes();
    }

    static boolean isEquals(RouteWrap a, RouteWrap b) {
        return a.mapping().equals(b.mapping());
    }
}
