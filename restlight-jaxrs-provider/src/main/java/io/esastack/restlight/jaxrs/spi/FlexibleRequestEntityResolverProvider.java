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
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverFactory;
import io.esastack.restlight.core.spi.RequestEntityResolverProvider;
import io.esastack.restlight.jaxrs.resolver.reqentity.FlexibleRequestEntityResolverFactoryImpl;

import java.util.Optional;

public class FlexibleRequestEntityResolverProvider implements RequestEntityResolverProvider {

    @Override
    public Optional<RequestEntityResolverFactory> factoryBean(DeployContext ctx) {
        SerializeOptions options = ctx.options().getSerialize().getRequest();
        return Optional.of(new FlexibleRequestEntityResolverFactoryImpl(options.isNegotiation(),
                options.getNegotiationParam()));
    }

}
