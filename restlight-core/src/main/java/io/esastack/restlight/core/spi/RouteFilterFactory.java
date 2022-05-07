/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.spi;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.HandlerPredicate;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.filter.RouteFilterAdapter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.route.Route;

import java.util.Optional;

@SPI
public interface RouteFilterFactory extends HandlerPredicate {

    /**
     * Converts given {@link RouteFilterAdapter} to {@link RouteFilterFactory} which
     * always use the given {@link RouteFilterAdapter} as the result of
     * {@link #create(HandlerMethod)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static RouteFilterFactory singleton(RouteFilterAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link RouteFilter} for given {@link Route}.
     *
     * @param method handler method
     * @return an optional instance of filter which is corresponding with given {@code route}.
     */
    Optional<RouteFilter> create(HandlerMethod method);

    class Singleton implements RouteFilterFactory {

        private final RouteFilterAdapter filter;

        Singleton(RouteFilterAdapter filter) {
            Checks.checkNotNull(filter, "filter");
            this.filter = filter;
        }

        @Override
        public boolean supports(HandlerMethod method) {
            return filter.supports(method);
        }

        @Override
        public Optional<RouteFilter> create(HandlerMethod method) {
            return Optional.of(filter);
        }
    }
}

