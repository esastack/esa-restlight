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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.route.Mapping;

import java.util.Optional;

/**
 * Interface defines a couple elements of a handler.
 */
public interface HandlerMapping {

    /**
     * Gets the {@link Mapping}.
     *
     * @return mapping
     */
    Mapping mapping();

    /**
     * Gets the {@link RouteHandler}.
     *
     * @return route handler
     */
    RouteMethodInfo methodInfo();

    /**
     * Obtains an optional {@code object}.
     *
     * @return optional object, which must not be {@code null}.
     */
    Optional<Object> bean();

    /**
     * Obtains an optional {@link HandlerMapping}.
     *
     * @return optional mapping, which must not be {@code null}.
     */
    Optional<HandlerMapping> parent();

}
