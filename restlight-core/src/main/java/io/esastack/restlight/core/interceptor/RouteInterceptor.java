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
package io.esastack.restlight.core.interceptor;

import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.impl.RouteHandlerMethodAdapter;
import io.esastack.restlight.server.BaseDeployments;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.Routing;

/**
 * We will determine whether to attach the {@link Interceptor} to the target {@link Route}
 * before starting the restlight server by the return value of {@link #match(DeployContext, Routing)} while every
 * instances of {@link Route} maintains its own interceptors which have possibility to match
 * to it when a {@link HttpRequest} is coming.
 * <p>
 * whether a {@link HttpRequest} should be matched to this interceptor will just depends on the
 * return value of {@link #match(DeployContext, Routing)}.
 * @see InternalInterceptor
 * @see InterceptorFactory#of(RouteInterceptor)
 */
@SPI
public interface RouteInterceptor extends InternalInterceptor {

    /**
     * Gets the affinity value between current interceptor and the given {@link Route}.
     *
     * @param ctx   context
     * @param route route to match, the {@link Route} that added by
     * {@link BaseDeployments#addRoute(Route)}
     *              will not be passed to this method, and {@link Route} that associates to a {@link
     *              RouteHandlerMethodAdapter} will be passed to this method instead.
     *
     * @return affinity value.
     */
    boolean match(DeployContext<? extends RestlightOptions> ctx, Routing route);
}
