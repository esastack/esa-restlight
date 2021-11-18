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

import esa.commons.Checks;
import io.esastack.httpserver.impl.AttributesProxy;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Collection;
import java.util.Collections;

public class ConfigurableHandlerImpl extends AttributesProxy implements ConfigurableHandler {

    private final HandlerConfiguration configuration;

    public ConfigurableHandlerImpl(HandlerConfiguration configuration) {
        super(configuration);
        Checks.checkNotNull(configuration, "configuration");
        this.configuration = configuration;
    }

    @Override
    public ConfigurableHandler addRouteFilter(RouteFilterFactory filter) {
        Checks.checkNotNull(filter, "filter");
        return addRouteFilters(Collections.singletonList(filter));
    }

    @Override
    public ConfigurableHandler addRouteFilters(Collection<? extends RouteFilterFactory> filters) {
        configuration.addRouteFilters(filters);
        return this;
    }

    @Override
    public ConfigurableHandler addParamResolver(ParamResolverAdapter resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addParamResolver(ParamResolverFactory.singleton(resolver));
    }

    @Override
    public ConfigurableHandler addParamResolver(ParamResolverFactory resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addParamResolvers(Collections.singleton(resolver));
    }

    @Override
    public ConfigurableHandler addParamResolvers(Collection<? extends ParamResolverFactory> resolvers) {
        configuration.addParamResolvers(resolvers);
        return this;
    }

    @Override
    public ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceAdapter advice) {
        Checks.checkNotNull(advice, "advice");
        return addParamResolverAdvice(ParamResolverAdviceFactory.singleton(advice));
    }

    @Override
    public ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceFactory advice) {
        Checks.checkNotNull(advice, "advice");
        return addParamResolverAdvices(Collections.singleton(advice));
    }

    @Override
    public ConfigurableHandler addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory>
                                                                         advices) {
        configuration.addParamResolverAdvices(advices);
        return this;
    }

    @Override
    public ConfigurableHandler addContextResolver(ContextResolverAdapter resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addContextResolver(ContextResolverFactory.singleton(resolver));
    }

    @Override
    public ConfigurableHandler addContextResolver(ContextResolverFactory resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addContextResolvers(Collections.singletonList(resolver));
    }

    @Override
    public ConfigurableHandler addContextResolvers(Collection<? extends ContextResolverFactory> resolvers) {
        configuration.addContextResolvers(resolvers);
        return this;
    }

    @Override
    public ConfigurableHandler addRequestEntityResolver(RequestEntityResolverAdapter resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addRequestEntityResolver(RequestEntityResolverFactory.singleton(resolver));
    }

    @Override
    public ConfigurableHandler addRequestEntityResolver(RequestEntityResolverFactory resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addRequestEntityResolvers(Collections.singletonList(resolver));
    }

    @Override
    public ConfigurableHandler addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory>
                                                                           resolvers) {
        configuration.addRequestEntityResolvers(resolvers);
        return this;
    }

    @Override
    public ConfigurableHandler addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice) {
        Checks.checkNotNull(advice, "advice");
        return addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory.singleton(advice));
    }

    @Override
    public ConfigurableHandler addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory advice) {
        Checks.checkNotNull(advice, "advice");
        return addRequestEntityResolverAdvices(Collections.singleton(advice));
    }

    @Override
    public ConfigurableHandler addRequestEntityResolverAdvices(Collection<? extends
            RequestEntityResolverAdviceFactory> advices) {
        configuration.addRequestEntityResolverAdvices(advices);
        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolver(ResponseEntityResolver resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addResponseEntityResolver(ResponseEntityResolverFactory.singleton(resolver));
    }

    @Override
    public ConfigurableHandler addResponseEntityResolver(ResponseEntityResolverFactory resolver) {
        Checks.checkNotNull(resolver, "resolver");
        return addResponseEntityResolvers(Collections.singletonList(resolver));
    }

    @Override
    public ConfigurableHandler addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory>
                                                                            resolvers) {
        configuration.addResponseEntityResolvers(resolvers);
        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice) {
        Checks.checkNotNull(advice, "advice");
        return addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory.singleton(advice));
    }

    @Override
    public ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory advice) {
        Checks.checkNotNull(advice, "advice");
        return addResponseEntityResolverAdvices(Collections.singleton(advice));
    }

    @Override
    public ConfigurableHandler addResponseEntityResolverAdvices(Collection<? extends
            ResponseEntityResolverAdviceFactory> advices) {
        configuration.addResponseEntityResolverAdvices(advices);
        return this;
    }
}

