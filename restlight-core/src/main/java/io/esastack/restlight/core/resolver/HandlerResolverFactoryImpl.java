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

import esa.commons.Checks;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.configure.HandlerConfiguration;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.FutureTransferFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.core.util.OrderedComparator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HandlerResolverFactoryImpl implements HandlerResolverFactory {

    /**
     * serializers in current context
     */
    private final List<HttpRequestSerializer> rxSerializers;
    private final List<HttpResponseSerializer> txSerializers;

    private final List<FutureTransferFactory> futureTransfers;
    private final List<RouteFilterFactory> routeFilters;

    private final List<StringConverterFactory> stringConverters;
    private final List<ParamResolverFactory> paramResolvers;
    private final List<ParamResolverAdviceFactory> paramResolverAdvices;
    private final List<ContextResolverFactory> contextResolvers;

    /**
     * Customize request entity resolvers
     */
    private final List<RequestEntityResolverFactory> requestEntityResolvers;
    private final List<RequestEntityResolverAdviceFactory> requestEntityResolverAdvices;

    /**
     * Customize response entity resolvers
     */
    private final List<ResponseEntityResolver> responseEntityResolvers;
    private final List<ResponseEntityResolverAdviceFactory> responseEntityResolverAdvices;

    public HandlerResolverFactoryImpl(Collection<? extends HttpRequestSerializer> rxSerializers,
                                      Collection<? extends HttpResponseSerializer> txSerializers,
                                      Collection<? extends FutureTransferFactory> futureTransfers,
                                      Collection<? extends RouteFilterFactory> routeFilters,
                                      Collection<? extends StringConverterFactory> stringConverters,
                                      Collection<? extends ParamResolverAdapter> paramResolvers,
                                      Collection<? extends ParamResolverFactory> paramResolverFactories,
                                      Collection<? extends ParamResolverAdviceAdapter> paramResolverAdvices,
                                      Collection<? extends ParamResolverAdviceFactory> paramResolverAdviceFactories,
                                      Collection<? extends ContextResolverAdapter> contextResolvers,
                                      Collection<? extends ContextResolverFactory> contextResolverFactories,
                                      Collection<? extends RequestEntityResolverFactory>
                                              requestEntityResolverFactories,
                                      Collection<? extends RequestEntityResolverAdviceAdapter>
                                              requestEntityResolverAdvices,
                                      Collection<? extends RequestEntityResolverAdviceFactory>
                                              requestEntityResolverAdviceFactories,
                                      Collection<? extends ResponseEntityResolverFactory>
                                              responseEntityResolverFactories,
                                      Collection<? extends ResponseEntityResolverAdviceAdapter>
                                              responseEntityResolverAdvices,
                                      Collection<? extends ResponseEntityResolverAdviceFactory>
                                              responseEntityResolverAdviceFactories) {
        Checks.checkNotEmptyArg(rxSerializers, "rxSerializers");
        Checks.checkNotEmptyArg(txSerializers, "txSerializers");
        this.rxSerializers = sortForUnmodifiableList(rxSerializers);
        this.txSerializers = sortForUnmodifiableList(txSerializers);

        this.futureTransfers = sortForUnmodifiableList(futureTransfers);
        this.routeFilters = sortForUnmodifiableList(routeFilters);
        this.stringConverters = sortForUnmodifiableList(stringConverters);
        this.paramResolvers = getParamResolvers(paramResolvers, paramResolverFactories);
        this.paramResolverAdvices = getParamResolverAdvices(paramResolverAdvices,
                paramResolverAdviceFactories);
        this.contextResolvers = getContextResolvers(contextResolvers, contextResolverFactories);
        this.requestEntityResolvers = sortForUnmodifiableList(requestEntityResolverFactories);
        this.requestEntityResolverAdvices = getRequestEntityResolverAdvices(requestEntityResolverAdvices,
                requestEntityResolverAdviceFactories);
        this.responseEntityResolvers = instantiateResponseEntityResolvers(
                sortForUnmodifiableList(responseEntityResolverFactories), txSerializers);
        this.responseEntityResolverAdvices = getResponseEntityResolverAdvices(responseEntityResolverAdvices,
                responseEntityResolverAdviceFactories);
    }

    private static List<ResponseEntityResolver> instantiateResponseEntityResolvers(List<ResponseEntityResolverFactory>
                                                                                           factories,
                                                                                   Collection<? extends
                                                                                           HttpResponseSerializer>
                                                                                           txSerializers) {
        final List<HttpResponseSerializer> txSerializers0 = new ArrayList<>(txSerializers.size());
        txSerializers0.addAll(txSerializers);
        final List<ResponseEntityResolver> resolvers = new ArrayList<>(factories.size());
        factories.forEach(factory -> resolvers.add(factory.createResolver(txSerializers0)));
        OrderedComparator.sort(resolvers);
        return resolvers;
    }

    private static List<RequestEntityResolverAdviceFactory> getRequestEntityResolverAdvices(
            Collection<? extends RequestEntityResolverAdviceAdapter> resolvers,
            Collection<? extends RequestEntityResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                RequestEntityResolverAdviceFactory::singleton);
    }

    private static List<ResponseEntityResolverAdviceFactory> getResponseEntityResolverAdvices(
            Collection<? extends ResponseEntityResolverAdviceAdapter> resolvers,
            Collection<? extends ResponseEntityResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ResponseEntityResolverAdviceFactory::singleton);
    }

    private static List<ParamResolverFactory> getParamResolvers(
            Collection<? extends ParamResolverAdapter> resolvers,
            Collection<? extends ParamResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ParamResolverFactory::singleton);
    }

    private static List<ContextResolverFactory> getContextResolvers(
            Collection<? extends ContextResolverAdapter> resolvers,
            Collection<? extends ContextResolverFactory> factories) {
        return mergeResolvers(resolvers,
                factories,
                ContextResolverFactory::singleton);
    }

    private static List<ParamResolverAdviceFactory> getParamResolverAdvices(
            Collection<? extends ParamResolverAdviceAdapter> resolvers,
            Collection<? extends ParamResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ParamResolverAdviceFactory::singleton);
    }

    private static <R, F> List<F> mergeResolvers(Collection<? extends R> resolvers,
                                                 Collection<? extends F> factories,
                                                 Function<R, F> transfer) {
        List<F> arguments = factories == null
                ? new ArrayList<>()
                : new ArrayList<>(factories);

        if (resolvers != null) {
            arguments.addAll(resolvers.stream()
                    .map(transfer)
                    .collect(Collectors.toList()));
        }

        // sort for custom argument resolvers
        OrderedComparator.sort(arguments);

        return Collections.unmodifiableList(arguments);
    }

    private static <T> List<T> sortForUnmodifiableList(Collection<? extends T> components) {
        if (components == null) {
            return Collections.emptyList();
        }

        List<T> tmpComponents = new ArrayList<>(components);
        //sort
        OrderedComparator.sort(tmpComponents);
        return Collections.unmodifiableList(tmpComponents);
    }

    @Override
    public FutureTransfer getFutureTransfer(HandlerMethod method) {
        Optional<FutureTransfer> transfer;
        for (FutureTransferFactory factory : futureTransfers) {
            transfer = factory.futureTransfer(method);
            if (transfer.isPresent()) {
                return transfer.get();
            }
        }
        return null;
    }

    @Override
    public List<RouteFilter> getRouteFilters(HandlerMethod method) {
        List<RouteFilter> filters = routeFilters.stream().map(factory -> factory.create(method))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        OrderedComparator.sort(filters);
        return filters;
    }

    @Override
    public StringConverter getStringConverter(Class<?> type, Type genericType, Param param) {
        //resolve the fixed parameter resolver
        Optional<StringConverter> converter;
        for (StringConverterFactory factory : stringConverters) {
            if ((converter = factory.createConverter(type, genericType, param)).isPresent()) {
                return converter.get();
            }
        }
        return null;
    }

    @Override
    public ParamResolver getParamResolver(Param param) {
        //resolve the fixed parameter resolver
        return paramResolvers.stream().filter(r -> r.supports(param))
                .findFirst()
                .map(factory -> Checks.checkNotNull(factory.createResolver(param, rxSerializers),
                        "Failed to create param resolver for parameter: " + param))
                .orElse(null);
    }

    @Override
    public List<ParamResolverAdvice> getParamResolverAdvices(Param param, ParamResolver resolver) {
        if (paramResolverAdvices != null && !paramResolverAdvices.isEmpty()) {
            List<ParamResolverAdvice> advices =
                    paramResolverAdvices.stream()
                            .filter(advice -> advice.supports(param))
                            .map(factory -> Checks.checkNotNull(factory.createResolverAdvice(param, resolver),
                                    "Failed to create param resolver advice for parameter: " + param))
                            .collect(Collectors.toList());

            if (!advices.isEmpty()) {
                return Collections.unmodifiableList(advices);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public ContextResolver getContextResolver(Param param) {
        //resolve the fixed parameter resolver
        return contextResolvers.stream().filter(r -> r.supports(param))
                .findFirst()
                .map(factory -> Checks.checkNotNull(factory.createResolver(param),
                        "Failed to create context resolver for parameter: " + param))
                .orElse(null);
    }

    @Override
    public List<RequestEntityResolver> getRequestEntityResolvers(Param param) {
        List<RequestEntityResolver> resolvers = new ArrayList<>();
        requestEntityResolvers.forEach(factory -> {
            if (factory.supports(param)) {
                resolvers.add(factory.createResolver(param, rxSerializers));
            }
        });
        return sortForUnmodifiableList(resolvers);
    }

    @Override
    public List<RequestEntityResolverAdvice> getRequestEntityResolverAdvices(HandlerMethod handlerMethod) {
        if (requestEntityResolverAdvices != null && !requestEntityResolverAdvices.isEmpty()) {
            List<RequestEntityResolverAdvice> advices =
                    requestEntityResolverAdvices.stream()
                            .filter(advice -> advice.supports(handlerMethod))
                            .map(factory -> Checks.checkNotNull(factory.createResolverAdvice(handlerMethod),
                                    "Failed to create request entity resolver advice for handler: "
                                            + handlerMethod))
                            .collect(Collectors.toList());
            if (!advices.isEmpty()) {
                return Collections.unmodifiableList(advices);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<ResponseEntityResolver> getResponseEntityResolvers() {
        return responseEntityResolvers;
    }

    @Override
    public List<ResponseEntityResolverAdvice> getResponseEntityResolverAdvices(ResponseEntity entity) {
        if (responseEntityResolverAdvices != null && !responseEntityResolverAdvices.isEmpty()) {
            List<ResponseEntityResolverAdvice> advices =
                    responseEntityResolverAdvices.stream()
                            .filter(advice -> advice.supports(entity.handler().orElse(null)))
                            .map(factory -> Checks.checkNotNull(factory.createResolverAdvice(entity),
                                    "Failed to create response entity resolver advice for response entity: "
                                            + entity))
                            .collect(Collectors.toList());
            if (!advices.isEmpty()) {
                return Collections.unmodifiableList(advices);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<HttpResponseSerializer> txSerializers() {
        return Collections.unmodifiableList(txSerializers);
    }

    @Override
    public List<HttpRequestSerializer> rxSerializers() {
        return Collections.unmodifiableList(rxSerializers);
    }

    @Override
    public List<FutureTransferFactory> futureTransfers() {
        return Collections.unmodifiableList(futureTransfers);
    }

    public static HandlerConfiguration buildConfiguration(HandlerResolverFactory resolverFactory,
                                                          Attributes attributes) {
        if (resolverFactory instanceof HandlerResolverFactoryImpl) {
            HandlerResolverFactoryImpl factory = (HandlerResolverFactoryImpl) resolverFactory;
            List<ResponseEntityResolverFactory> responseEntityResolvers = new LinkedList<>();
            factory.responseEntityResolvers.forEach(resolver -> responseEntityResolvers
                    .add(ResponseEntityResolverFactory.singleton(resolver)));
            return new HandlerConfiguration(attributes,
                    new LinkedList<>(factory.routeFilters),
                    new LinkedList<>(factory.stringConverters),
                    new LinkedList<>(factory.paramResolvers),
                    new LinkedList<>(factory.paramResolverAdvices),
                    new LinkedList<>(factory.contextResolvers),
                    new LinkedList<>(factory.requestEntityResolvers),
                    new LinkedList<>(factory.requestEntityResolverAdvices),
                    responseEntityResolvers,
                    new LinkedList<>(factory.responseEntityResolverAdvices));
        } else {
            return new HandlerConfiguration(attributes,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());
        }
    }

    public static HandlerResolverFactory getHandlerResolverFactory(HandlerResolverFactory factory,
                                                                   HandlerConfiguration configuration) {
        // keep in order.
        OrderedComparator.sort(configuration.getParamResolvers());
        OrderedComparator.sort(configuration.getContextResolvers());
        OrderedComparator.sort(configuration.getParamResolverAdvices());
        OrderedComparator.sort(configuration.getRequestEntityResolvers());
        OrderedComparator.sort(configuration.getRequestEntityResolverAdvices());
        OrderedComparator.sort(configuration.getResponseEntityResolvers());
        OrderedComparator.sort(configuration.getResponseEntityResolverAdvices());

        return new HandlerResolverFactoryImpl(
                factory.rxSerializers(),
                factory.txSerializers(),
                factory.futureTransfers(),
                configuration.getRouteFilters(),
                configuration.getStringConverts(),
                null,
                configuration.getParamResolvers(),
                null,
                configuration.getParamResolverAdvices(),
                null,
                configuration.getContextResolvers(),
                configuration.getRequestEntityResolvers(),
                null,
                configuration.getRequestEntityResolverAdvices(),
                configuration.getResponseEntityResolvers(),
                null,
                configuration.getResponseEntityResolverAdvices());
    }
}
