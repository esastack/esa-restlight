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
package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamPredicate;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;
import java.util.Optional;

public abstract class NameAndValueResolverFactory implements ParamResolverProvider, ParamPredicate, Ordered {

    /**
     * Produces a an optional instance of {@link ParamResolverFactory}.
     *
     * @param ctx deploy context
     * @return Optional value of {@link ParamResolverFactory}.
     */
    @Override
    public Optional<ParamResolverFactory> factoryBean(DeployContext ctx) {
        return Optional.of(new ParamResolverFactory() {

            @Override
            public boolean supports(Param param) {
                return NameAndValueResolverFactory.this.supports(param);
            }

            @Override
            public NameAndValueResolverAdapter createResolver(Param param,
                                                              List<? extends HttpRequestSerializer> serializers) {

                return new NameAndValueResolverAdapter(param,
                        NameAndValueResolverFactory
                                .this.createResolver(param, ctx.resolverFactory().orElse(null)));
            }

            @Override
            public int getOrder() {
                return NameAndValueResolverFactory.this.getOrder();
            }
        });
    }

    /**
     * Creates an instance of {@link NameAndValueResolver} for given handler method.
     *
     * @param param           method
     * @param resolverFactory the {@link HandlerResolverFactory} of current context
     * @return resolver
     */
    public abstract NameAndValueResolver createResolver(Param param, HandlerResolverFactory resolverFactory);
}
