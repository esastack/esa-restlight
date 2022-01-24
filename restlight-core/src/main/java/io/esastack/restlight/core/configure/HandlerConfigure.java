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
package io.esastack.restlight.core.configure;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.util.Ordered;

/**
 * This interface is used to configure {@link ConfigurableHandler} dynamically when {@link HandlerMethod}
 * is prepared to be registered.
 */
@FunctionalInterface
@Internal
@SPI
public interface HandlerConfigure extends Ordered {

    /**
     * Configures the {@link ConfigurableHandler} when the given {@code handlerMethod} is prepare to be
     * registered.
     *
     * @param handlerMethod handler method
     * @param configurable  configurable
     */
    void configure(HandlerMethod handlerMethod, ConfigurableHandler configurable);

}

