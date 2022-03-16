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
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Collections;
import java.util.Optional;

public class ConfigurableHandlerImpl implements ConfigurableHandler {

    private final HandlerMethod method;
    private final HandlerConfiguration configuration;

    public ConfigurableHandlerImpl(HandlerMethod method, HandlerConfiguration configuration) {
        Checks.checkNotNull(method, "method");
        Checks.checkNotNull(configuration, "configuration");
        this.method = method;
        this.configuration = configuration;
    }

    @Override
    public Attributes attrs() {
        return configuration.attrs();
    }

    @Override
    public ConfigurableHandler addRouteFilter(RouteFilter filter) {
        if (filter == null) {
            return this;
        }

        configuration.addRouteFilters(Collections.singleton(new RouteFilterFactory() {
            @Override
            public Optional<RouteFilter> create(HandlerMethod method) {
                return Optional.of(filter);
            }

            @Override
            public boolean supports(HandlerMethod method) {
                return ConfigurableHandlerImpl.this.method.method().equals(method.method());
            }
        }));

        return this;
    }

    @Override
    public ConfigurableHandler addParamResolver(ParamResolverAdapter resolver) {
        if (resolver == null) {
            return this;
        }
        configuration.addParamResolvers(Collections.singleton(ParamResolverFactory.singleton(resolver)));
        return this;
    }

    @Override
    public ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceAdapter advice) {
        if (advice == null) {
            return this;
        }
        configuration.addParamResolverAdvices(Collections.singleton(ParamResolverAdviceFactory.singleton(advice)));
        return this;
    }

    @Override
    public ConfigurableHandler addContextResolver(ContextResolverAdapter resolver) {
        if (resolver == null) {
            return this;
        }
        configuration.addContextResolvers(Collections.singletonList(ContextResolverFactory.singleton(resolver)));
        return this;
    }

    @Override
    public ConfigurableHandler addRequestEntityResolver(RequestEntityResolverAdapter resolver) {
        if (resolver == null) {
            return this;
        }
        configuration.addRequestEntityResolvers(Collections.singletonList(
                RequestEntityResolverFactory.singleton(resolver)));
        return this;
    }

    @Override
    public ConfigurableHandler addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice) {
        if (advice == null) {
            return this;
        }

        configuration.addRequestEntityResolverAdvices(Collections.singleton(new RequestEntityResolverAdviceFactory() {
            @Override
            public boolean supports(Param param) {
                if (param.isMethodParam()) {
                    return ConfigurableHandlerImpl.this.method.method().equals(param.methodParam().method());
                }
                return false;
            }

            @Override
            public RequestEntityResolverAdvice createResolverAdvice(Param param) {
                return advice;
            }
        }));

        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolver(ResponseEntityResolverAdapter resolver) {
        if (resolver == null) {
            return this;
        }

        configuration.addResponseEntityResolvers(
                Collections.singletonList(ResponseEntityResolverFactory.singleton(resolver)));
        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice) {
        if (advice == null) {
            return this;
        }

        configuration.addResponseEntityResolverAdvices(Collections.singleton(new ResponseEntityResolverAdviceFactory() {

            @Override
            public boolean supports(HandlerMethod method) {
                return advice.supports(method)
                        && ConfigurableHandlerImpl.this.method.method().equals(method.method());
            }

            @Override
            public boolean alsoApplyWhenMissingHandler() {
                return advice.alsoApplyWhenMissingHandler();
            }

            @Override
            public ResponseEntityResolverAdvice createResolverAdvice(HandlerMethod method) {
                return advice;
            }

            @Override
            public int getOrder() {
                return advice.getOrder();
            }
        }));
        return this;
    }
}

