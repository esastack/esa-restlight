/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.server.spi;

import esa.commons.spi.SPI;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteRegistry;

/**
 * A marker interface which provides a way to access the {@link RouteRegistry} after it's has been instantiated
 * and all the internal {@link Route}s have been registered to it. You can register or deregister custom {@link Route}s
 * whenever you like even while the server is running by implementing current interface.
 */
@SPI
@FunctionalInterface
public interface RouteRegistryAware {

    /**
     * This callback method is invoked when all the internal routes have been registered to {@code registry}.
     *
     * @param registry registry
     */
    void setRegistry(RouteRegistry registry);

}

