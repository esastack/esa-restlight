/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.server.spi;

import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.server.handler.ConnectionInitHandler;

import java.util.Optional;

/**
 * The factory to create a {@link ConnectionInitHandler}
 */
@SPI
@FunctionalInterface
public interface ConnectionInitHandlerFactory {

    /**
     * Creates an optional instance of {@link ConnectionInitHandler}.
     *
     * @param ctx ctx
     * @return an optional {@link ConnectionInitHandler}, which must not be {@code null}.
     */
    Optional<ConnectionInitHandler> handler(DeployContext ctx);
}
