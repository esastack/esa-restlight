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
package io.esastack.restlight.core.handler.locate;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.RouteMethodInfo;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

/**
 * Shows multiple {@link RouteMethodLocator}s as a single {@link RouteMethodLocator}.
 */
public class CompositeRouteMethodLocator implements RouteMethodLocator {

    private final Collection<? extends RouteMethodLocator> locators;

    private CompositeRouteMethodLocator(Collection<? extends RouteMethodLocator> locators) {
        Checks.checkNotEmptyArg(locators, "locators");
        this.locators = locators;
    }

    public static RouteMethodLocator wrapIfNecessary(Collection<? extends RouteMethodLocator> locators) {
        if (locators.isEmpty()) {
            return null;
        }
        if (locators.size() == 1) {
            return locators.iterator().next();
        } else {
            return new CompositeRouteMethodLocator(locators);
        }
    }

    @Override
    public Optional<RouteMethodInfo> getRouteHandlerInfo(Class<?> userType, Method method) {
        Optional<RouteMethodInfo> routeMethod = Optional.empty();
        for (RouteMethodLocator locator : locators) {
            routeMethod = locator.getRouteHandlerInfo(userType, method);
            if (routeMethod.isPresent()) {
                break;
            }
        }
        return routeMethod;
    }
}
