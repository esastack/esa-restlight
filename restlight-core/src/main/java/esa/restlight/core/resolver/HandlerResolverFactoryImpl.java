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
package esa.restlight.core.resolver;

import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.Param;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.util.OrderedComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HandlerResolverFactoryImpl implements HandlerResolverFactory {
    /**
     * serializers in current context
     */
    private final List<HttpRequestSerializer> rxSerializers;
    private final List<HttpResponseSerializer> txSerializers;

    private final List<ArgumentResolverFactory> argResolvers;
    private final List<ArgumentResolverAdviceFactory> argResolverAdvices;

    /**
     * Customize retValResolvers
     */
    private final List<ReturnValueResolverFactory> retValResolvers;
    private final List<ReturnValueResolverAdviceFactory> retValResolverAdvices;

    public HandlerResolverFactoryImpl(Collection<? extends HttpRequestSerializer> rxSerializers,
                                      Collection<? extends HttpResponseSerializer> txSerializers,
                                      Collection<? extends ArgumentResolverAdapter> argResolvers,
                                      Collection<? extends ArgumentResolverFactory> argResolverFactories,
                                      Collection<? extends ArgumentResolverAdviceAdapter> argResolverAdvices,
                                      Collection<? extends ArgumentResolverAdviceFactory> argResolverAdviceFactories,
                                      Collection<? extends ReturnValueResolverAdapter> retValResolvers,
                                      Collection<? extends ReturnValueResolverFactory> retValResolverFactories,
                                      Collection<? extends ReturnValueResolverAdviceAdapter> retValResolverAdvices,
                                      Collection<?
                                              extends ReturnValueResolverAdviceFactory> retValResolverAdviceFactories) {

        if (rxSerializers == null || rxSerializers.isEmpty()) {
            throw new IllegalArgumentException("Request serializer must not be null or empty!");
        }

        if (txSerializers == null || txSerializers.isEmpty()) {
            throw new IllegalArgumentException("Request Serializer must not be or empty!");
        }
        this.rxSerializers = sortForUnmodifiableList(rxSerializers);
        this.txSerializers = sortForUnmodifiableList(txSerializers);

        this.argResolvers = getArgResolvers(argResolvers, argResolverFactories);
        this.argResolverAdvices = getArgResolverAdvices(argResolverAdvices,
                argResolverAdviceFactories);

        this.retValResolvers = getRetValResolvers(retValResolvers, retValResolverFactories);
        this.retValResolverAdvices = getRetValResolverAdvices(retValResolverAdvices,
                retValResolverAdviceFactories);
    }

    private static List<ReturnValueResolverFactory> getRetValResolvers(
            Collection<? extends ReturnValueResolverAdapter> resolvers,
            Collection<? extends ReturnValueResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ReturnValueResolverFactory::singleton);
    }

    private static List<ReturnValueResolverAdviceFactory> getRetValResolverAdvices(
            Collection<? extends ReturnValueResolverAdviceAdapter> resolvers,
            Collection<? extends ReturnValueResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ReturnValueResolverAdviceFactory::singleton);
    }

    private static List<ArgumentResolverFactory> getArgResolvers(
            Collection<? extends ArgumentResolverAdapter> resolvers,
            Collection<? extends ArgumentResolverFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ArgumentResolverFactory::singleton);
    }

    private static List<ArgumentResolverAdviceFactory> getArgResolverAdvices(
            Collection<? extends ArgumentResolverAdviceAdapter> resolvers,
            Collection<? extends ArgumentResolverAdviceFactory> factories) {

        return mergeResolvers(resolvers,
                factories,
                ArgumentResolverAdviceFactory::singleton);
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

    private static <T> List<T> sortForUnmodifiableList(Collection<? extends T> rxSerializers) {
        List<T> tmpSerializers = new ArrayList<>(rxSerializers);
        //sort
        OrderedComparator.sort(tmpSerializers);
        return Collections.unmodifiableList(tmpSerializers);
    }

    @Override
    public ArgumentResolver getArgumentResolver(Param param) {
        //resolve the fixed parameter resolver
        final ArgumentResolver resolver = argResolvers.stream()
                .filter(r -> r.supports(param))
                .findFirst()
                .map(factory -> Objects.requireNonNull(factory.createResolver(param, rxSerializers),
                        "Failed to create argument resolver for parameter: " + param))
                .orElse(null);

        if (resolver != null && argResolverAdvices != null && !argResolverAdvices.isEmpty()) {
            List<ArgumentResolverAdvice> advices =
                    argResolverAdvices.stream()
                            .filter(advice -> advice.supports(param))
                            .map(factory -> Objects.requireNonNull(factory.createResolverAdvice(param, resolver),
                                    "Failed to create argument resolver advice for parameter: " + param))
                            .collect(Collectors.toList());

            if (!advices.isEmpty()) {
                return new AdvisedArgumentResolver(resolver, advices);
            }
        }
        return resolver;
    }

    @Override
    public ReturnValueResolver getReturnValueResolver(InvocableMethod handlerMethod) {
        //resolve the fixed parameter resolver
        final ReturnValueResolver resolver = retValResolvers.stream()
                .filter(r -> r.supports(handlerMethod))
                .findFirst().map(factory -> Objects.requireNonNull(factory.createResolver(handlerMethod,
                        txSerializers), "Failed to create return value resolver for handler: " + handlerMethod))
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve handler: " +
                        handlerMethod.method().getDeclaringClass().getName()
                        + "." + handlerMethod.method().getName() + "'s return value, " +
                        "Maybe a @ResponseBody has missed?"));

        if (retValResolverAdvices != null && !retValResolverAdvices.isEmpty()) {
            List<ReturnValueResolverAdvice> advices =
                    retValResolverAdvices.stream()
                            .filter(advice -> advice.supports(handlerMethod))
                            .map(factory -> Objects.requireNonNull(factory.createResolverAdvice(handlerMethod,
                                    resolver),
                                    "Failed to create return value resolver advice for handler: "
                                            + handlerMethod))
                            .collect(Collectors.toList());
            return advices.isEmpty() ? resolver : new AdvisedReturnValueResolver(resolver, advices);
        }
        return resolver;
    }

    @Override
    public List<ArgumentResolverFactory> argumentResolvers() {
        return argResolvers;
    }

    @Override
    public List<ReturnValueResolverFactory> returnValueResolvers() {
        return retValResolvers;
    }

    @Override
    public List<HttpRequestSerializer> rxSerializers() {
        return rxSerializers;
    }

    @Override
    public List<HttpResponseSerializer> txSerializers() {
        return txSerializers;
    }
}
