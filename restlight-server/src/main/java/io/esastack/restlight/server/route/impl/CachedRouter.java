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
package io.esastack.restlight.server.route.impl;

import esa.commons.concurrent.UnsafeUtils;
import io.esastack.restlight.server.context.RequestContext;

class CachedRouter extends AbstractRouter<CountedRoute> {

    private final CachedRoutes routes;

    CachedRouter(int computeRate) {
        if (UnsafeUtils.hasUnsafe()) {
            this.routes = new UnsafeCachedRoutes(computeRate);
        } else {
            this.routes = new DefaultCachedRoutes(computeRate);
        }
    }

    @Override
    CountedRoute converted(RouteWrap route) {
        return new CountedRoute(route);
    }

    @Override
    CountedRoute[] instantiate1() {
        return new CountedRoute[1];
    }

    @Override
    CountedRoute matchAll(RequestContext context) {
        return findFor(routes.lookup(), context);
    }

    @Override
    CountedRoute findFor(CountedRoute[] routes, RequestContext context) {
        CountedRoute found = super.findFor(routes, context);
        if (found != null) {
            this.routes.hit(found);
        }
        return found;
    }

    @Override
    void addConverted(CountedRoute countedRoute) {
        super.addConverted(countedRoute);
        this.routes.add(countedRoute);
    }

    @Override
    void removeConverted(CountedRoute r) {
        super.removeConverted(r);
        this.routes.remove(r);
    }
}

