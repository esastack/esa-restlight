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
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
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

public interface ConfigurableHandler extends Attributes {

    /**
     * Adds {@link RouteFilterFactory}.
     *
     * @param filter    route filter factory
     * @return this configurable handler
     */
    ConfigurableHandler addRouteFilter(RouteFilterFactory filter);

    /**
     * Adds {@link RouteFilterFactory}s.
     *
     * @param filters    route filter factories
     * @return this configurable handler
     */
    ConfigurableHandler addRouteFilters(Collection<? extends RouteFilterFactory> filters);

    /**
     * Adds {@link ParamResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolver(ParamResolverAdapter resolver);

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolver(ParamResolverFactory resolver);

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolvers(Collection<? extends ParamResolverFactory> resolvers);

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceAdapter advice);

    /**
     * Adds {@link ParamResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvice(ParamResolverAdviceFactory advice);

    /**
     * Adds {@link ParamResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices advices
     *
     * @return this configurable
     */
    ConfigurableHandler addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory>
                                                                       advices);

    /**
     * Adds {@link ContextResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addContextResolver(ContextResolverAdapter resolver);

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addContextResolver(ContextResolverFactory resolver);

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addContextResolvers(Collection<? extends ContextResolverFactory> resolvers);

    /**
     * Adds {@link RequestEntityResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolver(RequestEntityResolverAdapter resolver);

    /**
     * Adds {@link RequestEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolver(RequestEntityResolverFactory resolver);

    /**
     * Adds {@link RequestEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory>
                                                                         resolvers);

    /**
     * Adds {@link RequestEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice);

    /**
     * Adds {@link RequestEntityResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory advice);

    /**
     * Adds {@link RequestEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addRequestEntityResolverAdvices(
            Collection<? extends RequestEntityResolverAdviceFactory> advices);

    /**
     * Adds {@link ResponseEntityResolver}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolver(ResponseEntityResolver resolver);

    /**
     * Adds {@link ResponseEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolver(ResponseEntityResolverFactory resolver);

    /**
     * Adds {@link ResponseEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory>
                                                                          resolvers);

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice);

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory advice);

    /**
     * Adds {@link ResponseEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     *
     * @return this configurable
     */
    ConfigurableHandler addResponseEntityResolverAdvices(
            Collection<? extends ResponseEntityResolverAdviceFactory> advices);

}

