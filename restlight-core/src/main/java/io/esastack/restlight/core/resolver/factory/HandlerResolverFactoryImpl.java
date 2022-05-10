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

import esa.commons.Checks;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.deploy.HandlerConfiguration;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.context.ContextResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterAdapter;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.param.HttpParamResolver;
import io.esastack.restlight.core.resolver.param.HttpParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.HttpParamResolverAdvice;
import io.esastack.restlight.core.resolver.param.HttpParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.HttpParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.HttpParamResolverFactory;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolver;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.FutureTransferFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.core.util.OrderedComparator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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
    private final List<HttpParamResolverFactory> paramResolvers;
    private final List<HttpParamResolverAdviceFactory> paramResolverAdvices;
    private final List<ContextResolverFactory> contextResolvers;

    /**
     * Customize request entity resolvers
     */
    private final List<RequestEntityResolverFactory> requestEntityResolvers;
    private final List<RequestEntityResolverAdviceFactory> requestEntityResolverAdvices;

    /**
     * Customize response entity resolvers
     */
    private final List<ResponseEntityResolverFactory> responseEntityResolvers;
    private final List<ResponseEntityResolverAdviceFactory> responseEntityResolverAdvices;

    private final ResponseResolversFactory responseResolversFactory;

    public HandlerResolverFactoryImpl(Collection<? extends HttpRequestSerializer> rxSerializers,
                                      Collection<? extends HttpResponseSerializer> txSerializers,
                                      Collection<? extends FutureTransferFactory> futureTransfers,
                                      Collection<? extends RouteFilterFactory> routeFilters,
                                      Collection<? extends StringConverterAdapter> stringConverters,
                                      Collection<? extends StringConverterFactory> stringConverterFactories,
                                      Collection<? extends HttpParamResolverAdapter> paramResolvers,
                                      Collection<? extends HttpParamResolverFactory> paramResolverFactories,
                                      Collection<? extends HttpParamResolverAdviceAdapter> paramResolverAdvices,
                                      Collection<? extends HttpParamResolverAdviceFactory> paramResolverAdviceFactories,
                                      Collection<? extends ContextResolverAdapter> contextResolvers,
                                      Collection<? extends ContextResolverFactory> contextResolverFactories,
                                      Collection<? extends RequestEntityResolverAdapter> requestEntityResolvers,
                                      Collection<? extends RequestEntityResolverFactory>
                                              requestEntityResolverFactories,
                                      Collection<? extends RequestEntityResolverAdviceAdapter>
                                              requestEntityResolverAdvices,
                                      Collection<? extends RequestEntityResolverAdviceFactory>
                                              requestEntityResolverAdviceFactories,
                                      Collection<? extends ResponseEntityResolverAdapter>
                                              responseEntityResolvers,
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
        this.stringConverters = getStringConverters(stringConverters, stringConverterFactories);
        this.paramResolvers = getParamResolvers(paramResolvers, paramResolverFactories);
        this.paramResolverAdvices = getParamResolverAdvices(paramResolverAdvices,
                paramResolverAdviceFactories);
        this.contextResolvers = getContextResolvers(contextResolvers, contextResolverFactories);
        this.requestEntityResolvers = getRequestEntityResolvers(requestEntityResolvers,
                requestEntityResolverFactories);
        this.requestEntityResolverAdvices = getRequestEntityResolverAdvices(requestEntityResolverAdvices,
                requestEntityResolverAdviceFactories);
        this.responseEntityResolvers = getResponseEntityResolvers(responseEntityResolvers,
                responseEntityResolverFactories);
        this.responseEntityResolverAdvices = getResponseEntityResolverAdvices(responseEntityResolverAdvices,
                responseEntityResolverAdviceFactories);
        this.responseResolversFactory = new ResponseResolversFactory();
    }

    private static List<RequestEntityResolverFactory> getRequestEntityResolvers(
            Collection<? extends RequestEntityResolverAdapter> resolvers,
            Collection<? extends RequestEntityResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                RequestEntityResolverFactory::singleton);
    }

    private static List<RequestEntityResolverAdviceFactory> getRequestEntityResolverAdvices(
            Collection<? extends RequestEntityResolverAdviceAdapter> resolvers,
            Collection<? extends RequestEntityResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                RequestEntityResolverAdviceFactory::singleton);
    }

    private static List<ResponseEntityResolverFactory> getResponseEntityResolvers(
            Collection<? extends ResponseEntityResolverAdapter> resolvers,
            Collection<? extends ResponseEntityResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ResponseEntityResolverFactory::singleton);
    }

    private static List<ResponseEntityResolverAdviceFactory> getResponseEntityResolverAdvices(
            Collection<? extends ResponseEntityResolverAdviceAdapter> resolvers,
            Collection<? extends ResponseEntityResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ResponseEntityResolverAdviceFactory::singleton);
    }

    private static List<StringConverterFactory> getStringConverters(
            Collection<? extends StringConverterAdapter> resolvers,
            Collection<? extends StringConverterFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                StringConverterFactory::singleton);
    }

    private static List<HttpParamResolverFactory> getParamResolvers(
            Collection<? extends HttpParamResolverAdapter> resolvers,
            Collection<? extends HttpParamResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                HttpParamResolverFactory::singleton);
    }

    private static List<ContextResolverFactory> getContextResolvers(
            Collection<? extends ContextResolverAdapter> resolvers,
            Collection<? extends ContextResolverFactory> factories) {
        return mergeResolvers(resolvers,
                factories,
                ContextResolverFactory::singleton);
    }

    private static List<HttpParamResolverAdviceFactory> getParamResolverAdvices(
            Collection<? extends HttpParamResolverAdviceAdapter> resolvers,
            Collection<? extends HttpParamResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                HttpParamResolverAdviceFactory::singleton);
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
    public HttpParamResolver getParamResolver(Param param) {
        //resolve the fixed parameter resolver
        return paramResolvers.stream().filter(r -> r.supports(param))
                .findFirst()
                .map(factory -> Checks.checkNotNull(factory.createResolver(param, getStringConverters(param),
                        rxSerializers),
                        "Failed to create ParamResolver for parameter: " + param))
                .orElse(null);
    }

    @Override
    public List<HttpParamResolverAdvice> getParamResolverAdvices(Param param, HttpParamResolver resolver) {
        if (paramResolverAdvices != null && !paramResolverAdvices.isEmpty()) {
            List<HttpParamResolverAdvice> advices =
                    paramResolverAdvices.stream()
                            .filter(advice -> advice.supports(param))
                            .map(factory -> Checks.checkNotNull(factory.createResolverAdvice(param, resolver),
                                    "Failed to create ParamResolverAdvice for parameter: " + param))
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
                        "Failed to create ContextResolver for parameter: " + param))
                .orElse(null);
    }

    @Override
    public List<RequestEntityResolver> getRequestEntityResolvers(Param param) {
        List<RequestEntityResolver> resolvers = new ArrayList<>();
        requestEntityResolvers.forEach(factory -> {
            if (factory.supports(param)) {
                resolvers.add(factory.createResolver(param, getStringConverters(param), rxSerializers));
            }
        });
        return Collections.unmodifiableList(resolvers);
    }

    @Override
    public List<RequestEntityResolverAdvice> getRequestEntityResolverAdvices(Param param) {
        if (requestEntityResolverAdvices != null && !requestEntityResolverAdvices.isEmpty()) {
            List<RequestEntityResolverAdvice> advices =
                    requestEntityResolverAdvices.stream()
                            .filter(advice -> advice.supports(param))
                            .map(factory -> Checks.checkNotNull(factory.createResolverAdvice(param),
                                    "Failed to create RequestEntityResolverAdvice for handler: "
                                            + param))
                            .collect(Collectors.toList());
            if (!advices.isEmpty()) {
                return Collections.unmodifiableList(advices);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<ResponseEntityResolver> getResponseEntityResolvers(HandlerMethod handlerMethod) {
        return responseResolversFactory.getResolvers(responseEntityResolvers, txSerializers(), handlerMethod);
    }

    @Override
    public List<ResponseEntityResolverAdvice> getResponseEntityResolverAdvices(HandlerMethod handlerMethod) {
        return responseResolversFactory.getResolverAdvices(responseEntityResolverAdvices, handlerMethod);
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

    private StringConverterProvider getStringConverters(Param param) {
        return key -> {
            Optional<StringConverter> converter;
            for (StringConverterFactory factory : stringConverters) {
                if ((converter = factory.createConverter(StringConverterFactory.Key
                        .of(key.genericType(), key.type(), param))).isPresent()) {
                    return converter.get();
                }
            }
            return null;
        };
    }

    public static HandlerConfiguration buildConfiguration(HandlerResolverFactory resolverFactory,
                                                          Attributes attributes) {
        if (resolverFactory instanceof HandlerResolverFactoryImpl) {
            HandlerResolverFactoryImpl factory = (HandlerResolverFactoryImpl) resolverFactory;
            return new HandlerConfiguration(attributes,
                    new LinkedList<>(factory.routeFilters),
                    new LinkedList<>(factory.stringConverters),
                    new LinkedList<>(factory.paramResolvers),
                    new LinkedList<>(factory.paramResolverAdvices),
                    new LinkedList<>(factory.contextResolvers),
                    new LinkedList<>(factory.requestEntityResolvers),
                    new LinkedList<>(factory.requestEntityResolverAdvices),
                    new LinkedList<>(factory.responseEntityResolvers),
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

    private static final class ResponseResolversFactory {

        private final AtomicReference<List<ResponseEntityResolver>> resolvers4MissingHandler
                = new AtomicReference<>();
        private final AtomicReference<List<ResponseEntityResolverAdvice>> advices4MissingHandler
                = new AtomicReference<>();

        private final ConcurrentHashMap<Method, List<ResponseEntityResolver>> resolversMap =
                new ConcurrentHashMap<>(8);
        private final ConcurrentHashMap<Method, List<ResponseEntityResolverAdvice>> advicesMap =
                new ConcurrentHashMap<>(8);

        private List<ResponseEntityResolver> getResolvers(List<ResponseEntityResolverFactory> factories,
                                                          List<HttpResponseSerializer> txSerializers,
                                                          HandlerMethod method) {
            if (method == null) {
                resolvers4MissingHandler.compareAndSet(null, buildResolvers(factories, txSerializers,
                        null));
                return resolvers4MissingHandler.get();
            }
            return resolversMap.computeIfAbsent(method.method(),
                    m -> buildResolvers(factories, txSerializers, method));
        }

        private List<ResponseEntityResolverAdvice> getResolverAdvices(List<ResponseEntityResolverAdviceFactory>
                                                                              factories,
                                                                      HandlerMethod method) {
            if (method == null) {
                advices4MissingHandler.compareAndSet(null, buildResolverAdvices(factories, null));
                return advices4MissingHandler.get();
            }
            return advicesMap.computeIfAbsent(method.method(), m -> buildResolverAdvices(factories, method));
        }

        private List<ResponseEntityResolver> buildResolvers(List<ResponseEntityResolverFactory> factories,
                                                            List<HttpResponseSerializer> txSerializers,
                                                            HandlerMethod method) {
            if (factories == null || factories.isEmpty()) {
                return Collections.emptyList();
            }
            final List<ResponseEntityResolver> resolvers = new LinkedList<>();
            final boolean missingHandler = (method == null);

            for (ResponseEntityResolverFactory factory : factories) {
                if ((missingHandler && factory.alsoApplyWhenMissingHandler())
                        || (!missingHandler && factory.supports(method))) {
                    resolvers.add(factory.createResolver(method, txSerializers));
                }
            }
            return resolvers;
        }

        private List<ResponseEntityResolverAdvice> buildResolverAdvices(List<ResponseEntityResolverAdviceFactory>
                                                                                factories,
                                                                        HandlerMethod method) {
            if (factories == null || factories.isEmpty()) {
                return Collections.emptyList();
            }
            final List<ResponseEntityResolverAdvice> advices = new LinkedList<>();
            final boolean missingHandler = (method == null);

            for (ResponseEntityResolverAdviceFactory factory : factories) {
                if ((missingHandler && factory.alsoApplyWhenMissingHandler())
                        || (!missingHandler && factory.supports(method))) {
                    advices.add(factory.createResolverAdvice(method));
                }
            }
            return advices;
        }
    }

}
