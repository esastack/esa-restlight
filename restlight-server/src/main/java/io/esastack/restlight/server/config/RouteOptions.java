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
package io.esastack.restlight.server.config;

import io.esastack.restlight.server.route.impl.CachedRouteRegistry;

import java.io.Serializable;

public class RouteOptions implements Serializable {

    private static final long serialVersionUID = 2544181710481210323L;

    private boolean useCachedRouting = true;

    /**
     * cache ratio in {@link CachedRouteRegistry}
     * @deprecated unused
     */
    @Deprecated
    private int cacheRatio = 10;

    /**
     * compute rate ration in {@link CachedRouteRegistry}
     */
    private int computeRate = 1;

    public boolean isUseCachedRouting() {
        return useCachedRouting;
    }

    public void setUseCachedRouting(boolean useCachedRouting) {
        this.useCachedRouting = useCachedRouting;
    }

    @Deprecated
    public int getCacheRatio() {
        return cacheRatio;
    }

    @Deprecated
    public void setCacheRatio(int cacheRatio) {
        this.cacheRatio = cacheRatio;
    }

    public int getComputeRate() {
        return computeRate;
    }

    public void setComputeRate(int computeRate) {
        this.computeRate = computeRate;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RouteOptions{");
        sb.append("useCachedRouting=").append(useCachedRouting);
        sb.append(", cacheRatio=").append(cacheRatio);
        sb.append(", computeRate=").append(computeRate);
        sb.append('}');
        return sb.toString();
    }
}
