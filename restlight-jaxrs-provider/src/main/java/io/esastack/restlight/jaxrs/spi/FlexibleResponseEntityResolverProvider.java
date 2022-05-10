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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.SerializeOptions;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverFactory;
import io.esastack.restlight.core.spi.ResponseEntityResolverProvider;
import io.esastack.restlight.jaxrs.resolver.rspentity.FlexibleResponseEntityResolverFactory;
import io.esastack.restlight.jaxrs.resolver.rspentity.NegotiationResponseResolverFactory;

import java.util.Optional;

public class FlexibleResponseEntityResolverProvider implements ResponseEntityResolverProvider {

    @Override
    public Optional<ResponseEntityResolverFactory> factoryBean(DeployContext ctx) {
        SerializeOptions serializeOptions = ctx.options().getSerialize().getResponse();
        if (serializeOptions.isNegotiation()) {
            return Optional.of(new NegotiationResponseResolverFactory(
                    serializeOptions.getNegotiationParam()));
        } else {
            return Optional.of(new FlexibleResponseEntityResolverFactory());
        }
    }
}
