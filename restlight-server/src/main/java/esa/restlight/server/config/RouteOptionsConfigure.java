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
package esa.restlight.server.config;

public final class RouteOptionsConfigure {
    private boolean useCachedRouting = true;
    private int cacheRatio = 10;
    private int computeRate = 1;

    private RouteOptionsConfigure() {
    }

    public static RouteOptionsConfigure newOpts() {
        return new RouteOptionsConfigure();
    }

    public static RouteOptions defaultOpts() {
        return newOpts().configured();
    }

    public RouteOptionsConfigure useCachedRouting(boolean useCachedRouting) {
        this.useCachedRouting = useCachedRouting;
        return this;
    }

    @Deprecated
    public RouteOptionsConfigure cacheRatio(int cacheRatio) {
        this.cacheRatio = cacheRatio;
        return this;
    }

    public RouteOptionsConfigure computeRate(int computeRate) {
        this.computeRate = computeRate;
        return this;
    }

    public RouteOptions configured() {
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setUseCachedRouting(useCachedRouting);
        routeOptions.setCacheRatio(cacheRatio);
        routeOptions.setComputeRate(computeRate);
        return routeOptions;
    }
}
