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
package io.esastack.restlight.jaxrs.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.spi.HandlerFactoryProvider;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.configure.JaxrsHandlerFactory;

import java.util.Optional;

@Internal
@Feature(tags = Constants.INTERNAL)
public class JaxrsHandlerFactoryProvider implements HandlerFactoryProvider {

    @Override
    public Optional<HandlerFactory> factoryBean(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new JaxrsHandlerFactory(ctx, ctx.handlers().orElse(null)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

