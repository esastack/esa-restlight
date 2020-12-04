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
package esa.restlight.core;

import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.HandlerAdvicesFactory;
import esa.restlight.core.handler.locate.MappingLocator;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.interceptor.InterceptorFactory;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.exception.ExceptionMapper;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.server.ServerDeployContextImpl;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;

public class DeployContextImpl<O extends RestlightOptions>
        extends ServerDeployContextImpl<O>
        implements DeployContext<O> {

    private volatile List<ExceptionMapper> exceptionMappers;
    private volatile List<Object> controllers;
    private volatile List<Object> advices;
    private volatile List<InterceptorFactory> interceptors;
    private volatile HandlerResolverFactory resolverFactory;
    private volatile Validator validator;
    private volatile HandlerAdvicesFactory handlerAdvicesFactory;
    private volatile RouteHandlerLocator routeHandlerLocator;
    private volatile MappingLocator mappingLocator;
    private volatile ExceptionResolverFactory exceptionResolverFactory;

    protected DeployContextImpl(String name, O options) {
        super(name, options);
    }

    @Override
    public Optional<List<ExceptionMapper>> exceptionMappers() {
        return Optional.ofNullable(exceptionMappers);
    }

    @Override
    public Optional<List<Object>> controllers() {
        return Optional.ofNullable(controllers);
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
    public Optional<Validator> validator() {
        return Optional.ofNullable(validator);
    }

    @Override
    public Optional<HandlerAdvicesFactory> handlerAdvicesFactory() {
        return Optional.ofNullable(handlerAdvicesFactory);
    }

    @Override
    public Optional<RouteHandlerLocator> routeHandlerLocator() {
        return Optional.ofNullable(routeHandlerLocator);
    }

    @Override
    public Optional<MappingLocator> mappingLocator() {
        return Optional.ofNullable(mappingLocator);
    }

    @Override
    public Optional<ExceptionResolverFactory> exceptionResolverFactory() {
        return Optional.ofNullable(exceptionResolverFactory);
    }

    @Override
    public Optional<List<InterceptorFactory>> interceptors() {
        return Optional.ofNullable(interceptors);
    }

    void setControllers(List<Object> controllers) {
        this.controllers = controllers;
    }

    void setAdvices(List<Object> advices) {
        this.advices = advices;
    }

    void setExceptionMappers(List<ExceptionMapper> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
    }

    void setValidator(Validator validator) {
        this.validator = validator;
    }

    void setResolverFactory(HandlerResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    void setHandlerAdvicesFactory(HandlerAdvicesFactory handlerAdvicesFactory) {
        this.handlerAdvicesFactory = handlerAdvicesFactory;
    }

    void setRouteHandlerLocator(RouteHandlerLocator routeHandlerLocator) {
        this.routeHandlerLocator = routeHandlerLocator;
    }

    void setMappingLocator(MappingLocator mappingLocator) {
        this.mappingLocator = mappingLocator;
    }

    void setExceptionResolverFactory(ExceptionResolverFactory exceptionResolverFactory) {
        this.exceptionResolverFactory = exceptionResolverFactory;
    }

    void setInterceptors(List<InterceptorFactory> interceptors) {
        this.interceptors = interceptors;
    }
}
