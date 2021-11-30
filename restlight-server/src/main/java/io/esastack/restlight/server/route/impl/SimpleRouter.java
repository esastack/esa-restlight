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

import io.esastack.httpserver.core.HttpRequest;

class SimpleRouter extends AbstractRouter<RouteWrap> {

    @Override
    RouteWrap matchAll(HttpRequest request) {
        return findFromMappings(request);
    }

    @Override
    RouteWrap converted(RouteWrap route) {
        return route;
    }

    @Override
    RouteWrap[] instantiate1() {
        return new RouteWrap[1];
    }

    /**
     * route the RouteHandler for current request from given HandlerMethodMappings
     *
     * @param request request
     * @return RouteHandler found, {@code null} if not found
     */
    private RouteWrap findFromMappings(HttpRequest request) {
        for (RouteWrap route : mappingLookups) {
            if (route.test(request)) {
                return route;
            }
        }
        return null;
    }
}

