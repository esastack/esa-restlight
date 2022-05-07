/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.spring;

import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.MappingInterceptor;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.server.processor.schedule.RequestTaskHook;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import io.esastack.restlight.core.spi.RequestTaskHookFactory;
import io.esastack.restlight.spring.serialize.GsonHttpBodySerializerAdapter;
import io.esastack.restlight.spring.spi.AdviceLocator;
import io.esastack.restlight.spring.spi.ControllerLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Deployments4SpringTest {


    @BeforeEach
    void setUp() {
        ControllerLocator0.context = null;
        AdviceLocator0.toReturn = Collections.emptyList();
    }

    @Test
    void testAutoConfigureWithEmptyContext() {
        final Restlight4Spring restlight = mock(Restlight4Spring.class);
        final ApplicationContext context = mock(ApplicationContext.class);

        final Deployments4Spring.Impl mock =
                spy(new Deployments4Spring.Impl(restlight, context, RestlightOptionsConfigure.defaultOpts()));
        // call it again
        mock.autoConfigureFromSpringContext(context);

        // schedulers
        final Map<String, Scheduler> schedulers = mock.deployContext().schedulers();
        assertEquals(2, schedulers.size());
        assertTrue(schedulers.containsKey(Schedulers.IO));
        assertTrue(schedulers.containsKey(Schedulers.BIZ));
        verify(mock, never()).addSchedulers(any());
        verify(mock, never()).addScheduler(any());

        verify(mock, never()).addRequestTaskHook(any(RequestTaskHook.class));
        verify(mock, never()).addRequestTaskHook(any(RequestTaskHookFactory.class));
        verify(mock, never()).addRequestTaskHooks(any());

        // routes
        verify(mock, never()).addRoutes(any());
        verify(mock, never()).addRoute(any());

        // mappings
        verify(mock, never()).addHandlerMappingProvider(any());
        verify(mock, never()).addHandlerMappingProviders(any());
        verify(mock, never()).addHandlerMapping(any(HandlerMapping.class));
        verify(mock, never()).addHandlerMappings(anyCollection());

        // controllers
        verify(mock, never()).addController(any());

        // advice
        verify(mock, never()).addControllerAdvice(any());

        // serializers
        verify(mock, never()).addRequestSerializer(any());
        verify(mock, never()).addRequestSerializers(any());
        verify(mock, never()).addResponseSerializer(any());
        verify(mock, never()).addResponseSerializer(any());
        verify(mock, never()).addSerializer(any());
        verify(mock, never()).addSerializers(any());

        // exception resolver
        verify(mock, never()).addExceptionResolver(any(), any());

        // interceptors
        verify(mock, never()).addMappingInterceptor(any());
        verify(mock, never()).addMappingInterceptors(argThat(c -> c != null && !c.isEmpty()));
        verify(mock, never()).addHandlerInterceptor(any());
        verify(mock, never()).addHandlerInterceptors(argThat(c -> c != null && !c.isEmpty()));
        verify(mock, never()).addRouteInterceptor(any());
        verify(mock, never()).addRouteInterceptors(argThat(c -> c != null && !c.isEmpty()));
        verify(mock, never()).addInterceptorFactory(any());
        verify(mock, never()).addInterceptorFactories(argThat(c -> c != null && !c.isEmpty()));
        verify(mock, never()).addInterceptor(any());
        verify(mock, never()).addInterceptors(argThat(c -> c != null && !c.isEmpty()));
    }

    @Test
    void testAutoConfigure() {

        final Restlight4Spring restlight = mock(Restlight4Spring.class);
        final ApplicationContext context = mock(ApplicationContext.class);

        // mock target
        final Deployments4Spring.Impl mock =
                spy(new Deployments4Spring.Impl(restlight, context, RestlightOptionsConfigure.defaultOpts()));

        // scheduler
        final Scheduler scheduler = Schedulers.fromExecutor("foo", r -> {
        });
        when(context.getBeansOfType(eq(Scheduler.class)))
                .thenReturn(Collections.singletonMap("useless", scheduler));

        // request task hook
        final RequestTaskHook requestTaskHook1 = task -> task;
        final RequestTaskHookFactory requestTaskAdvice2 = ctx -> Optional.of(requestTaskHook1);

        when(context.getBeansOfType(eq(RequestTaskHook.class)))
                .thenReturn(Collections.singletonMap("useless", requestTaskHook1));
        when(context.getBeansOfType(eq(RequestTaskHookFactory.class)))
                .thenReturn(Collections.singletonMap("useless", requestTaskAdvice2));

        // route
        final Route route = Route.route();
        when(context.getBeansOfType(eq(Route.class)))
                .thenReturn(Collections.singletonMap("useless", route));

        // handler mapping
        final HandlerMappingProvider handlerMappingProvider = mock(HandlerMappingProvider.class);
        when(context.getBeansOfType(eq(HandlerMappingProvider.class)))
                .thenReturn(Collections.singletonMap("useless", handlerMappingProvider));

        final HandlerMapping handlerMapping = mock(HandlerMapping.class);
        when(context.getBeansOfType(eq(HandlerMapping.class)))
                .thenReturn(Collections.singletonMap("useless", handlerMapping));

        // controller
        final Object controller = new Object();
        ControllerLocator0.context = context;
        when(context.getBeansWithAnnotation(eq(Controller.class)))
                .thenReturn(Collections.singletonMap("useless", controller));

        // controller advice
        final Object advice = new Object();
        AdviceLocator0.toReturn = Collections.singleton(advice);

        // serializer
        final HttpRequestSerializer requestSerializer = mock(HttpRequestSerializer.class);
        when(context.getBeansOfType(eq(HttpRequestSerializer.class)))
                .thenReturn(Collections.singletonMap("useless", requestSerializer));

        final HttpResponseSerializer responseSerializer = mock(HttpResponseSerializer.class);
        when(context.getBeansOfType(eq(HttpResponseSerializer.class)))
                .thenReturn(Collections.singletonMap("useless", responseSerializer));

        // exception resolver
        final ExceptionResolver<IllegalStateException> exceptionExceptionResolver = new ExResolver();
        when(context.getBeansOfType(eq(ExceptionResolver.class)))
                .thenReturn(Collections.singletonMap("useless", exceptionExceptionResolver));

        // interceptor
        final MappingInterceptor mappingInterceptor = mock(MappingInterceptor.class);
        when(context.getBeansOfType(eq(MappingInterceptor.class)))
                .thenReturn(Collections.singletonMap("useless", mappingInterceptor));

        final HandlerInterceptor handlerInterceptor = mock(HandlerInterceptor.class);
        when(context.getBeansOfType(eq(HandlerInterceptor.class)))
                .thenReturn(Collections.singletonMap("useless", handlerInterceptor));

        final RouteInterceptor routeInterceptor = mock(RouteInterceptor.class);
        when(context.getBeansOfType(eq(RouteInterceptor.class)))
                .thenReturn(Collections.singletonMap("useless", routeInterceptor));

        final InterceptorFactory interceptorFactory = mock(InterceptorFactory.class);
        when(context.getBeansOfType(eq(InterceptorFactory.class)))
                .thenReturn(Collections.singletonMap("useless", interceptorFactory));

        final Interceptor interceptor = mock(Interceptor.class);
        when(context.getBeansOfType(eq(Interceptor.class)))
                .thenReturn(Collections.singletonMap("useless", interceptor));

        mock.autoConfigureFromSpringContext(context);

        // schedulers
        final Map<String, Scheduler> schedulers = mock.deployContext().schedulers();
        assertEquals(3, schedulers.size());
        assertTrue(schedulers.containsKey(Schedulers.IO));
        assertTrue(schedulers.containsKey(Schedulers.BIZ));
        assertTrue(schedulers.containsKey("foo"));
        verify(mock).addSchedulers(argThat(c -> c != null && c.contains(scheduler)));

        // request task hook
        verify(mock).addRequestTaskHook(same(requestTaskHook1));
        verify(mock).addRequestTaskHooks(argThat(c -> c != null && c.contains(requestTaskAdvice2)));

        // serializers
        verify(mock).addRequestSerializer(same(requestSerializer));
        verify(mock).addResponseSerializer(same(responseSerializer));

        // exception resolver
        verify(mock).addExceptionResolver(eq(IllegalStateException.class), same(exceptionExceptionResolver));

        // interceptors
        verify(mock).addMappingInterceptors(argThat(c -> c != null && c.contains(mappingInterceptor)));
        verify(mock).addHandlerInterceptors(argThat(c -> c != null && c.contains(handlerInterceptor)));
        verify(mock).addRouteInterceptors(argThat(c -> c != null && c.contains(routeInterceptor)));
        verify(mock).addInterceptorFactories(argThat(c -> c != null && c.contains(interceptorFactory)));
        verify(mock).addInterceptors(argThat(c -> c != null && c.contains(interceptor)));
    }

    @Test
    void testDuplicatedSerializer() {
        final Restlight4Spring restlight = mock(Restlight4Spring.class);
        final ApplicationContext context = mock(ApplicationContext.class);

        reset(context);
        when(context.getBean(eq(GsonHttpBodySerializer.class)))
                .thenReturn(mock(GsonHttpBodySerializer.class));
        when(context.getBean(eq(GsonHttpBodySerializerAdapter.class)))
                .thenReturn(mock(GsonHttpBodySerializerAdapter.class));
        assertThrows(IllegalStateException.class,
                () -> new Deployments4Spring.Impl(restlight, context, RestlightOptionsConfigure.defaultOpts()));
    }

    @Feature(tags = Constants.INTERNAL)
    public static class ControllerLocator0 implements ControllerLocator {

        static ApplicationContext context;

        @Override
        public Collection<Object> getControllers(ApplicationContext spring,
                                                 DeployContext ctx) {
            if (context == null) {
                return Collections.emptyList();
            }
            return ControllerLocator.super.getControllers(context, ctx);
        }
    }

    @Feature(tags = Constants.INTERNAL)
    public static class AdviceLocator0 implements AdviceLocator {

        static Collection<Object> toReturn = Collections.emptyList();

        @Override
        public Collection<Object> getAdvices(ApplicationContext spring, DeployContext ctx) {

            return toReturn;
        }
    }

    private static class ExResolver implements ExceptionResolver<IllegalStateException> {

        @Override
        public CompletionStage<Void> handleException(RequestContext context, IllegalStateException e) {
            return null;
        }
    }

}
