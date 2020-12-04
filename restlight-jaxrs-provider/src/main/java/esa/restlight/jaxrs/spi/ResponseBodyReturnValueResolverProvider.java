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
package esa.restlight.jaxrs.spi;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.spi.ReturnValueResolverProvider;
import esa.restlight.jaxrs.resolver.result.ResponseBodyReturnValueResolver;

import java.util.Optional;

public class ResponseBodyReturnValueResolverProvider implements ReturnValueResolverProvider {

    @Override
    public Optional<ReturnValueResolverFactory> factoryBean(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new ResponseBodyReturnValueResolver(
                ctx.options().getSerialize().getRequest().isNegotiation(),
                ctx.options().getSerialize().getRequest().getNegotiationParam()));
    }
}
