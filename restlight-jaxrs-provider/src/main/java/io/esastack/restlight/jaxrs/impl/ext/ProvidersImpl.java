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
package io.esastack.restlight.jaxrs.impl.ext;

import esa.commons.Checks;
import io.esastack.restlight.jaxrs.configure.ProvidersProxyFactory;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProvidersImpl implements Providers {

    public final ProvidersProxyFactory providers;

    private Collection<MessageBodyReader<?>> messageBodyReaders;
    private Collection<MessageBodyWriter<?>> messageBodyWriters;
    private Map<Class<Throwable>, ExceptionMapper<Throwable>> exceptionMappers;
    private Map<Class<?>, ProducesComponent<ContextResolver<?>>> contextResolvers;

    public ProvidersImpl(ProvidersProxyFactory providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType,
                                                         Annotation[] annotations, MediaType mediaType) {
        if (messageBodyReaders == null) {
            messageBodyReaders = providers.messageBodyReaders().stream().map(ProxyComponent::proxied)
                    .collect(Collectors.toList());
        }
        if (messageBodyReaders.isEmpty()) {
            return null;
        }
        for (MessageBodyReader<?> reader : messageBodyReaders) {
            if (reader.isReadable(type, genericType, annotations, mediaType)) {
                return (MessageBodyReader<T>) reader;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType,
                                                         Annotation[] annotations, MediaType mediaType) {
        if (messageBodyWriters == null) {
            messageBodyWriters = providers.messageBodyWriters().stream().map(ProxyComponent::proxied)
                    .collect(Collectors.toList());
        }
        if (messageBodyWriters.isEmpty()) {
            return null;
        }
        for (MessageBodyWriter<?> writer : messageBodyWriters) {
            if (writer.isWriteable(type, genericType, annotations, mediaType)) {
                return (MessageBodyWriter<T>) writer;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        if (exceptionMappers == null) {
            Map<Class<Throwable>, ExceptionMapper<Throwable>> exMappers = new HashMap<>();
            for (Map.Entry<Class<Throwable>, ProxyComponent<ExceptionMapper<Throwable>>> entry :
                    providers.exceptionMappers().entrySet()) {
                exMappers.put(entry.getKey(), entry.getValue().proxied());
            }
            exceptionMappers = exMappers;
        }
        return (ExceptionMapper<T>) exceptionMappers.get(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        if (contextResolvers == null) {
            Map<Class<?>, ProducesComponent<ContextResolver<?>>> resolvers = new HashMap<>();
            for (Map.Entry<Class<?>, ProxyComponent<ContextResolver<?>>> entry :
                    providers.contextResolvers().entrySet()) {
                resolvers.put(entry.getKey(), new ProducesComponent<>(JaxrsUtils.produces(
                        entry.getValue().underlying()), entry.getValue().proxied()));
            }
            contextResolvers = resolvers;
        }
        ProducesComponent<ContextResolver<?>> contextResolver = contextResolvers.get(contextType);
        if (contextResolver == null) {
            return null;
        }
        if (contextResolver.mediaTypes().isEmpty()) {
            return (ContextResolver<T>) contextResolver;
        }
        for (MediaType type : contextResolver.mediaTypes()) {
            if (type.isCompatible(mediaType)) {
                return (ContextResolver<T>) contextResolver;
            }
        }
        return null;
    }

    private static class ProducesComponent<T> {

        private final List<MediaType> mediaTypes;
        private final T underlying;

        private ProducesComponent(List<MediaType> mediaTypes, T underlying) {
            this.mediaTypes = mediaTypes;
            this.underlying = underlying;
        }

        private T underlying() {
            return underlying;
        }

        private List<MediaType> mediaTypes() {
            return mediaTypes;
        }

    }

}

