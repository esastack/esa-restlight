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
package io.esastack.restlight.core.deploy;

import esa.commons.collection.Attributes;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdviceAdapter;

public interface ConfigurableHandler {

    /**
     * Obtains {@link Attributes}.
     *
     * @return attrs
     */
    Attributes attrs();

    /**
     * Add {@link RouteFilter}s.
     *
     * @param filter route filter corresponding with specified {@link HandlerMethod}.
     * @return this configurable handler
     */
    ConfigurableHandler addRouteFilter(RouteFilter filter);

    /**
     * Add {@link ParamResolverAdapter}s.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolver(ParamResolverAdapter resolver);

    /**
     * Add {@link ParamResolverAdviceAdapter}.
     *
     * @param advice advice corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceAdapter advice);

    /**
     * Add {@link ContextResolverAdapter}s.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addContextResolver(ContextResolverAdapter resolver);

    /**
     * Add {@link ResponseEntityResolverAdapter}s.
     *
     * @param resolver resolver corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolver(ResponseEntityResolverAdapter resolver);

    /**
     * Add {@link ResponseEntityResolverAdviceAdapter}.
     *
     * @param advice advice corresponding with specified {@link HandlerMethod}.
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice);

}

