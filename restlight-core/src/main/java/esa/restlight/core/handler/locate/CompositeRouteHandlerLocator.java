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
package esa.restlight.core.handler.locate;

import esa.commons.Checks;
import esa.restlight.core.handler.RouteHandler;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

/**
 * Shows multiple {@link RouteHandlerLocator}s as a single {@link RouteHandlerLocator}.
 */
public class CompositeRouteHandlerLocator implements RouteHandlerLocator {

    private final Collection<? extends RouteHandlerLocator> locators;

    private CompositeRouteHandlerLocator(Collection<? extends RouteHandlerLocator> locators) {
        Checks.checkNotEmptyArg(locators, "locators");
        this.locators = locators;
    }

    public static RouteHandlerLocator wrapIfNecessary(Collection<? extends RouteHandlerLocator> locators) {
        if (locators.isEmpty()) {
            return null;
        }
        if (locators.size() == 1) {
            return locators.iterator().next();
        } else {
            return new CompositeRouteHandlerLocator(locators);
        }
    }

    @Override
    public Optional<RouteHandler> getRouteHandler(Class<?> userType, Method method, Object bean) {
        Optional<RouteHandler> routeHandler = Optional.empty();
        for (RouteHandlerLocator locator : locators) {
            routeHandler = locator.getRouteHandler(userType, method, bean);
            if (routeHandler.isPresent()) {
                break;
            }
        }
        return routeHandler;
    }
}
