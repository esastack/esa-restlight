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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ProvidersFactoryImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class,
                () -> new ProvidersFactoryImpl(null, new ConfigurationImpl()));
        assertThrows(NullPointerException.class,
                () -> new ProvidersFactoryImpl(mock(DeployContext.class), null));
        assertDoesNotThrow(() -> new ProvidersFactoryImpl(mock(DeployContext.class), new ConfigurationImpl()));
    }

    @Test
    void testBasic() {
        final DeployContext context = mock(DeployContext.class);
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final ProvidersFactory factory = new ProvidersFactoryImpl(context, configuration);

        configurable.register(CompositeProvider1.class);
        final CompositeProvider2 provider2 = new CompositeProvider2();
        configurable.register(provider2);

        // messageBodyReaders
        List<ProxyComponent<MessageBodyReader<?>>> readers = new ArrayList<>(factory.messageBodyReaders());
        assertEquals(2, readers.size());
        assertEquals(CompositeProvider1.class, readers.get(0).underlying());
        assertSame(provider2, readers.get(1).underlying());

        // messageBodyWriters
        List<ProxyComponent<MessageBodyWriter<?>>> writers = new ArrayList<>(factory.messageBodyWriters());
        assertEquals(2, writers.size());
        assertEquals(CompositeProvider1.class, writers.get(0).underlying());
        assertSame(provider2, writers.get(1).underlying());

        // exceptionMappers
        List<ProxyComponent<ExceptionMapper<Throwable>>> exceptionMappers =
                new ArrayList<>(factory.exceptionMappers());
        assertEquals(2, exceptionMappers.size());
        assertEquals(CompositeProvider1.class, exceptionMappers.get(0).underlying());
        assertSame(provider2, exceptionMappers.get(1).underlying());

        // contextResolvers
        List<ProxyComponent<ContextResolver<?>>> contextResolvers = new ArrayList<>(factory.contextResolvers());
        assertEquals(2, contextResolvers.size());
        assertEquals(CompositeProvider1.class, contextResolvers.get(0).underlying());
        assertSame(provider2, contextResolvers.get(1).underlying());

        // features
        List<ProxyComponent<Feature>> features = new ArrayList<>(factory.features());
        assertEquals(2, features.size());
        assertEquals(CompositeProvider1.class, features.get(0).underlying());
        assertSame(provider2, features.get(1).underlying());

        // paramConverterProviders
        List<ProxyComponent<ParamConverterProvider>> providers = new ArrayList<>(factory.paramConverterProviders());
        assertEquals(2, providers.size());
        assertEquals(CompositeProvider1.class, providers.get(0).underlying());
        assertSame(provider2, providers.get(1).underlying());

        // dynamicFeatures
        List<ProxyComponent<DynamicFeature>> dynamicFeatures = new ArrayList<>(factory.dynamicFeatures());
        assertEquals(2, dynamicFeatures.size());
        assertEquals(CompositeProvider1.class, dynamicFeatures.get(0).underlying());
        assertSame(provider2, dynamicFeatures.get(1).underlying());

        // readerInterceptors
        List<ProxyComponent<ReaderInterceptor>> readerInterceptors = new ArrayList<>(factory.readerInterceptors());
        assertEquals(2, readerInterceptors.size());
        assertEquals(CompositeProvider1.class, readerInterceptors.get(0).underlying());
        assertSame(provider2, readerInterceptors.get(1).underlying());

        // writerInterceptors
        List<ProxyComponent<WriterInterceptor>> writerInterceptors = new ArrayList<>(factory.writerInterceptors());
        assertEquals(2, writerInterceptors.size());
        assertEquals(CompositeProvider1.class, writerInterceptors.get(0).underlying());
        assertSame(provider2, writerInterceptors.get(1).underlying());

        // requestFilters
        List<ProxyComponent<ContainerRequestFilter>> requestFilters = new ArrayList<>(factory.requestFilters());
        assertEquals(2, requestFilters.size());
        assertEquals(CompositeProvider1.class, requestFilters.get(0).underlying());
        assertSame(provider2, requestFilters.get(1).underlying());

        // responseFilters
        List<ProxyComponent<ContainerResponseFilter>> responseFilters = new ArrayList<>(factory.responseFilters());
        assertEquals(2, responseFilters.size());
        assertEquals(CompositeProvider1.class, responseFilters.get(0).underlying());
        assertSame(provider2, responseFilters.get(1).underlying());
    }

    private static class CompositeProvider1 implements MessageBodyReader<String>,
            MessageBodyWriter<String>, ExceptionMapper<RuntimeException>, ContextResolver<Object>,
            Feature, ParamConverterProvider, DynamicFeature, ReaderInterceptor, WriterInterceptor,
            ContainerRequestFilter, ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        }

        @Override
        public boolean configure(FeatureContext context) {
            return false;
        }

        @Override
        public Object getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public String readFrom(Class<String> type, Type genericType, Annotation[] annotations,
                               MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws WebApplicationException {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws WebApplicationException {

        }

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            return null;
        }

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    private static final class CompositeProvider2 extends CompositeProvider1 {

    }

}

