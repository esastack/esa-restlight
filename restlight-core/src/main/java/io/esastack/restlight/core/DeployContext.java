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
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.locate.HandlerValueResolverLocator;
import io.esastack.restlight.core.handler.locate.MappingLocator;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionMapper;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.server.ServerDeployContext;

import java.util.List;
import java.util.Optional;

/**
 * DeployContext is a container hat holds the contexts of a Restlight such as {@link AbstractRestlight}, {@link
 * Restlight}.
 * <p>
 * Some of the methods in this interface will return an {@link Optional} value which means this value maybe a {@code
 * null} or this value may be instantiate later.
 */
public interface DeployContext<O extends RestlightOptions> extends ServerDeployContext<O> {

    /**
     * Gets all the singleton controller beans. It should be instantiate before server is about to starting
     * and initializing.
     *
     * @return optional value
     */
    Optional<List<Object>> singletonControllers();

    /**
     * Obtains all prototype controller beans.
     *
     * @return prototype beans
     */
    Optional<List<Class<?>>> prototypeControllers();

    /**
     * Obtains all custom extensions.
     *
     * @return  extensions
     */
    Optional<List<Object>> extensions();

    /**
     * Gets all the advice beans. It should be instantiate before server is about to starting and initializing.
     *
     * @return optional value
     */
    Optional<List<Object>> advices();

    /**
     * Gets all the {@link InterceptorFactory}s. It should be instantiate before server is about to starting and
     * initializing.
     *
     * @return optional value
     */
    Optional<List<InterceptorFactory>> interceptors();

    /**
     * Gets all the {@link ExceptionMapper}s. It should be instantiate before server is about to starting and
     * initializing.
     *
     * @return optional value
     */
    Optional<List<ExceptionMapper>> exceptionMappers();

    /**
     * Gets the instance of {@link HandlerResolverFactory}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return optional value
     */
    Optional<HandlerResolverFactory> resolverFactory();

    /**
     * <p>Gets the instance of {@link HandlerAdvicesFactory}. It should be instantiate when server is about to
     * starting and initializing.</p>
     *
     * @return optional value
     */
    Optional<HandlerAdvicesFactory> handlerAdvicesFactory();

    /**
     * Gets the instance of {@link RouteMethodLocator}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return optional value
     */
    Optional<RouteMethodLocator> methodLocator();

    /**
     * Gets the instance of {@link MappingLocator}. It should be instantiate when server is about to starting and
     * initializing.
     *
     * @return optional value
     */
    Optional<MappingLocator> mappingLocator();

    /**
     * Gets the instance of {@link HandlerValueResolverLocator}. It should be instantiate when server is about to
     * starting and initializing.
     *
     * @return optional value
     */
    Optional<HandlerValueResolverLocator> handlerResolverLocator();

    /**
     * Gets the instance of {@link ResolvableParamPredicate}. It should be instantiate when server is about to
     * starting and initializing.
     *
     * @return  an optional instance of {@link ResolvableParamPredicate}.
     */
    Optional<ResolvableParamPredicate> paramPredicate();

    /**
     * Gets the instance of {@link ExceptionResolverFactory}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return optional value
     */
    Optional<ExceptionResolverFactory> exceptionResolverFactory();

    /**
     * Gets the {@link HandlerConfigure}s. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return optional value
     */
    Optional<List<HandlerConfigure>> handlerConfigures();

    /**
     * Obtains the {@link HandlerFactory}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return  an optional value of {@link HandlerFactory}.
     */
    Optional<HandlerFactory> handlerFactory();

    /**
     * Obtains the {@link HandlerContextProvider}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return  an optional value of {@link HandlerContextProvider}.
     */
    Optional<HandlerContextProvider> handlerContexts();

    /**
     * Gets the {@link Handlers}. It should be instantiate when server is about to starting
     * and initializing.
     *
     * @return  optional value
     */
    Optional<Handlers> handlers();

}
