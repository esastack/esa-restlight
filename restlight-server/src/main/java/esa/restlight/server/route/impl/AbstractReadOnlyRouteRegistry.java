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
import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import esa.restlight.server.util.PathMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractReadOnlyRouteRegistry<R extends RouteWrap,
        RS extends Routes<R>> implements ReadOnlyRouteRegistry {

    /**
     * Mapping for the url -> handler method mapping
     */
    private final Map<String, R[]> urlLookup;
    private final List<Route> immutable;
    final RS routes;

    AbstractReadOnlyRouteRegistry(List<RouteWrap> mappingLookup) {
        Checks.checkNotNull(mappingLookup);
        MultiValueMap<String, R> forUrl = new LinkedMultiValueMap<>();
        R[] routes = mappingLookup.stream()
                .map(route -> {
                    R r = route(route);
                    final List<String> directUrls = getDirectUrls(route.mapping());
                    for (String url : directUrls) {
                        forUrl.add(url, r);
                    }
                    return r;
                })
                .collect(Collectors.toList())
                .toArray(toArray().apply(0));
        this.routes = toRoutes(routes);
        this.urlLookup = new HashMap<>(forUrl.size());
        forUrl.forEach((k, v) -> urlLookup.put(k, v.toArray(toArray().apply(0))));
        this.immutable = Collections.unmodifiableList(mappingLookup);
    }

    @Override
    public Route route(AsyncRequest request) {
        // find from url lookup
        R route = matchByUri(request);
        if (route == null) {
            route = matchAll(request);
        }
        if (route == null) {
            return null;
        }
        return route.route;
    }

    R matchByUri(AsyncRequest request) {
        // find from url lookup
        final R[] routes = urlLookup.get(request.path());
        if (routes == null || routes.length == 0) {
            return null;
        }
        return findFor(routes, request);
    }

    R matchAll(AsyncRequest request) {
        return findFor(routes.lookup(), request);
    }

    /**
     * route the RouteHandler for current request from given HandlerMethodMappings
     *
     * @param routes  immutable
     * @param request request
     * @return RouteHandler found, {@code null} if not found
     */
    R findFor(R[] routes, AsyncRequest request) {
        for (R route : routes) {
            if (route.test(request)) {
                return route;
            }
        }
        return null;
    }

    @Override
    public List<Route> routes() {
        return immutable;
    }

    abstract RS toRoutes(R[] routes);

    abstract Function<Integer, R[]> toArray();

    abstract R route(RouteWrap routeWrap);

    private static List<String> getDirectUrls(Mapping mapping) {
        List<String> urls = new ArrayList<>(1);
        for (String path : mapping.path()) {
            if (!PathMatcher.isPattern(path)) {
                urls.add(path);
            }
        }
        return urls;
    }
}
