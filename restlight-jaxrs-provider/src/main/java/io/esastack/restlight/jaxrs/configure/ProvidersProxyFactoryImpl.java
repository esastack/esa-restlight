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

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProvidersProxyFactoryImpl implements ProvidersProxyFactory {

    private final DeployContext<? extends RestlightOptions> context;
    private final ConfigurationImpl configuration;

    public ProvidersProxyFactoryImpl(DeployContext<? extends RestlightOptions> context,
                                     ConfigurationImpl configuration) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(configuration, "configuration");
        this.context = context;
        this.configuration = configuration;
    }

    @Override
    public Collection<ProxyComponent<MessageBodyReader<?>>> messageBodyReaders() {
        List<ProxyComponent<MessageBodyReader<?>>> readers = new LinkedList<>();
        getFromClasses(configuration.getProviderClasses(), MessageBodyReader.class).values()
                .forEach(component -> readers.add(new ProxyComponent<>(component.underlying(),
                        (MessageBodyReader<?>) component.proxied())));
        getFromInstances(configuration.getProviderInstances(), MessageBodyReader.class).values()
                .forEach(component -> readers.add(new ProxyComponent<>(component.underlying(),
                        (MessageBodyReader<?>) component.proxied())));
        return Collections.unmodifiableList(readers);
    }

    @Override
    public Collection<ProxyComponent<MessageBodyWriter<?>>> messageBodyWriters() {
        List<ProxyComponent<MessageBodyWriter<?>>> writers = new LinkedList<>();
        getFromClasses(configuration.getProviderClasses(), MessageBodyWriter.class).values()
                .forEach(component -> writers.add(new ProxyComponent<>(component.underlying(),
                        (MessageBodyWriter<?>) component.proxied())));
        getFromInstances(configuration.getProviderInstances(), MessageBodyWriter.class).values()
                .forEach(component -> writers.add(new ProxyComponent<>(component.underlying(),
                        (MessageBodyWriter<?>) component.proxied())));
        return Collections.unmodifiableList(writers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Class<Throwable>, ProxyComponent<ExceptionMapper<Throwable>>> exceptionMappers() {
        Map<Class<Throwable>, ProxyComponent<ExceptionMapper<Throwable>>> mappers = new HashMap<>();
        getFromClasses(configuration.getProviderClasses(), ExceptionMapper.class).forEach((clazz, instance) ->
                mappers.put((Class<Throwable>) ClassUtils.findFirstGenericType(clazz).orElse(Throwable.class),
                        new ProxyComponent<>(instance.underlying(),
                                (ExceptionMapper<Throwable>) instance.proxied())));
        getFromInstances(configuration.getProviderInstances(), ExceptionMapper.class).forEach((clazz, instance) ->
                mappers.put((Class<Throwable>) ClassUtils.findFirstGenericType(clazz).orElse(Throwable.class),
                        new ProxyComponent<>(instance.underlying(),
                                (ExceptionMapper<Throwable>) instance.proxied())));
        return Collections.unmodifiableMap(mappers);
    }

    @Override
    public Map<Class<?>, ProxyComponent<ContextResolver<?>>> contextResolvers() {
        Map<Class<?>, ProxyComponent<ContextResolver<?>>> resolvers = new HashMap<>();
        getFromClasses(configuration.getProviderClasses(), ContextResolver.class).forEach((clazz, instance) ->
                resolvers.put(ClassUtils.getRawType(clazz), new ProxyComponent<>(instance.underlying(),
                        (ContextResolver<?>) instance.proxied())));
        getFromInstances(configuration.getProviderInstances(), ContextResolver.class).forEach((clazz, instance) ->
                resolvers.put(ClassUtils.getRawType(clazz), new ProxyComponent<>(instance.underlying(),
                        (ContextResolver<?>) instance.proxied())));
        return Collections.unmodifiableMap(resolvers);
    }

    @Override
    public Collection<ProxyComponent<Feature>> features() {
        List<ProxyComponent<Feature>> features = new LinkedList<>();
        features.addAll(getFromClasses(configuration.getProviderClasses(), Feature.class).values());
        features.addAll(getFromInstances(configuration.getProviderInstances(), Feature.class).values());
        return Collections.unmodifiableList(features);
    }

    @Override
    public Collection<ProxyComponent<ParamConverterProvider>> paramConverterProviders() {
        List<ProxyComponent<ParamConverterProvider>> providers = new LinkedList<>();
        providers.addAll(getFromClasses(configuration.getProviderClasses(), ParamConverterProvider.class).values());
        providers.addAll(getFromInstances(configuration.getProviderInstances(), ParamConverterProvider.class).values());
        return Collections.unmodifiableList(providers);
    }

    @Override
    public Collection<ProxyComponent<DynamicFeature>> dynamicFeatures() {
        List<ProxyComponent<DynamicFeature>> dynamicFeatures = new LinkedList<>();
        dynamicFeatures.addAll(getFromClasses(configuration.getProviderClasses(), DynamicFeature.class).values());
        dynamicFeatures.addAll(getFromInstances(configuration.getProviderInstances(), DynamicFeature.class).values());
        return Collections.unmodifiableList(dynamicFeatures);
    }

    @Override
    public Collection<ProxyComponent<ReaderInterceptor>> readerInterceptors() {
        List<ProxyComponent<ReaderInterceptor>> interceptors = new LinkedList<>();
        interceptors.addAll(getFromClasses(configuration.getProviderClasses(), ReaderInterceptor.class).values());
        interceptors.addAll(getFromInstances(configuration.getProviderInstances(), ReaderInterceptor.class).values());
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public Collection<ProxyComponent<WriterInterceptor>> writerInterceptors() {
        List<ProxyComponent<WriterInterceptor>> interceptors = new LinkedList<>();
        interceptors.addAll(getFromClasses(configuration.getProviderClasses(), WriterInterceptor.class).values());
        interceptors.addAll(getFromInstances(configuration.getProviderInstances(), WriterInterceptor.class).values());
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public Collection<ProxyComponent<ContainerRequestFilter>> requestFilters() {
        List<ProxyComponent<ContainerRequestFilter>> filters = new LinkedList<>();
        filters.addAll(getFromClasses(configuration.getProviderClasses(), ContainerRequestFilter.class).values());
        filters.addAll(getFromInstances(configuration.getProviderInstances(), ContainerRequestFilter.class).values());
        return Collections.unmodifiableList(filters);
    }

    @Override
    public Collection<ProxyComponent<ContainerResponseFilter>> responseFilters() {
        List<ProxyComponent<ContainerResponseFilter>> filters = new LinkedList<>();
        filters.addAll(getFromClasses(configuration.getProviderClasses(), ContainerResponseFilter.class).values());
        filters.addAll(getFromInstances(configuration.getProviderInstances(), ContainerResponseFilter.class).values());
        return Collections.unmodifiableList(filters);
    }

    @SuppressWarnings("unchecked")
    protected <T> Map<Class<?>, ProxyComponent<T>> getFromClasses(Collection<Class<?>> classes, Class<T> target) {
        Map<Class<?>, ProxyComponent<T>> values = new HashMap<>();
        for (Class<?> clazz : classes) {
            if (target.isAssignableFrom(clazz)) {
                values.put(clazz, new ProxyComponent<>(clazz, (T) ExtensionHandlerProxy.newProxy(target,
                        new LazyInstantiateHandler(clazz, context))));
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    protected <T> Map<Class<?>, ProxyComponent<T>> getFromInstances(Collection<Object> instances, Class<T> target) {
        Map<Class<?>, ProxyComponent<T>> values = new HashMap<>();
        for (Object instance : instances) {
            Class<?> userType = ClassUtils.getUserType(instance);
            if (target.isAssignableFrom(userType)) {
                values.put(userType, new ProxyComponent<>(instance, (T) ExtensionHandlerProxy.newProxy(target,
                        new LazyInjectHandler(instance, context))));
            }
        }
        return values;
    }
}

