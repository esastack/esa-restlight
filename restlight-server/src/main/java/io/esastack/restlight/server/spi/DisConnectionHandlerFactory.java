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
import io.esastack.restlight.server.ServerDeployContext;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.handler.DisConnectionHandler;

import java.util.Optional;

/**
 * The factory to create an {@link DisConnectionHandler}.
 */
@SPI
@FunctionalInterface
public interface DisConnectionHandlerFactory {

    /**
     * Creates an optional instance of {@link DisConnectionHandler}.
     *
     * @param ctx   ctx
     * @return      an optional {@link DisConnectionHandler}, which must not be {@code null}.
     */
    Optional<DisConnectionHandler> handler(ServerDeployContext<? extends ServerOptions> ctx);

}

