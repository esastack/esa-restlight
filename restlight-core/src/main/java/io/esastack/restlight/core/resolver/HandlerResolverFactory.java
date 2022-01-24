/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver;

import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.FutureTransferFactory;

import java.lang.reflect.Type;
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
     * Obtains the {@link ParamResolver} for given {@code param} which is used to convert a {@link String} to object.
     *
     * @param type        type
     * @param genericType genericType
     * @param param       related Param, which may be {@code null}.
     * @return StringConverter
     */
    StringConverter getStringConverter(Class<?> type, Type genericType, Param param);

    /**
     * Get the {@link ParamResolver} for given parameter.
     *
     * @param param parameter
     * @return resolver
     */
    ParamResolver getParamResolver(Param param);

    /**
     * Obtains the {@link ContextResolver} for given {@code param}.
     *
     * @param param param
     * @return context resolver
     */
    ContextResolver getContextResolver(Param param);

    /**
     * Obtains the {@link ParamResolverAdvice}s for given parameter.
     *
     * @param param param
     * @return advices
     */
    List<ParamResolverAdvice> getParamResolverAdvices(Param param, ParamResolver resolver);

    /**
     * Obtains request entity resolvers, must not be {@code null}.
     *
     * @param param param
     * @return resolvers
     */
    List<RequestEntityResolver> getRequestEntityResolvers(Param param);

    /**
     * Obtains the {@link RequestEntityResolverAdvice}s for given {@link HandlerMethod}.
     *
     * @param handlerMethod handlerMethod
     * @return advices
     */
    List<RequestEntityResolverAdvice> getRequestEntityResolverAdvices(HandlerMethod handlerMethod);

    /**
     * Get response entity resolvers, must not be {@code null}.
     *
     * @return resolvers
     */
    List<ResponseEntityResolver> getResponseEntityResolvers();

    /**
     * Obtains the {@link ResponseEntityResolverAdvice}s for given {@link ResponseEntity}.
     *
     * @param entity response entity
     * @return advices
     */
    List<ResponseEntityResolverAdvice> getResponseEntityResolverAdvices(ResponseEntity entity);

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
