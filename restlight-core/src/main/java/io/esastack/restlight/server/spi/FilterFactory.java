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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.server.handler.Filter;

import java.util.Optional;

/**
 * The factory to create {@link Filter}.
 */
@SPI
public interface FilterFactory {

    /**
     * Creates an optional instance of {@link Filter} for give target handler.
     *
     * @param ctx deploy context
     * @return optional value of {@link Filter}
     */
    Optional<Filter> filter(DeployContext ctx);

}

