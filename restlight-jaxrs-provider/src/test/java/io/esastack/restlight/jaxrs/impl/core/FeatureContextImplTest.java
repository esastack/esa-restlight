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
package io.esastack.restlight.jaxrs.impl.core;

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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureContextImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new FeatureContextImpl(null,
                new ConfigurableImpl(new ConfigurationImpl())));
        assertThrows(NullPointerException.class, () -> new FeatureContextImpl(Object.class, null));
        assertDoesNotThrow(() -> new FeatureContextImpl(Object.class, new ConfigurableImpl(new ConfigurationImpl())));
    }

    @Test
    void testBasic() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final FeatureContextImpl context = new FeatureContextImpl(Object.class, configurable);
        assertSame(configuration, context.getConfiguration());
        assertSame(context, context.property("name", "value"));
        assertEquals("value", configuration.getProperty("name"));

        assertSame(context, context.register(DemoFeature.class));
        assertTrue(configuration.getProviderClasses().contains(DemoFeature.class));
        assertSame(context, context.register(RequestFilter1.class));
        assertTrue(configuration.getProviderClasses().contains(RequestFilter1.class));

        assertSame(context, context.register(ResponseFilter1.class, 100));
        assertTrue(configuration.getProviderClasses().contains(ResponseFilter1.class));

        assertSame(context, context.register(CompositeFeature1.class, ExceptionMapper.class,
                ContextResolver.class, Feature.class, DynamicFeature.class));
        assertTrue(configuration.getProviderClasses().contains(CompositeFeature1.class));

        final Map<Class<?>, Integer> contracts1 = new HashMap<>();
        contracts1.put(ExceptionMapper.class, 100);
        contracts1.put(ContextResolver.class, 200);
        contracts1.put(Feature.class, 300);
        contracts1.put(DynamicFeature.class, 400);
        assertSame(context, context.register(CompositeFeature1.class, contracts1));
        assertTrue(configuration.getProviderClasses().contains(CompositeFeature1.class));

        final ReaderInterceptor readerInterceptor = new ReaderInterceptor1();
        assertSame(context, context.register(readerInterceptor));
        assertTrue(configuration.getProviderInstances().contains(readerInterceptor));

        final WriterInterceptor writerInterceptor = new WriterInterceptor1();
        assertSame(context, context.register(writerInterceptor, 100));
        assertTrue(configuration.getProviderInstances().contains(writerInterceptor));

        final CompositeFeature2 feature2 = new CompositeFeature2();
        assertSame(context, context.register(feature2, ExceptionMapper.class, ContextResolver.class,
                Feature.class, DynamicFeature.class));
        assertTrue(configuration.getProviderInstances().contains(feature2));

        final Map<Class<?>, Integer> contracts3 = new HashMap<>(contracts1);
        contracts3.put(Providers.class, 500);
        final CompositeFeature3 feature3 = new CompositeFeature3();
        assertSame(context, context.register(feature3, contracts3));
        assertTrue(configuration.getProviderInstances().contains(feature3));
    }

    private static final class DemoFeature implements Feature {
        @Override
        public boolean configure(FeatureContext context) {
            return true;
        }
    }

    @Priority(100)
    private static final class RequestFilter1 implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    @Priority(130)
    private static final class ResponseFilter1 implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @Priority(150)
    private static final class CompositeFeature1 implements ExceptionMapper<Throwable>, ContextResolver<String>,
            Feature, DynamicFeature {

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        }

        @Override
        public boolean configure(FeatureContext context) {
            return false;
        }

        @Override
        public String getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(Throwable exception) {
            return null;
        }
    }

    @Priority(170)
    private static final class ReaderInterceptor1 implements ReaderInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }
    }

    @Priority(180)
    private static final class WriterInterceptor1 implements WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    @Priority(200)
    private static final class CompositeFeature2 implements ExceptionMapper<Throwable>, ContextResolver<String>,
            Feature, DynamicFeature {

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        }

        @Override
        public boolean configure(FeatureContext context) {
            return false;
        }

        @Override
        public String getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(Throwable exception) {
            return null;
        }
    }

    @Priority(210)
    private static final class CompositeFeature3 implements ExceptionMapper<Throwable>, ContextResolver<String>,
            Feature, DynamicFeature, Providers {

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        }

        @Override
        public boolean configure(FeatureContext context) {
            return false;
        }

        @Override
        public String getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(Throwable exception) {
            return null;
        }

        @Override
        public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
                                                             Type genericType,
                                                             Annotation[] annotations,
                                                             MediaType mediaType) {
            return null;
        }

        @Override
        public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
                                                             Type genericType,
                                                             Annotation[] annotations,
                                                             MediaType mediaType) {
            return null;
        }

        @Override
        public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
            return null;
        }

        @Override
        public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
                                                         MediaType mediaType) {
            return null;
        }
    }
}

