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
package io.esastack.restlight.core.spi;

import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;

import java.util.Optional;

/**
 * Produces an optional instance of {@link ParamResolverAdviceFactory} which will be added to the Restlight context.
 */
@SPI
public interface ParamResolverAdviceProvider {

    /**
     * Produces a an optional instance of {@link ParamResolverAdviceFactory}.
     *
     * @param ctx deploy context
     *
     * @return Optional value of {@link ParamResolverAdviceFactory}.
     */
    Optional<ParamResolverAdviceFactory> factoryBean(DeployContext<? extends RestlightOptions> ctx);

}
