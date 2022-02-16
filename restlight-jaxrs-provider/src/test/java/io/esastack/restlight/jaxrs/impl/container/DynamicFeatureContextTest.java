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
package io.esastack.restlight.jaxrs.impl.container;

import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicFeatureContextTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class,
                () -> new DynamicFeatureContext(null, new ConfigurationImpl()));
        assertThrows(NullPointerException.class,
                () -> new DynamicFeatureContext(DynamicFeatureContextTest.class, null));
        assertDoesNotThrow(() -> new DynamicFeatureContext(DynamicFeatureContextTest.class,
                new ConfigurationImpl()));
    }

    @Test
    void testBasic() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        DynamicFeatureContext context = new DynamicFeatureContext(DynamicFeatureContextTest.class, configuration);
        assertSame(configuration, context.getConfiguration());
        assertSame(context, context.property("name", "LiMing"));
        assertEquals("LiMing", configuration.getProperty("name"));
    }

    @Test
    void testRegister() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        DynamicFeatureContext context = new DynamicFeatureContext(DynamicFeatureContextTest.class, configuration);

        context.register(new RequestFilter1());
        assertEquals(1, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts1 = configuration.getContracts(RequestFilter1.class);
        assertEquals(1, contracts1.size());
        assertEquals(100, contracts1.get(ContainerRequestFilter.class));

        context.register(new ResponseFilter1(), 100);
        assertEquals(2, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts2 = configuration.getContracts(ResponseFilter1.class);
        assertEquals(1, contracts2.size());
        assertEquals(100, contracts2.get(ContainerResponseFilter.class));

        context.register(new CompositeFilter1(), new Class[] { ContainerRequestFilter.class,
                ContainerResponseFilter.class, ExceptionMapper.class, ContextResolver.class});
        assertEquals(3, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts3 = configuration.getContracts(CompositeFilter1.class);
        assertEquals(2, contracts3.size());
        assertEquals(300, contracts3.get(ContainerRequestFilter.class));
        assertEquals(300, contracts3.get(ContainerResponseFilter.class));

        final Map<Class<?>, Integer> contracts00 = new HashMap<>();
        contracts00.put(ContainerResponseFilter.class, 200);
        context.register(new CompositeFilter2(), contracts00);

        assertEquals(4, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderClasses().isEmpty());

        Map<Class<?>, Integer> contracts4 = configuration.getContracts(CompositeFilter2.class);
        assertEquals(1, contracts4.size());
        assertEquals(200, contracts4.get(ContainerResponseFilter.class));



        context.register(ReaderInterceptor1.class);
        assertEquals(4, configuration.getProviderInstances().size());
        assertEquals(1, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts5 = configuration.getContracts(ReaderInterceptor1.class);
        assertEquals(1, contracts5.size());
        assertEquals(500, contracts5.get(ReaderInterceptor.class));

        context.register(WriterInterceptor1.class, 100);
        assertEquals(4, configuration.getProviderInstances().size());
        assertEquals(2, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts6 = configuration.getContracts(WriterInterceptor1.class);
        assertEquals(1, contracts6.size());
        assertEquals(100, contracts6.get(WriterInterceptor.class));

        final Map<Class<?>, Integer> contracts01 = new HashMap<>(3);
        contracts01.put(ReaderInterceptor.class, 100);
        contracts01.put(ExceptionMapper.class, 300);
        contracts01.put(Feature.class, 400);
        context.register(CompositeInterceptor1.class, contracts01);
        assertEquals(4, configuration.getProviderInstances().size());
        assertEquals(3, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts7 = configuration.getContracts(CompositeInterceptor1.class);
        assertEquals(1, contracts7.size());
        assertEquals(100, contracts7.get(ReaderInterceptor.class));

        final Map<Class<?>, Integer> contracts11 = new HashMap<>();
        contracts11.put(WriterInterceptor.class, 200);
        context.register(CompositeInterceptor2.class, contracts11);

        assertEquals(4, configuration.getProviderInstances().size());
        assertEquals(4, configuration.getProviderClasses().size());

        Map<Class<?>, Integer> contracts8 = configuration.getContracts(CompositeInterceptor2.class);
        assertEquals(1, contracts8.size());
        assertEquals(200, contracts8.get(WriterInterceptor.class));
    }

    @Test
    void testUnRegistrable() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        DynamicFeatureContext context = new DynamicFeatureContext(DynamicFeatureContextTest.class, configuration);
        context.register(new Object());
        assertTrue(configuration.getProviderInstances().isEmpty());
        assertTrue(configuration.getProviderClasses().isEmpty());

        context.register(RequestFilter11.class);
        Map<Class<?>, Integer> contracts1 = configuration.getContracts(RequestFilter11.class);
        assertTrue(contracts1.isEmpty());
        assertTrue(configuration.getProviderInstances().isEmpty());
        assertTrue(configuration.getProviderClasses().isEmpty());

        context.register(CompositeFilter11.class);
        Map<Class<?>, Integer> contracts2 = configuration.getContracts(CompositeFilter11.class);
        assertEquals(1, contracts2.size());
        assertEquals(1000, contracts2.get(ContainerResponseFilter.class));

        assertTrue(configuration.getProviderInstances().isEmpty());
        assertEquals(1, configuration.getProviderClasses().size());
    }

    @Priority(100)
    private static final class RequestFilter1 implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    @Priority(200)
    private static final class ResponseFilter1 implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @Priority(300)
    private static final class CompositeFilter1 implements ContainerRequestFilter, ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @Priority(400)
    private static final class CompositeFilter2 implements ContainerRequestFilter, ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @Priority(500)
    private static final class ReaderInterceptor1 implements ReaderInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }
    }

    @Priority(600)
    private static final class WriterInterceptor1 implements WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    @Priority(700)
    private static final class CompositeInterceptor1 implements ReaderInterceptor, WriterInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    @Priority(800)
    private static final class CompositeInterceptor2 implements ReaderInterceptor, WriterInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    @Priority(900)
    @PreMatching
    private static final class RequestFilter11 implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    @Priority(1000)
    @PreMatching
    private static final class CompositeFilter11 implements ContainerRequestFilter, ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

}

