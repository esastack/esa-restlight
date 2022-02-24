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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.RuntimeType;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationImplTest {

    @Test
    void testConstructor() {
        final HelloWorld2 h2 = new HelloWorld2();
        final ResponseFilter1 filter1 = new ResponseFilter1();
        final Feature feature = new DemoFeature();

        final ConfigurationImpl configuration = new ConfigurationImpl();
        configuration.setProperty("name", "LiMing");
        configuration.addProviderClass(RequestFilter1.class, Collections
                .singletonMap(ContainerRequestFilter.class, 200));
        configuration.addProviderInstance(filter1, Collections
                .singletonMap(ContainerResponseFilter.class, 300));
        configuration.addResourceClass(HelloWorld1.class);
        configuration.addResourceInstance(h2);
        configuration.addEnabledFeature(feature);

        final ConfigurationImpl copied = new ConfigurationImpl(configuration);
        assertTrue(copied.isEnabled(feature));
        assertTrue(copied.isRegistered(ResponseFilter1.class));
        assertTrue(copied.isRegistered(filter1));
        assertTrue(copied.getClasses().contains(HelloWorld1.class));
        assertTrue(copied.getInstances().contains(h2));
        assertEquals("LiMing", copied.getProperty("name"));

        assertEquals(configuration.getProperties().size(), copied.getProperties().size());
        assertEquals(configuration.getClasses().size(), configuration.getClasses().size());
        assertEquals(configuration.getProviderClasses().size(), configuration.getProviderClasses().size());

        configuration.setProperty("age", "22");
        configuration.addProviderClass(CompositeFeature1.class, Collections.singletonMap(Feature.class, 200));

        assertNotEquals(configuration.getProperties().size(), copied.getProperties().size());
        assertNotEquals(configuration.getProviderClasses().size(), copied.getProviderClasses().size());
    }

    @Test
    void testBasic() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        assertEquals(RuntimeType.SERVER, configuration.getRuntimeType());
        configuration.setProperty("name", "LiMing");
        configuration.setProperty("age", "20");
        assertEquals(2, configuration.getProperties().size());
        assertThrows(UnsupportedOperationException.class, () -> configuration.getProperties().put("xx", "yy"));
        assertEquals("LiMing", configuration.getProperty("name"));
        assertEquals("20", configuration.getProperty("age"));
        assertEquals(2, configuration.getPropertyNames().size());
        assertTrue(configuration.getPropertyNames().contains("name"));
        assertTrue(configuration.getPropertyNames().contains("age"));

        configuration.removeProperty("name");
        configuration.removeProperty("age");
        assertTrue(configuration.getPropertyNames().isEmpty());
        assertTrue(configuration.getProperties().isEmpty());
    }

    @Test
    void testOperateFeature() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final DemoFeature feature = new DemoFeature();
        configuration.addEnabledFeature(feature);
        configuration.addEnabledFeature(CompositeFeature1.class);
        configuration.addEnabledFeature(RequestFilter1.class);

        assertTrue(configuration.isEnabled(feature));
        assertTrue(configuration.isEnabled(DemoFeature.class));
        assertFalse(configuration.isEnabled(new CompositeFeature1()));
        assertTrue(configuration.isEnabled(CompositeFeature1.class));
        assertFalse(configuration.isEnabled(context -> false));
    }

    @Test
    void testOperateProvider() {
        final ConfigurationImpl configuration = new ConfigurationImpl();

        final Map<Class<?>, Integer> contracts1 = new HashMap<>();
        contracts1.put(ReaderInterceptor.class, 200);
        contracts1.put(WriterInterceptor.class, 300);
        configuration.addProviderClass(ReaderInterceptor1.class, contracts1);

        final Map<Class<?>, Integer> contracts2 = new HashMap<>(contracts1);
        final WriterInterceptor interceptor2 = new WriterInterceptor1();
        configuration.addProviderInstance(interceptor2, contracts2);

        final Map<Class<?>, Integer> contracts3 = new HashMap<>();
        contracts3.put(ReaderInterceptor.class, 100);
        contracts3.put(WriterInterceptor.class, 200);
        contracts3.put(ExceptionMapper.class, 300);
        contracts3.put(ContextResolver.class, 400);
        contracts3.put(Feature.class, 500);
        configuration.addProviderClass(CompositeFeature2.class, contracts3);

        final Map<Class<?>, Integer> contracts4 = new HashMap<>(contracts3);
        contracts4.put(Providers.class, 700);
        final CompositeFeature3 component4 = new CompositeFeature3();
        configuration.addProviderInstance(component4, contracts4);

        assertFalse(configuration.isRegistered(new ReaderInterceptor1()));
        assertTrue(configuration.isRegistered(ReaderInterceptor1.class));
        assertTrue(configuration.isRegistered(interceptor2));
        assertTrue(configuration.isRegistered(interceptor2.getClass()));
        assertFalse(configuration.isRegistered(new CompositeFeature2()));
        assertTrue(configuration.isRegistered(CompositeFeature2.class));
        assertTrue(configuration.isRegistered(component4));
        assertTrue(configuration.isRegistered(component4.getClass()));
        assertFalse(configuration.isRegistered(Object.class));

        assertEquals(2, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderInstances().contains(interceptor2));
        assertTrue(configuration.getProviderInstances().contains(component4));
        assertEquals(2, configuration.getProviderClasses().size());
        assertTrue(configuration.getProviderClasses().contains(ReaderInterceptor1.class));
        assertTrue(configuration.getProviderClasses().contains(CompositeFeature2.class));

        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getProviderInstances().add(new Object()));
        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getProviderClasses().add(Object.class));

        final Map<Class<?>, Integer> contracts11 = configuration.getContracts(ReaderInterceptor1.class);
        assertEquals(1, contracts11.size());
        assertEquals(200, contracts11.get(ReaderInterceptor.class));
        final Map<Class<?>, Integer> contracts22 = configuration.getContracts(WriterInterceptor1.class);
        assertEquals(1, contracts22.size());
        assertEquals(300, contracts22.get(WriterInterceptor.class));
        final Map<Class<?>, Integer> contracts33 = configuration.getContracts(CompositeFeature2.class);
        assertEquals(3, contracts33.size());
        assertEquals(300, contracts33.get(ExceptionMapper.class));
        assertEquals(400, contracts33.get(ContextResolver.class));
        assertEquals(500, contracts33.get(Feature.class));

        final Map<Class<?>, Integer> contracts44 = configuration.getContracts(CompositeFeature3.class);
        assertEquals(4, contracts44.size());
        assertEquals(300, contracts44.get(ExceptionMapper.class));
        assertEquals(400, contracts44.get(ContextResolver.class));
        assertEquals(500, contracts44.get(Feature.class));
        assertEquals(700, contracts44.get(Providers.class));

        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getContracts(CompositeFeature3.class).put(WriterInterceptor1.class, 200));

        configuration.addProviderInstance(new ReaderInterceptor1(), contracts1);
        configuration.addProviderClass(WriterInterceptor1.class, contracts2);
        configuration.addProviderInstance(new CompositeFeature2(), contracts3);
        configuration.addProviderClass(CompositeFeature3.class, contracts4);
        assertEquals(2, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderInstances().contains(interceptor2));
        assertTrue(configuration.getProviderInstances().contains(component4));
        assertEquals(2, configuration.getProviderClasses().size());
        assertTrue(configuration.getProviderClasses().contains(ReaderInterceptor1.class));
        assertTrue(configuration.getProviderClasses().contains(CompositeFeature2.class));

        configuration.addProviderInstance(new RequestFilter1(), Collections.emptyMap());
        configuration.addProviderClass(RequestFilter1.class, Collections.emptyMap());
    }

    @Test
    void testOperateResource() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final HelloWorld1 h1 = new HelloWorld1();
        configuration.addResourceInstance(h1);
        configuration.addResourceClass(HelloWorld2.class);
        assertEquals(1, configuration.getInstances().size());
        assertTrue(configuration.getInstances().contains(h1));
        assertEquals(1, configuration.getClasses().size());
        assertTrue(configuration.getClasses().contains(HelloWorld2.class));

        assertThrows(UnsupportedOperationException.class, () -> configuration.getClasses().add(Object.class));
        assertThrows(UnsupportedOperationException.class, () -> configuration.getInstances().add(new Object()));

        configuration.addResourceClass(HelloWorld1.class);
        configuration.addResourceInstance(new HelloWorld2());
        assertEquals(1, configuration.getInstances().size());
        assertTrue(configuration.getInstances().contains(h1));
        assertEquals(1, configuration.getClasses().size());
        assertTrue(configuration.getClasses().contains(HelloWorld2.class));

        configuration.addResourceClass(Object.class);
        assertEquals(1, configuration.getClasses().size());
        configuration.addResourceInstance(new HelloWorld3());
        assertEquals(1, configuration.getInstances().size());
    }

    @Path("/abc")
    private static final class HelloWorld1 {

        @GET
        public String hello() {
            return "Hello";
        }

    }

    @Path("/def")
    private static final class HelloWorld2 {

        @GET
        public String hello() {
            return "Hello";
        }

    }

    private static final class HelloWorld3 {
        @GET
        public String hello() {
            return "Hello";
        }
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

