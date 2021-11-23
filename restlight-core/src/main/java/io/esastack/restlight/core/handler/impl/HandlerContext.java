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

import io.esastack.httpserver.core.Attributes;
import io.esastack.httpserver.core.AttributesImpl;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.ConfigurableHandler;
import io.esastack.restlight.core.configure.ConfigurableHandlerImpl;
import io.esastack.restlight.core.configure.DelegatingDeployContext;
import io.esastack.restlight.core.configure.HandlerConfiguration;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static io.esastack.restlight.core.resolver.HandlerResolverFactoryImpl.buildConfiguration;
import static io.esastack.restlight.core.resolver.HandlerResolverFactoryImpl.getHandlerResolverFactory;

public class HandlerContext<O extends RestlightOptions> extends DelegatingDeployContext<O> {

    public HandlerContext(DeployContext<O> underlying) {
        super(underlying);
    }

    public static <C extends RestlightOptions> HandlerContext<C> build(DeployContext<C> ctx,
                                                                       HandlerMethod method) {
        Attributes attributes = new AttributesImpl();
        for (String name : ctx.attributeNames()) {
            attributes.setAttribute(name, ctx.attribute(name));
        }
        assert ctx.resolverFactory().isPresent();
        HandlerConfiguration configuration = buildConfiguration(ctx.resolverFactory().get(), attributes);
        ConfigurableHandler configurable = new ConfigurableHandlerImpl(configuration);
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
            public void attribute(String key, Object value) {
                configuration.setAttribute(key, value);
            }

            @Override
            public Object attribute(String key) {
                return configuration.getAttribute(key);
            }

            @Override
            public Object removeAttribute(String key) {
                return configuration.removeAttribute(key);
            }

            @Override
            public Collection<String> attributeNames() {
                return Collections.unmodifiableCollection(Arrays.asList(attributes.attributeNames()));
            }
        };
    }
}

