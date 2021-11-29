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

import io.esastack.httpserver.core.Attributes;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;

import java.util.Collection;

public interface ConfigurableHandler extends Attributes {

    /**
     * Add {@link RouteFilter}s.
     *
     * @param filters    route filters corresponding with specified {@link HandlerMethod}.
     * @return this configurable handler
     */
    ConfigurableHandler addRouteFilters(Collection<? extends RouteFilter> filters);

    /**
     * Add {@link ParamResolver}s.
     *
     * @param resolvers resolvers corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolvers(Collection<? extends ParamResolver> resolvers);

    /**
     * Add {@link ParamResolverAdapter}s.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolver(ParamResolverAdapter resolver);

    /**
     * Add {@link ParamResolverAdvice}.
     *
     * @param advices advices corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvices(Collection<? extends ParamResolverAdvice> advices);

    /**
     * Add {@link ParamResolverAdviceAdapter}.
     *
     * @param advice advice corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceAdapter advice);

    /**
     * Add {@link ContextResolver}s.
     *
     * @param resolvers resolvers corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addContextResolvers(Collection<? extends ContextResolver> resolvers);

    /**
     * Add {@link ContextResolverAdapter}s.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addContextResolver(ContextResolverAdapter resolver);

    /**
     * Add {@link RequestEntityResolver}s.
     *
     * @param resolvers resolvers corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolvers(Collection<? extends RequestEntityResolver> resolvers);

    /**
     * Add {@link RequestEntityResolverAdapter}.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolver(RequestEntityResolverAdapter resolver);

    /**
     * Add {@link RequestEntityResolverAdvice}s.
     *
     * @param advices advices corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolverAdvices(Collection<? extends RequestEntityResolverAdvice> advices);

    /**
     * Add {@link ResponseEntityResolver}s.
     *
     * @param resolvers resolvers corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolvers(Collection<? extends ResponseEntityResolver> resolvers);

    /**
     * Add {@link ResponseEntityResolverAdvice}s.
     *
     * @param advices advices corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolverAdvices(Collection<? extends ResponseEntityResolverAdvice> advices);

}

