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
package io.esastack.restlight.core.handler.impl;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.ConfigurableHandler;
import io.esastack.restlight.core.configure.ConfigurableHandlerImpl;
import io.esastack.restlight.core.configure.DelegatingDeployContext;
import io.esastack.restlight.core.configure.HandlerConfiguration;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;

import java.util.Optional;

import static io.esastack.restlight.core.resolver.HandlerResolverFactoryImpl.buildConfiguration;
import static io.esastack.restlight.core.resolver.HandlerResolverFactoryImpl.getHandlerResolverFactory;

public class HandlerContext<O extends RestlightOptions> extends DelegatingDeployContext<O> {

    public HandlerContext(DeployContext<O> underlying) {
        super(underlying);
    }

    public static <C extends RestlightOptions> HandlerContext<C> build(DeployContext<C> ctx,
                                                                       HandlerMethod method) {
        Attributes attributes = new AttributeMap(ctx.attrs().size());
        ctx.attrs().forEach((name, value) -> attributes.attr(AttributeKey.valueOf(name.name())).set(value.get()));

        assert ctx.resolverFactory().isPresent();
        HandlerConfiguration configuration = buildConfiguration(ctx.resolverFactory().get(), attributes);
        ConfigurableHandler configurable = new ConfigurableHandlerImpl(method, configuration);
        if (ctx.handlerConfigures().isPresent()) {
            for (HandlerConfigure configure : ctx.handlerConfigures().get()) {
                configure.configure(method, configurable);
            }
        }
        HandlerResolverFactory resolverFactory = getHandlerResolverFactory(ctx.resolverFactory().get(), configuration);
        return new HandlerContext<C>(ctx) {
            @Override
            public Optional<HandlerResolverFactory> resolverFactory() {
                return Optional.of(resolverFactory);
            }

            @Override
            public Attributes attrs() {
                return configuration.attrs();
            }
        };
    }
}

