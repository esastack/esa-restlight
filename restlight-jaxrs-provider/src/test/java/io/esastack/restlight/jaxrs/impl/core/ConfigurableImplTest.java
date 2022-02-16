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
import static org.mockito.Mockito.mock;

class ConfigurableImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new ConfigurableImpl(null));
        assertDoesNotThrow(() -> new ConfigurableImpl(mock(ConfigurationImpl.class)));
    }

    @Test
    void testBasic() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        assertTrue(configuration.getProperties().isEmpty());
        configurable.property("abc", "xyz");
        assertEquals("xyz", configuration.getProperty("abc"));
        configurable.property("abc", null);
        assertTrue(configuration.getProperties().isEmpty());

        assertSame(configuration, configurable.getConfiguration());
    }

    @Test
    void testRegister() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);

        configurable.register(new RequestFilter1());
        assertEquals(1, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts1 = configuration.getContracts(RequestFilter1.class);
        assertEquals(1, contracts1.size());
        assertEquals(100, contracts1.get(ContainerRequestFilter.class));

        configurable.register(new ResponseFilter1(), 100);
        assertEquals(2, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts2 = configuration.getContracts(ResponseFilter1.class);
        assertEquals(1, contracts2.size());
        assertEquals(100, contracts2.get(ContainerResponseFilter.class));

        configurable.register(new CompositeFeature1(), new Class[]{ContainerRequestFilter.class,
                ContainerResponseFilter.class, ExceptionMapper.class, ContextResolver.class, Feature.class});
        assertEquals(3, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts3 = configuration.getContracts(CompositeFeature1.class);
        assertEquals(3, contracts3.size());
        assertEquals(150, contracts3.get(ExceptionMapper.class));
        assertEquals(150, contracts3.get(ContextResolver.class));
        assertEquals(150, contracts3.get(Feature.class));


        configurable.register(ReaderInterceptor1.class);
        assertEquals(3, configuration.getProviderInstances().size());
        assertEquals(1, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts4 = configuration.getContracts(ReaderInterceptor1.class);
        assertEquals(1, contracts4.size());
        assertEquals(170, contracts4.get(ReaderInterceptor.class));

        configurable.register(WriterInterceptor1.class);
        assertEquals(3, configuration.getProviderInstances().size());
        assertEquals(2, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts5 = configuration.getContracts(WriterInterceptor1.class);
        assertEquals(1, contracts5.size());
        assertEquals(180, contracts5.get(WriterInterceptor.class));

        final Map<Class<?>, Integer> contracts00 = new HashMap<>(4);
        contracts00.put(ReaderInterceptor.class, 100);
        contracts00.put(WriterInterceptor.class, 200);
        contracts00.put(ExceptionMapper.class, 300);
        contracts00.put(DynamicFeature.class, 400);
        configurable.register(CompositeFeature2.class, contracts00);
        assertEquals(3, configuration.getProviderInstances().size());
        assertEquals(3, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts6 = configuration.getContracts(CompositeFeature2.class);
        assertEquals(2, contracts6.size());
        assertEquals(300, contracts6.get(ExceptionMapper.class));
        assertEquals(400, contracts6.get(DynamicFeature.class));
    }

    @Test
    void testUnRegistrable() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        configurable.register(new Object());
        assertTrue(configuration.getProviderInstances().isEmpty());
        assertTrue(configuration.getProviderClasses().isEmpty());

        final Map<Class<?>, Integer> contracts = new HashMap<>(4);
        contracts.put(Feature.class, 200);
        contracts.put(DynamicFeature.class, 300);
        contracts.put(ReaderInterceptor.class, 400);
        contracts.put(WriterInterceptor.class, 500);
        configurable.register(CompositeFeature3.class, contracts);

        Map<Class<?>, Integer> contracts1 = configuration.getContracts(CompositeFeature3.class);
        assertTrue(configuration.getProviderInstances().isEmpty());
        assertEquals(1, configuration.getProviderClasses().size());
        assertEquals(2, contracts1.size());
        assertEquals(200, contracts1.get(Feature.class));
        assertEquals(300, contracts1.get(DynamicFeature.class));
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

