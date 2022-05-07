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

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.Routing;

import java.util.Optional;

/**
 * Factory of {@link Interceptor}.
 */
@SPI
public interface InterceptorFactory {

    /**
     * Creates a implementation of {@link InterceptorFactory} by the given {@link Interceptor}
     *
     * @param interceptor interceptor
     * @return factory
     */
    static InterceptorFactory of(Interceptor interceptor) {
        return new Singleton(interceptor);
    }

    /**
     * Creates a implementation of {@link InterceptorFactory} which will transfer the given {@link InternalInterceptor}
     * to a instance of {@link InternalInterceptorWrap} which will always using the given {@link InternalInterceptor} as
     * the delegate.
     *
     * @param interceptor interceptor
     * @return factory
     */
    static InterceptorFactory of(InternalInterceptor interceptor) {
        return new Singleton(new InternalInterceptorWrap(interceptor));
    }

    /**
     * Creates a implementation of {@link InterceptorFactory} which will transfer the given {@link HandlerInterceptor}
     * to a instance of {@link HandlerInterceptorWrap} which will always using the given {@link HandlerInterceptor} as
     * the delegate.
     *
     * @param interceptor interceptor
     * @return factory
     */
    static InterceptorFactory of(HandlerInterceptor interceptor) {
        return new HandlerFactory(interceptor);
    }

    /**
     * Creates a implementation of {@link InterceptorFactory} which will transfer the given {@link MappingInterceptor}
     * to a instance of {@link MappingInterceptorWrap} which will always using the given {@link MappingInterceptor} as
     * the delegate.
     *
     * @param interceptor interceptor
     * @return factory
     */
    static InterceptorFactory of(MappingInterceptor interceptor) {
        return new Singleton(new MappingInterceptorWrap(interceptor));
    }

    /**
     * Creates a implementation of {@link InterceptorFactory} which will transfer the given {@link RouteInterceptor} to
     * a instance of {@link RouteInterceptorWrap} which will always using the given {@link RouteInterceptor} as the
     * delegate.
     *
     * @param interceptor interceptor
     * @return factory
     */
    static InterceptorFactory of(RouteInterceptor interceptor) {
        return new RouteFactory(interceptor);
    }

    /**
     * Create an optional instance of {@link Interceptor} for given target {@link Route} before starting.
     *
     * @param ctx   deploy context
     * @param route target route.
     * @return interceptor associated with given {@link Route} or else an empty optional result.
     */
    Optional<Interceptor> create(DeployContext ctx, Routing route);

    class Singleton implements InterceptorFactory {

        private final Interceptor interceptor;

        private Singleton(Interceptor interceptor) {
            Checks.checkNotNull(interceptor, "interceptor");
            this.interceptor = interceptor;
        }

        @Override
        public Optional<Interceptor> create(DeployContext ctx, Routing route) {
            return Optional.of(interceptor);
        }
    }

    class RouteFactory implements InterceptorFactory {

        private final RouteInterceptor interceptor;

        private RouteFactory(RouteInterceptor interceptor) {
            Checks.checkNotNull(interceptor, "interceptor");
            this.interceptor = interceptor;
        }

        @Override
        public Optional<Interceptor> create(DeployContext ctx, Routing route) {
            return Optional.of(new RouteInterceptorWrap(interceptor, ctx, route));
        }

    }

    class HandlerFactory implements InterceptorFactory {

        private final HandlerInterceptor interceptor;

        private HandlerFactory(HandlerInterceptor interceptor) {
            Checks.checkNotNull(interceptor, "interceptor");
            this.interceptor = interceptor;
        }

        @Override
        public Optional<Interceptor> create(DeployContext ctx, Routing route) {
            return Optional.of(new HandlerInterceptorWrap(interceptor, ctx, route));
        }

    }

}
