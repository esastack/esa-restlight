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
package esa.restlight.server.spi;

import esa.commons.spi.SPI;
import esa.restlight.server.ServerDeployContext;
import esa.restlight.server.bootstrap.DispatcherExceptionHandler;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.route.ExceptionHandler;

/**
 * DispatcherExceptionHandlerFactory is used to produce an instance of {@link DispatcherExceptionHandler}
 * which handles exceptions which can't be handled by custom {@link ExceptionHandler}s.
 *
 * @see DispatcherExceptionHandler
 */
@SPI
public interface DispatcherExceptionHandlerFactory {

    /**
     * Produces an instance of {@link DispatcherExceptionHandler}.
     *
     * @param ctx deploy context
     *
     * @return handler
     */
    DispatcherExceptionHandler exceptionHandler(ServerDeployContext<? extends ServerOptions> ctx);

}

