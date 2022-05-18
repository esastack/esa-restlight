/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver.factory;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.FutureTransferFactory;

import java.util.List;

public interface HandlerResolverFactory {

    /**
     * Obtains the {@link FutureTransfer} for given {@link HandlerMethod}.
     *
     * @param method method
     * @return future transfer
     */
    FutureTransfer getFutureTransfer(HandlerMethod method);

    /**
     * Obtains the {@link RouteFilter}s for given {@code route}.
     *
     * @param method handler method
     * @return filters
     */
    List<RouteFilter> getRouteFilters(HandlerMethod method);

    /**
     * Get the {@link ParamResolver} for given parameter no contains the entity param resolver.
     *
     * @param param parameter
     * @return resolver
     */
    ParamResolver getNoEntityParamResolver(Param param);

    /**
     * Get the {@link ParamResolver} for given parameter only contains the entity param resolver.
     *
     * @param param parameter
     * @return resolver
     */
    List<ParamResolver> getEntityParamResolvers(Param param);

    /**
     * Obtains the {@link ContextResolver} for given {@code param}.
     *
     * @param param param
     * @param deployContext  deployContext
     * @return context resolver
     */
    ContextResolver getContextResolver(Param param, DeployContext deployContext);

    /**
     * Obtains the {@link ParamResolverAdvice}s for given parameter.
     *
     * @param param param
     * @param entityAdvice entity advice or common advice
     * @return advices
     */
    List<ParamResolverAdvice> getParamResolverAdvices(Param param, boolean entityAdvice);

    /**
     * Get response entity resolvers, must not be {@code null}.
     *
     * @param handlerMethod     handler method
     * @return resolvers
     */
    List<ResponseEntityResolver> getResponseEntityResolvers(HandlerMethod handlerMethod);

    /**
     * Obtains the {@link ResponseEntityResolverAdvice}s for given {@link HandlerMethod}.
     *
     * @param handlerMethod     handler method
     * @return advices
     */
    List<ResponseEntityResolverAdvice> getResponseEntityResolverAdvices(HandlerMethod handlerMethod);

    /**
     * Returns all the {@link HttpRequestSerializer}s in this factory.
     *
     * @return immutable serializers
     */
    List<HttpRequestSerializer> rxSerializers();

    /**
     * Returns all the {@link HttpResponseSerializer}s in this factory.
     *
     * @return immutable serializers
     */
    List<HttpResponseSerializer> txSerializers();

    /**
     * Obtains all the {@link FutureTransferFactory}s in this factory.
     *
     * @return immutable factories
     */
    List<FutureTransferFactory> futureTransfers();

}
