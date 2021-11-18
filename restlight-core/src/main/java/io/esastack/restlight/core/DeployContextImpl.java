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
package io.esastack.restlight.core;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.configure.HandlerRegistry;
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.locate.HandlerValueResolverLocator;
import io.esastack.restlight.core.handler.locate.MappingLocator;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionMapper;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.server.ServerDeployContextImpl;

import java.util.List;
import java.util.Optional;

public class DeployContextImpl<O extends RestlightOptions> extends ServerDeployContextImpl<O>
        implements DeployContext<O> {

    private volatile ResolvableParamPredicate paramPredicate;
    private volatile List<ExceptionMapper> exceptionMappers;
    private volatile List<Object> singletonControllers;
    private volatile List<Class<?>> prototypeControllers;
    private volatile List<Object> extensions;
    private volatile List<Object> advices;
    private volatile List<InterceptorFactory> interceptors;
    private volatile List<HandlerConfigure> handlerConfigures;
    private volatile HandlerResolverFactory resolverFactory;
    private volatile HandlerAdvicesFactory handlerAdvicesFactory;
    private volatile RouteMethodLocator routeMethodLocator;
    private volatile MappingLocator mappingLocator;
    private volatile HandlerValueResolverLocator handlerResolverLocator;
    private volatile ExceptionResolverFactory exceptionResolverFactory;
    private volatile HandlerRegistry handlerRegistry;
    private volatile Handlers handlers;

    protected DeployContextImpl(String name, O options) {
        super(name, options);
    }

    @Override
    public Optional<List<ExceptionMapper>> exceptionMappers() {
        return Optional.ofNullable(exceptionMappers);
    }

    @Override
    public Optional<List<Object>> singletonControllers() {
        return Optional.ofNullable(singletonControllers);
    }

    @Override
    public Optional<List<Class<?>>> prototypeControllers() {
        return Optional.ofNullable(prototypeControllers);
    }

    @Override
    public Optional<List<Object>> extensions() {
        return Optional.ofNullable(extensions);
    }

    @Override
    public Optional<List<Object>> advices() {
        return Optional.ofNullable(advices);
    }

    @Override
    public Optional<HandlerResolverFactory> resolverFactory() {
        return Optional.ofNullable(resolverFactory);
    }

    @Override
    public Optional<HandlerAdvicesFactory> handlerAdvicesFactory() {
        return Optional.ofNullable(handlerAdvicesFactory);
    }

    @Override
    public Optional<RouteMethodLocator> methodLocator() {
        return Optional.ofNullable(routeMethodLocator);
    }

    @Override
    public Optional<MappingLocator> mappingLocator() {
        return Optional.ofNullable(mappingLocator);
    }

    @Override
    public Optional<HandlerValueResolverLocator> handlerResolverLocator() {
        return Optional.ofNullable(handlerResolverLocator);
    }

    @Override
    public Optional<ExceptionResolverFactory> exceptionResolverFactory() {
        return Optional.ofNullable(exceptionResolverFactory);
    }

    @Override
    public Optional<List<InterceptorFactory>> interceptors() {
        return Optional.ofNullable(interceptors);
    }

    @Override
    public Optional<List<HandlerConfigure>> handlerConfigures() {
        return Optional.ofNullable(handlerConfigures);
    }

    @Override
    public Optional<ResolvableParamPredicate> paramPredicate() {
        return Optional.ofNullable(paramPredicate);
    }

    @Override
    public Optional<HandlerRegistry> handlerRegistry() {
        return Optional.ofNullable(handlerRegistry);
    }

    @Override
    public Optional<Handlers> handlers() {
        return Optional.ofNullable(handlers);
    }

    void setSingletonControllers(List<Object> controllers) {
        this.singletonControllers = controllers;
    }

    void setPrototypeControllers(List<Class<?>> controllers) {
        this.prototypeControllers = controllers;
    }

    void setExtensions(List<Object> extensions) {
        this.extensions = extensions;
    }

    void setAdvices(List<Object> advices) {
        this.advices = advices;
    }

    void setExceptionMappers(List<ExceptionMapper> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
    }

    void setResolverFactory(HandlerResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    void setHandlerAdvicesFactory(HandlerAdvicesFactory handlerAdvicesFactory) {
        this.handlerAdvicesFactory = handlerAdvicesFactory;
    }

    void setRouteMethodLocator(RouteMethodLocator routeMethodLocator) {
        this.routeMethodLocator = routeMethodLocator;
    }

    void setMappingLocator(MappingLocator mappingLocator) {
        this.mappingLocator = mappingLocator;
    }

    void setHandlerResolverLocator(HandlerValueResolverLocator handlerResolverLocator) {
        this.handlerResolverLocator = handlerResolverLocator;
    }

    void setExceptionResolverFactory(ExceptionResolverFactory exceptionResolverFactory) {
        this.exceptionResolverFactory = exceptionResolverFactory;
    }

    void setInterceptors(List<InterceptorFactory> interceptors) {
        this.interceptors = interceptors;
    }

    void setHandlerConfigure(List<HandlerConfigure> handlerConfigures) {
        this.handlerConfigures = handlerConfigures;
    }

    void setParamPredicate(ResolvableParamPredicate paramPredicate) {
        this.paramPredicate = paramPredicate;
    }

    void setHandlerRegistry(HandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    void setHandlers(Handlers handlers) {
        this.handlers = handlers;
    }
}
