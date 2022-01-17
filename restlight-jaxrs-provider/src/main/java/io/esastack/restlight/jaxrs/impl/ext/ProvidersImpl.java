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
import esa.commons.ClassUtils;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionMapper;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.jaxrs.adapter.JaxrsExceptionMapperAdapter;
import io.esastack.restlight.jaxrs.configure.ProvidersFactory;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ProvidersImpl implements Providers {

    public final ProvidersFactory providers;

    private final AtomicReference<ConsumesComponent<MessageBodyReader<?>>[]> messageBodyReaders
            = new AtomicReference<>();
    private final AtomicReference<ProducesComponent<MessageBodyWriter<?>>[]> messageBodyWriters
            = new AtomicReference<>();

    private final AtomicReference<io.esastack.restlight.core.resolver.exception.ExceptionMapper>
            exceptionMappers = new AtomicReference<>();
    private final AtomicReference<ProducesComponent<ContextResolver<?>>[]> contextResolvers
            = new AtomicReference<>();

    public ProvidersImpl(ProvidersFactory providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType,
                                                         Annotation[] annotations, MediaType mediaType) {
        ConsumesComponent<MessageBodyReader<?>>[] readers = messageBodyReaders.updateAndGet(pre -> {
            if (pre != null) {
                return pre;
            }

            Collection<ProxyComponent<MessageBodyReader<?>>> readers0 = providers.messageBodyReaders();
            ConsumesComponent<MessageBodyReader<?>>[] current = new ConsumesComponent[readers0.size()];
            int i = 0;
            for (ProxyComponent<MessageBodyReader<?>> reader : readers0) {
                current[i++] = new ConsumesComponent(reader.proxied(),
                        ClassUtils.findFirstGenericType(reader.underlying().getClass()).orElse(Object.class),
                        JaxrsUtils.getOrder(reader.underlying()),
                        JaxrsUtils.consumes(reader.underlying()).toArray(new MediaType[0]));
            }
            return current;
        });

        if (readers.length == 0) {
            return null;
        }

        // follow specification, default to application/octet-stream.
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }

        OrderedComparator.sort(readers);
        List<MessageBodyReader<?>> filtered = new LinkedList<>();
        for (ConsumesComponent<MessageBodyReader<?>> reader : readers) {
            if (reader.type.isAssignableFrom(type) && isCompatible(mediaType, reader.consumes)
                    && reader.underlying.isReadable(type, genericType, annotations, mediaType)) {
                filtered.add(reader.underlying);
            }
        }

        if (filtered.isEmpty()) {
            return null;
        }
        return (MessageBodyReader<T>) filtered.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType,
                                                         Annotation[] annotations, MediaType mediaType) {
        ProducesComponent<MessageBodyWriter<?>>[] writers = messageBodyWriters.updateAndGet(pre -> {
            if (pre != null) {
                return pre;
            }

            Collection<ProxyComponent<MessageBodyWriter<?>>> writers0 = providers.messageBodyWriters();
            ProducesComponent<MessageBodyWriter<?>>[] current = new ProducesComponent[writers0.size()];
            int i = 0;
            for (ProxyComponent<MessageBodyWriter<?>> reader : writers0) {
                current[i++] = new ProducesComponent(reader.proxied(),
                        ClassUtils.findFirstGenericType(reader.underlying().getClass()).orElse(Object.class),
                        JaxrsUtils.getOrder(reader.underlying()),
                        JaxrsUtils.produces(reader.underlying()).toArray(new MediaType[0]));
            }
            return current;
        });

        if (writers.length == 0) {
            return null;
        }

        OrderedComparator.sort(writers);
        List<MessageBodyWriter<?>> filtered = new LinkedList<>();
        for (ProducesComponent<MessageBodyWriter<?>> writer : writers) {
            if (writer.type.isAssignableFrom(type) && isCompatible(mediaType, writer.produces)
                    && writer.underlying.isWriteable(type, genericType, annotations, mediaType)) {
                filtered.add(writer.underlying);
            }
        }

        if (filtered.isEmpty()) {
            return null;
        }
        return (MessageBodyWriter<T>) filtered.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        io.esastack.restlight.core.resolver.exception.ExceptionMapper mapper = exceptionMappers.updateAndGet(pre -> {
            if (pre != null) {
                return pre;
            }
            Map<Class<? extends Throwable>, JaxrsExceptionMapperAdapter<Throwable>> mappings = new HashMap<>();
            for (Map.Entry<Class<Throwable>, ProxyComponent<ExceptionMapper<Throwable>>> entry :
                    providers.exceptionMappers().entrySet()) {
                mappings.put(entry.getKey(), new JaxrsExceptionMapperAdapter<>(entry.getValue().proxied()));
            }
            return new DefaultExceptionMapper(mappings);
        });

        return (ExceptionMapper<T>) ((JaxrsExceptionMapperAdapter<?>) mapper.mapTo(type)).underlying();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        ProducesComponent<ContextResolver<?>>[] resolvers = contextResolvers.updateAndGet(pre -> {
            if (pre != null) {
                return pre;
            }

            Map<Class<?>, ProxyComponent<ContextResolver<?>>> resolvers0 = providers.contextResolvers();
            ProducesComponent<ContextResolver<?>>[] current = new ProducesComponent[resolvers0.size()];
            int i = 0;
            for (Map.Entry<Class<?>, ProxyComponent<ContextResolver<?>>> resolver : resolvers0.entrySet()) {
                current[i++] = new ProducesComponent(resolver.getValue().proxied(),
                        resolver.getKey(),
                        JaxrsUtils.getOrder(resolver.getValue().underlying()),
                        JaxrsUtils.produces(resolver.getValue().underlying()).toArray(new MediaType[0]));
            }
            return current;
        });

        if (resolvers.length == 0) {
            return null;
        }
        List<ContextResolver<?>> filtered = new LinkedList<>();
        for (ProducesComponent<ContextResolver<?>> resolver : resolvers) {
            if (resolver.type.isAssignableFrom(contextType) && isCompatible(mediaType, resolver.produces)) {
                filtered.add(resolver.underlying);
            }
        }

        if (filtered.isEmpty()) {
            return null;
        }
        return (ContextResolver<T>) filtered.get(0);
    }

    private boolean isCompatible(MediaType current, MediaType[] target) {
        if (target.length == 0) {
            return true;
        }
        for (MediaType type : target) {
            if (type.isCompatible(current)) {
                return true;
            }
        }
        return false;
    }

    private static class GenericComponent<T> implements Ordered {

        final T underlying;
        final Class<?> type;
        private final int order;

        private GenericComponent(T underlying, Class<?> type, int order) {
            this.underlying = underlying;
            this.type = type;
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class ProducesComponent<T> extends GenericComponent<T> {

        private final MediaType[] produces;

        private ProducesComponent(T underlying, Class<?> type, int order, MediaType[] produces) {
            super(underlying, type, order);
            this.produces = produces;
        }
    }

    private static class ConsumesComponent<T> extends GenericComponent<T> {

        private final MediaType[] consumes;

        private ConsumesComponent(T underlying, Class<?> type, int order, MediaType[] consumes) {
            super(underlying, type, order);
            this.consumes = consumes;
        }
    }

}

