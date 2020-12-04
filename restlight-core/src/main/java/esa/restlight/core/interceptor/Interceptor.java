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

import esa.commons.spi.SPI;
import esa.restlight.core.util.Affinity;

/**
 * We will determine whether to attach the {@link Interceptor} to the target {@link esa.restlight.server.route.Route}
 * before starting the restlight server by the return value of {@link #affinity()} while every instances of {@link
 * esa.restlight.server.route.Route} maintains its own interceptors which have possibility to match to it when a {@link
 * esa.httpserver.core.AsyncRequest} is coming.
 * <p>
 * And we will also determine that whether a {@link esa.httpserver.core.AsyncRequest} should be matched to this
 * interceptor by the return value of {@link #predicate()}'s {@link InterceptorPredicate#test(Object)} when a {@link
 * esa.httpserver.core.AsyncRequest} is coming.
 * @see Affinity
 * @see InternalInterceptor
 */
@SPI
public interface Interceptor extends InternalInterceptor, Affinity {

    /**
     * Gets the predicate of current interceptor. determines whether current interceptor should be matched to a {@link
     * esa.httpserver.core.AsyncRequest}.
     *
     * @return predicate, or {@code null} if {@link #affinity()} return's a negative value.
     */
    InterceptorPredicate predicate();

    /**
     * Default to highest affinity.
     * <p>
     * Whether a {@link Interceptor} should be matched to a {@link esa.restlight.server.route.Route} is depends on it.
     *
     * @return affinity
     */
    @Override
    default int affinity() {
        return HIGHEST;
    }
}
