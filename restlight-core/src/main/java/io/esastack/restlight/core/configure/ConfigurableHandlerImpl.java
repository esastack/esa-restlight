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
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigurableHandlerImpl extends AttributesProxy implements ConfigurableHandler {

    private final HandlerMethod method;
    private final HandlerConfiguration configuration;

    public ConfigurableHandlerImpl(HandlerMethod method, HandlerConfiguration configuration) {
        super(configuration);
        Checks.checkNotNull(method, "method");
        Checks.checkNotNull(configuration, "configuration");
        this.method = method;
        this.configuration = configuration;
    }

    @Override
    public ConfigurableHandler addRouteFilters(Collection<? extends RouteFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return this;
        }

        configuration.addRouteFilters(filters.stream().map(f -> new RouteFilterFactory() {
            @Override
            public Optional<RouteFilter> create(HandlerMethod method) {
                return Optional.of(f);
            }

            @Override
            public boolean supports(HandlerMethod handlerMethod) {
                return method.equals(handlerMethod);
            }
        }).collect(Collectors.toList()));

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
    public ConfigurableHandler addParamResolvers(Collection<? extends ParamResolver> resolvers) {
        if (resolvers == null || resolvers.isEmpty()) {
            return this;
        }

        configuration.addParamResolvers(resolvers.stream().map(r -> new ParamResolverFactory() {
            @Override
            public boolean supports(Param param) {
                return ConfigurableHandlerImpl.this.supports(param);
            }

            @Override
            public ParamResolver createResolver(Param param, List<? extends HttpRequestSerializer> serializers) {
                return r;
            }
        }).collect(Collectors.toList()));

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
    public ConfigurableHandler addParamResolverAdvices(Collection<? extends ParamResolverAdvice> advices) {
        if (advices == null || advices.isEmpty()) {
            return this;
        }

        configuration.addParamResolverAdvices(advices.stream().map(advice -> new ParamResolverAdviceFactory() {
            @Override
            public boolean supports(Param param) {
                return ConfigurableHandlerImpl.this.supports(param);
            }

            @Override
            public ParamResolverAdvice createResolverAdvice(Param param, ParamResolver resolver) {
                return advice;
            }
        }).collect(Collectors.toList()));

        return this;
    }

    @Override
    public ConfigurableHandler addContextResolvers(Collection<? extends ContextResolver> resolvers) {
        if (resolvers == null || resolvers.isEmpty()) {
            return this;
        }

        configuration.addContextResolvers(resolvers.stream().map(resolver -> new ContextResolverFactory() {
            @Override
            public boolean supports(Param param) {
                return ConfigurableHandlerImpl.this.supports(param);
            }

            @Override
            public ContextResolver createResolver(Param param) {
                return resolver;
            }
        }).collect(Collectors.toList()));

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
    public ConfigurableHandler addRequestEntityResolvers(Collection<? extends RequestEntityResolver> resolvers) {
        if (resolvers == null || resolvers.isEmpty()) {
            return this;
        }

        configuration.addRequestEntityResolvers(resolvers.stream().map(resolver -> new RequestEntityResolverFactory() {
            @Override
            public RequestEntityResolver createResolver(Param param,
                                                        List<? extends HttpRequestSerializer> serializers) {
                return resolver;
            }

            @Override
            public boolean supports(Param param) {
                return ConfigurableHandlerImpl.this.supports(param);
            }
        }).collect(Collectors.toList()));

        return this;
    }

    @Override
    public ConfigurableHandler addRequestEntityResolverAdvices(Collection<? extends RequestEntityResolverAdvice>
                                                                           advices) {
        if (advices == null || advices.isEmpty()) {
            return this;
        }

        configuration.addRequestEntityResolverAdvices(advices.stream().map(advice ->
                new RequestEntityResolverAdviceFactory() {
            @Override
            public boolean supports(HandlerMethod handlerMethod) {
                return method.equals(handlerMethod);
            }

            @Override
            public RequestEntityResolverAdvice createResolverAdvice(HandlerMethod method) {
                return advice;
            }
        }).collect(Collectors.toList()));

        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolvers(Collection<? extends ResponseEntityResolver> resolvers) {
        if (resolvers == null || resolvers.isEmpty()) {
            return this;
        }

        configuration.addResponseEntityResolvers(resolvers.stream().map(resolver ->
                (ResponseEntityResolverFactory) serializers -> resolver)
                .collect(Collectors.toList()));
        return this;
    }

    @Override
    public ConfigurableHandler addResponseEntityResolverAdvices(Collection<? extends ResponseEntityResolverAdvice>
                                                                            advices) {
        if (advices == null || advices.isEmpty()) {
            return this;
        }

        configuration.addResponseEntityResolverAdvices(advices.stream().map(advice ->
                new ResponseEntityResolverAdviceFactory() {
                    @Override
                    public boolean supports(HandlerMethod handlerMethod) {
                        return method.equals(handlerMethod);
                    }

                    @Override
                    public ResponseEntityResolverAdvice createResolverAdvice(ResponseEntity entity) {
                        return advice;
                    }
                }).collect(Collectors.toList()));

        return this;
    }

    private boolean supports(Param param) {
        if (!param.isMethodParam()) {
            return false;
        }
        MethodParam mParam = param.methodParam();
        return mParam.type().equals(method.beanType()) && mParam.method().equals(method.method());
    }

}

