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
package esa.restlight.core.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.locate.RouteHandlerLocator;

/**
 * Produces an instance of {@link RouteHandlerLocator} for locating {@link esa.restlight.core.handler.RouteHandler}s.
 */
@SPI
@Internal
public interface RouteHandlerLocatorFactory {

    /**
     * Create an instance of {@link RouteHandlerLocator}.
     *
     * @param ctx deploy context
     *
     * @return locator
     */
    RouteHandlerLocator locator(DeployContext<? extends RestlightOptions> ctx);

}
