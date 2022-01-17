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
package io.esastack.restlight.jaxrs.configure;

import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.util.Collection;
import java.util.Map;

/**
 * This factory is designed to create proxy for {@link ConfigurationImpl#getProviderClasses()} and
 * {@link ConfigurationImpl#getProviderInstances()}. All the providers obtained from this interface are all
 * have been proxied to instantiate and inject fields and properties when the provider is firstly invoked.
 *
 * @see LazyInstantiateHandler
 * @see LazyInjectHandler
 */
public interface ProvidersFactory {

    /**
     * Obtains an immutable collection of {@link MessageBodyReader}s, which are proxied to instantiate and inject
     * fields and properties when the reader is firstly invoked.
     *
     * @return  immutable readers, which must not be {@code null}.
     */
    Collection<ProxyComponent<MessageBodyReader<?>>> messageBodyReaders();

    /**
     * Obtains an immutable collection of {@link MessageBodyWriter}s, which are proxied to instantiate and inject
     * fields and properties when the writer is firstly invoked.
     *
     * @return  immutable writers, which must not be {@code null}.
     */
    Collection<ProxyComponent<MessageBodyWriter<?>>> messageBodyWriters();

    /**
     * Obtains an immutable collection of {@link ExceptionMapper}s, which are proxied to instantiate and inject
     * fields and properties when the mapper is firstly invoked.
     *
     * @return  immutable mappers, which must not be {@code null}.
     */
    Map<Class<Throwable>, ProxyComponent<ExceptionMapper<Throwable>>> exceptionMappers();

    /**
     * Obtains an immutable collection of {@link ContextResolver}s, which are proxied to instantiate and inject
     * fields and properties when the resolver is firstly invoked.
     *
     * @return  immutable resolvers, which must not be {@code null}.
     */
    Map<Class<?>, ProxyComponent<ContextResolver<?>>> contextResolvers();

    /**
     * Obtains an immutable collection of {@link Feature}s, which are proxied to instantiate and inject
     * fields and properties when the feature is firstly invoked.
     *
     * @return  immutable features, which must not be {@code null}.
     */
    Collection<ProxyComponent<Feature>> features();

    /**
     * Obtains an immutable collection of {@link ParamConverterProvider}s, which are proxied to instantiate and inject
     * fields and properties when the provider is firstly invoked.
     *
     * @return  immutable providers, which must not be {@code null}.
     */
    Collection<ProxyComponent<ParamConverterProvider>> paramConverterProviders();

    /**
     * Obtains an immutable collection of {@link DynamicFeature}s, which are proxied to instantiate and inject
     * fields and properties when the dynamic feature is firstly invoked.
     *
     * @return  immutable features, which must not be {@code null}.
     */
    Collection<ProxyComponent<DynamicFeature>> dynamicFeatures();

    /**
     * Obtains an immutable collection of {@link ReaderInterceptor}s, which are proxied to instantiate and inject
     * fields and properties when the interceptor is firstly invoked.
     *
     * @return  immutable interceptors, which must not be {@code null}.
     */
    Collection<ProxyComponent<ReaderInterceptor>> readerInterceptors();

    /**
     * Obtains an immutable collection of {@link WriterInterceptor}s, which are proxied to instantiate and inject
     * fields and properties when the interceptor is firstly invoked.
     *
     * @return  immutable interceptors, which must not be {@code null}.
     */
    Collection<ProxyComponent<WriterInterceptor>> writerInterceptors();

    /**
     * Obtains an immutable collection of {@link ContainerRequestFilter}s, which are proxied to instantiate and inject
     * fields and properties when the filter is firstly invoked.
     *
     * @return  immutable filters, which must not be {@code null}.
     */
    Collection<ProxyComponent<ContainerRequestFilter>> requestFilters();

    /**
     * Obtains an immutable collection of {@link ContainerResponseFilter}s, which are proxied to instantiate and inject
     * fields and properties when the filter is firstly invoked.
     *
     * @return  immutable filters, which must not be {@code null}.
     */
    Collection<ProxyComponent<ContainerResponseFilter>> responseFilters();

}

