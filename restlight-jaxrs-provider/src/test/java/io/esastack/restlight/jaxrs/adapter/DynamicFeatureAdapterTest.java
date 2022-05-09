/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.adapter;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.deploy.ConfigurableHandler;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdvice;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicFeatureAdapterTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new DynamicFeatureAdapter(null,
                null, null, new ConfigurationImpl()));
        assertThrows(NullPointerException.class, () -> new DynamicFeatureAdapter(mock(DeployContext.class),
                null, null, null));
        assertDoesNotThrow(() -> new DynamicFeatureAdapter(mock(DeployContext.class),
                null, null, new ConfigurationImpl()));
    }

    @Test
    void testConfigure() throws Throwable {
        final DeployContext deployContext = mock(DeployContext.class);
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final DynamicFeatureAdapter feature = new DynamicFeatureAdapter(deployContext, null,
                null, configuration);

        final List<RequestEntityResolverAdvice> requestAdvices = new LinkedList<>();
        final List<ResponseEntityResolverAdvice> responseAdvices = new LinkedList<>();
        final List<RouteFilter> routeFilters = new LinkedList<>();
        final List<ContextResolverAdapter> contextResolvers = new LinkedList<>();
        final ConfigurableHandler configurable = mock(ConfigurableHandler.class);
        when(configurable.addRequestEntityResolverAdvice(any())).thenAnswer(invocationOnMock -> {
            requestAdvices.add(invocationOnMock.getArgument(0));
            return null;
        });
        when(configurable.addResponseEntityResolverAdvice(any())).thenAnswer(invocationOnMock -> {
            responseAdvices.add(invocationOnMock.getArgument(0));
            return null;
        });
        when(configurable.addRouteFilter(any())).thenAnswer(invocationOnMock -> {
            routeFilters.add(invocationOnMock.getArgument(0));
            return null;
        });
        when(configurable.addContextResolver(any())).thenAnswer(invocationOnMock -> {
            contextResolvers.add(invocationOnMock.getArgument(0));
            return null;
        });

        // when features\interceptors\filters are all empty.    ====> method
        final HandlerMethod method = HandlerMethodImpl.of(Hello.class,
                Hello.class.getDeclaredMethod("sayHello"));
        feature.configure(method, configurable);
        assertEquals(0, requestAdvices.size());
        assertEquals(0, responseAdvices.size());
        assertEquals(1, routeFilters.size());
        assertEquals(2, contextResolvers.size());

        requestAdvices.clear();
        responseAdvices.clear();
        routeFilters.clear();
        contextResolvers.clear();

        // when features\interceptors\filters are all empty.    ====> locator
        final HandlerMethod method1 = HandlerMethodImpl.of(Hello.class,
                Hello.class.getDeclaredMethod("sayHello0"));
        feature.configure(method1, configurable);
        assertEquals(0, requestAdvices.size());
        assertEquals(0, responseAdvices.size());
        assertEquals(0, routeFilters.size());
        assertEquals(2, contextResolvers.size());

        final AtomicInteger count = new AtomicInteger();
        final DynamicFeature f = (resourceInfo, context) -> count.incrementAndGet();

        // when features\interceptors\filters arent' empty.    ====> method
        final DynamicFeatureAdapter feature1 = new DynamicFeatureAdapter(deployContext,
                Collections.singleton(NameBinding1.class),
                Collections.singletonList(new ProxyComponent<>(f, f)), configuration);
        final ConfigurableImpl c = new ConfigurableImpl(configuration);

        c.register(RequestFilter.class);
        c.register(ResponseFilter.class);
        c.register(ReaderInterceptor.class);
        c.register(WriterInterceptor.class);

        requestAdvices.clear();
        responseAdvices.clear();
        routeFilters.clear();
        contextResolvers.clear();

        feature1.configure(method1, configurable);
        assertEquals(0, requestAdvices.size());
        assertEquals(0, responseAdvices.size());
        assertEquals(0, routeFilters.size());
        assertEquals(2, contextResolvers.size());
        assertEquals(1, count.intValue());

        requestAdvices.clear();
        responseAdvices.clear();
        routeFilters.clear();
        contextResolvers.clear();
        count.set(0);

        final Set<Class<? extends Annotation>> appNameBindings = new HashSet<>();
        appNameBindings.add(NameBinding1.class);
        appNameBindings.add(NameBinding2.class);
        appNameBindings.add(NameBinding3.class);
        final DynamicFeatureAdapter feature2 = new DynamicFeatureAdapter(deployContext,
                appNameBindings,
                Collections.singletonList(new ProxyComponent<>(f, f)), configuration);
        feature2.configure(method, configurable);
        assertEquals(1, requestAdvices.size());
        assertEquals(1, responseAdvices.size());
        assertEquals(2, routeFilters.size());
        assertEquals(2, contextResolvers.size());
    }

    private static final class Hello {

        @GET
        public String sayHello() {
            return "Hello";
        }

        public String sayHello0() {
            return "Hello0";
        }

    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class RequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class ReaderInterceptor implements jakarta.ws.rs.ext.ReaderInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class WriterInterceptor implements jakarta.ws.rs.ext.WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

        }
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding1 {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding2 {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding3 {
    }

}

