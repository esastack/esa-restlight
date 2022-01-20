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

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.Router;
import io.esastack.restlight.server.util.PathMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class AbstractRouter<R extends RouteWrap> implements Router {

    /**
     * all mappings
     */
    final List<R> mappingLookups = new CopyOnWriteArrayList<>();

    /**
     * maps which can from directly url to mapping
     */
    private final Map<String, R[]> urlLookups = new ConcurrentHashMap<>();

    @Override
    public Route route(RequestContext context) {
        // find from url lookup
        R route = matchByUri(context);
        if (route == null) {
            route = matchAll(context);
        }
        if (route == null) {
            return null;
        }
        return route.route;
    }

    List<Route> routes() {
        List<Route> routes = new LinkedList<>(mappingLookups);
        return Collections.unmodifiableList(routes);
    }

    void add(RouteWrap route) {
        addConverted(converted(route));
    }

    void remove(RouteWrap route) {
        removeConverted(converted(route));
    }

    R matchByUri(RequestContext context) {
        // find from url lookup
        final R[] routes = urlLookups.get(context.request().path());
        if (routes == null || routes.length == 0) {
            return null;
        }
        return findFor(routes, context);
    }

    void addConverted(R r) {
        Mapping mapping = r.route.mapping();
        getDirectUrls(mapping).forEach(direct -> urlLookups.compute(direct, (s, v) -> {
            R[] values;
            if (v == null) {
                values = instantiate1();
            } else {
                values = Arrays.copyOf(v, v.length + 1);
            }
            values[values.length - 1] = r;
            return values;
        }));

        mappingLookups.add(r);
    }

    void removeConverted(R r) {
        Mapping mapping = r.route.mapping();
        Map<String, RouteWrap> directs = new HashMap<>();
        getDirectUrls(mapping).forEach(direct -> directs.put(direct, r));
        directs.keySet().forEach(urlLookups::remove);

        mappingLookups.remove(r);
    }

    /**
     * Converts the given {@code routes} to generalized rotes.
     *
     * @param route routes
     * @return generic routes
     */
    abstract R converted(RouteWrap route);

    /**
     * Instantiates a generalized rotes which size is 1.
     *
     * @return instantiated generic routes
     */
    abstract R[] instantiate1();

    /**
     * Finds the generalized rotes which can be used to handle the given {@link HttpRequest}.
     *
     * @param context context
     * @return generic routes
     */
    abstract R matchAll(RequestContext context);

    /**
     * route the RouteHandler for current request from given HandlerMethodMappings
     *
     * @param routes  immutable
     * @param context context
     * @return RouteHandler found, {@code null} if not found
     */
    R findFor(R[] routes, RequestContext context) {
        for (R route : routes) {
            if (route.test(context)) {
                return route;
            }
        }
        return null;
    }

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

