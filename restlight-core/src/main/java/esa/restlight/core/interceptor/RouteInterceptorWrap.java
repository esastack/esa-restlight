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
package esa.restlight.core.interceptor;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Affinity;
import esa.restlight.server.route.Route;

class RouteInterceptorWrap extends AbstractInterceptorWrap<RouteInterceptor> {

    private final int affinity;

    RouteInterceptorWrap(RouteInterceptor interceptor,
                         DeployContext<? extends RestlightOptions> ctx,
                         Route route) {
        super(interceptor);
        this.affinity = interceptor.match(ctx, route) ? Affinity.ATTACHED : Affinity.DETACHED;
    }

    @Override
    public InterceptorPredicate predicate() {
        return InterceptorPredicate.ALWAYS;
    }

    @Override
    public int affinity() {
        return affinity;
    }
}
