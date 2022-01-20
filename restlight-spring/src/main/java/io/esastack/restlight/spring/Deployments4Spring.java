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

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.commons.spi.Feature;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.Deployments;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.configure.HandlerRegistryAware;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.handler.RouteFilterAdapter;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.MappingInterceptor;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.StringConverterAdapter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.ContextResolverProvider;
import io.esastack.restlight.core.spi.HandlerRegistryAwareFactory;
import io.esastack.restlight.core.spi.ParamResolverAdviceProvider;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.spi.RequestEntityResolverAdviceProvider;
import io.esastack.restlight.core.spi.RequestEntityResolverProvider;
import io.esastack.restlight.core.spi.ResponseEntityResolverAdviceProvider;
import io.esastack.restlight.core.spi.ResponseEntityResolverProvider;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.core.spi.impl.JacksonDefaultSerializerFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.handler.ConnectionHandler;
import io.esastack.restlight.server.handler.DisConnectionHandler;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.schedule.RequestTaskHook;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.spi.ConnectionHandlerFactory;
import io.esastack.restlight.server.spi.DisConnectionHandlerFactory;
import io.esastack.restlight.server.spi.ExceptionHandlerFactory;
import io.esastack.restlight.server.spi.FilterFactory;
import io.esastack.restlight.server.spi.RequestTaskHookFactory;
import io.esastack.restlight.server.spi.RouteRegistryAware;
import io.esastack.restlight.server.spi.RouteRegistryAwareFactory;
import io.esastack.restlight.spring.serialize.GsonHttpBodySerializerAdapter;
import io.esastack.restlight.spring.spi.AdviceLocator;
import io.esastack.restlight.spring.spi.ControllerLocator;
import io.esastack.restlight.spring.spi.ExtensionLocator;
import io.esastack.restlight.spring.util.DeployContextConfigure;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This Deployments will auto-configure itself from the {@link ApplicationContext}.
 */
public class Deployments4Spring<R extends AbstractRestlight4Spring<R, D>, D extends Deployments4Spring<R, D>>
        extends Deployments<R, D> {

    final List<DeployContextConfigure> contextConfigures = new LinkedList<>();

    protected Deployments4Spring(R restlight, ApplicationContext context, RestlightOptions options) {
        super(restlight, options);
        autoConfigureFromSpringContext(context);
    }

    void autoConfigureFromSpringContext(ApplicationContext context) {
        // configures of BaseDeployments
        configureContextConfigures(context);
        configureRoutes(context);
        configureSchedulers(context);
        configureConnectionHandler(context);
        configureDisConnectionHandler(context);
        configureFilters(context);
        configureExceptionHandlers(context);
        configureRouteRegistryAwareness(context);
        configureRequestTaskHooks(context);

        // configures of Deployments
        configureRouteFilter(context);
        configureExtensions(context);
        configureHandlerConfigure(context);
        configureMappings(context);
        configureController(context);
        configureControllerAdvices(context);
        configureInterceptors(context);
        configureStringConverter(context);
        configureParamResolversAndAdvices(context);
        configureContextResolvers(context);
        configureRequestEntityResolversAndAdvices(context);
        configureResponseEntityResolversAndAdvices(context);
        configureSerializers(context);
        configureExceptionResolvers(context);
        configureHandlerRegistryAwareness(context);
    }

    private void configureContextConfigures(ApplicationContext context) {
        Map<String, DeployContextConfigure> configures = beansOfType(context, DeployContextConfigure.class);
        if (!configures.isEmpty()) {
            contextConfigures.addAll(configures.values());
            OrderedComparator.sort(contextConfigures);
        }
    }

    private void configureSchedulers(ApplicationContext context) {
        Map<String, Scheduler> schedulers = beansOfType(context, Scheduler.class);
        if (!schedulers.isEmpty()) {
            this.addSchedulers(schedulers.values());
        }
    }

    private void configureConnectionHandler(ApplicationContext context) {
        List<ConnectionHandlerFactory> factories = new LinkedList<>();
        Map<String, ConnectionHandler> handlers = beansOfType(context, ConnectionHandler.class);
        if (!handlers.isEmpty()) {
            factories.addAll(handlers.values()
                    .stream()
                    .map(handler -> (ConnectionHandlerFactory) ctx -> Optional.of(handler))
                    .collect(Collectors.toList()));
        }
        factories.addAll(beansOfType(context, ConnectionHandlerFactory.class).values());
        this.addConnectionHandlers(factories);
    }

    private void configureDisConnectionHandler(ApplicationContext context) {
        List<DisConnectionHandlerFactory> factories = new LinkedList<>();
        Map<String, DisConnectionHandler> handlers = beansOfType(context, DisConnectionHandler.class);
        if (!handlers.isEmpty()) {
            factories.addAll(handlers.values()
                    .stream()
                    .map(handler -> (DisConnectionHandlerFactory) ctx -> Optional.of(handler))
                    .collect(Collectors.toList()));
        }
        factories.addAll(beansOfType(context, DisConnectionHandlerFactory.class).values());
        this.addDisConnectionHandlers(factories);
    }

    private void configureFilters(ApplicationContext context) {
        Map<String, Filter> filters = beansOfType(context, Filter.class);
        if (!filters.isEmpty()) {
            filters.values().forEach(this::addFilter);
        }
        Map<String, FilterFactory> factories = beansOfType(context, FilterFactory.class);
        if (!factories.isEmpty()) {
            this.addFilters(factories.values());
        }
    }

    private void configureExceptionHandlers(ApplicationContext context) {
        Map<String, IExceptionHandler> exceptionHandlers = beansOfType(context, IExceptionHandler.class);
        if (!exceptionHandlers.isEmpty()) {
            this.addExceptionHandlers(exceptionHandlers.values());
        }
        beansOfType(context, ExceptionHandlerFactory.class).values()
                .forEach(this::addExceptionHandler);
    }

    private void configureRouteRegistryAwareness(ApplicationContext context) {
        List<RouteRegistryAwareFactory> factories = new LinkedList<>();
        Map<String, RouteRegistryAware> awareness = beansOfType(context, RouteRegistryAware.class);
        if (!awareness.isEmpty()) {
            factories.addAll(awareness.values()
                    .stream()
                    .map(aware -> (RouteRegistryAwareFactory) ctx -> Optional.of(aware))
                    .collect(Collectors.toList()));
        }
        factories.addAll(beansOfType(context, RouteRegistryAwareFactory.class).values());
        this.addRouteRegistryAwareness(factories);
    }

    private void configureRequestTaskHooks(ApplicationContext context) {
        Collection<RequestTaskHookFactory> factories = beansOfType(context, RequestTaskHookFactory.class).values();
        if (!factories.isEmpty()) {
            this.addRequestTaskHooks(factories);
        }

        Collection<RequestTaskHook> advices = beansOfType(context, RequestTaskHook.class).values();
        if (!advices.isEmpty()) {
            advices.forEach(this::addRequestTaskHook);
        }
    }

    private void configureRoutes(ApplicationContext context) {
        beanOfType(context, Route.class).ifPresent(this::addRoute);
    }

    private void configureRouteFilter(ApplicationContext context) {
        beansOfType(context, RouteFilterAdapter.class).values().forEach(this::addRouteFilter);
        this.addRouteFilters(beansOfType(context, RouteFilterFactory.class).values());
    }

    private void configureExtensions(ApplicationContext context) {
        Collection<ExtensionLocator> locators = SpiLoader.cached(ExtensionLocator.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (!locators.isEmpty()) {
            Set<Object> controllers = new LinkedHashSet<>();
            locators.forEach(l -> controllers.addAll(l.getExtensions(context, ctx())));
            this.addExtensions(controllers);
        }
    }

    private void configureHandlerConfigure(ApplicationContext context) {
        this.addHandlerConfigures(beansOfType(context, HandlerConfigure.class).values());
    }

    private void configureMappings(ApplicationContext context) {
        // get mappings
        Collection<HandlerMappingProvider> handlerMappingProviders =
                beansOfType(context, HandlerMappingProvider.class).values();
        if (!handlerMappingProviders.isEmpty()) {
            this.addHandlerMappingProviders(handlerMappingProviders);
        }

        Collection<HandlerMapping> handlerMappings =
                beansOfType(context, HandlerMapping.class).values();
        if (!handlerMappings.isEmpty()) {
            this.addHandlerMappings(handlerMappings);
        }
    }

    private void configureController(ApplicationContext context) {
        Collection<ControllerLocator> locators = SpiLoader.cached(ControllerLocator.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (!locators.isEmpty()) {
            Set<Object> controllers = new LinkedHashSet<>();
            locators.forEach(l -> controllers.addAll(l.getControllers(context, ctx())));
            this.addControllers(controllers);
        }
    }

    private void configureControllerAdvices(ApplicationContext context) {
        Collection<AdviceLocator> adviceLocators = SpiLoader.cached(AdviceLocator.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (!adviceLocators.isEmpty()) {
            Set<Object> advices = new LinkedHashSet<>();
            adviceLocators.forEach(l -> advices.addAll(l.getAdvices(context, ctx())));
            this.addControllerAdvices(advices);
        }
    }

    private void configureStringConverter(ApplicationContext context) {
        beansOfType(context, StringConverterAdapter.class).values()
                .forEach(this::addStringConverter);
        this.addStringConverters(beansOfType(context, StringConverterFactory.class).values());
    }

    private void configureParamResolversAndAdvices(ApplicationContext context) {
        // auto inject param resolvers
        beansOfType(context, ParamResolverAdapter.class)
                .values()
                .forEach(this::addParamResolver);
        this.addParamResolvers(beansOfType(context, ParamResolverFactory.class)
                .values());
        beansOfType(context, ParamResolverProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addParamResolver);

        // auto inject param resolver advices
        beansOfType(context, ParamResolverAdviceAdapter.class)
                .values()
                .forEach(this::addParamResolverAdvice);
        this.addParamResolverAdvices(beansOfType(context, ParamResolverAdviceFactory.class).values());
        beansOfType(context, ParamResolverAdviceProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addParamResolverAdvice);
    }

    private void configureContextResolvers(ApplicationContext context) {
        // auto inject context resolvers
        beansOfType(context, ContextResolverAdapter.class)
                .values()
                .forEach(this::addContextResolver);
        this.addContextResolvers(beansOfType(context, ContextResolverFactory.class).values());
        beansOfType(context, ContextResolverProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addContextResolver);
    }

    private void configureRequestEntityResolversAndAdvices(ApplicationContext context) {
        // auto inject request entity resolvers
        beansOfType(context, RequestEntityResolverAdapter.class)
                .values()
                .forEach(this::addRequestEntityResolver);
        this.addRequestEntityResolvers(beansOfType(context, RequestEntityResolverFactory.class).values());
        beansOfType(context, RequestEntityResolverProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addRequestEntityResolver);

        // auto inject request entity resolver advices.
        beansOfType(context, RequestEntityResolverAdviceAdapter.class)
                .values()
                .forEach(this::addRequestEntityResolverAdvice);
        this.addRequestEntityResolverAdvices(beansOfType(context, RequestEntityResolverAdviceFactory.class).values());
        beansOfType(context, RequestEntityResolverAdviceProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addRequestEntityResolverAdvice);
    }

    private void configureResponseEntityResolversAndAdvices(ApplicationContext context) {
        // auto inject response entity resolvers
        beansOfType(context, ResponseEntityResolverAdapter.class).values()
                .forEach(this::addResponseEntityResolver);
        this.addResponseEntityResolvers(beansOfType(context, ResponseEntityResolverFactory.class).values());
        beansOfType(context, ResponseEntityResolverProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addResponseEntityResolver);

        // auto inject response entity resolver advices.
        beansOfType(context, ResponseEntityResolverAdviceAdapter.class)
                .values()
                .forEach(this::addResponseEntityResolverAdvice);
        this.addResponseEntityResolverAdvices(beansOfType(context, ResponseEntityResolverAdviceFactory.class).values());
        beansOfType(context, ResponseEntityResolverAdviceProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addResponseEntityResolverAdvice);
    }

    private void configureSerializers(ApplicationContext context) {
        // auto inject ObjectMapper
        Class<?> objectMapperClz = ClassUtils.forName("com.fasterxml.jackson.databind.ObjectMapper");
        if (objectMapperClz != null) {
            beanOfType(context, objectMapperClz)
                    .ifPresent(objectMapper -> ctx()
                            .attrs().attr(JacksonDefaultSerializerFactory.OBJECT_MAPPER).set(objectMapper));
        }

        // Check whether GsonHttpBodySerializer and GsonHttpBodySerializerAdapter exist at the same time
        checkDuplicatedGsonSerializer(context);

        // auto inject serializer
        beansOfType(context, HttpRequestSerializer.class).values().forEach(this::addRequestSerializer);
        beansOfType(context, HttpResponseSerializer.class).values().forEach(this::addResponseSerializer);
    }

    private void checkDuplicatedGsonSerializer(ApplicationContext context) {
        try {
            if (context.getBean(GsonHttpBodySerializer.class) != null
                    && context.getBean(GsonHttpBodySerializerAdapter.class) != null) {
                throw new IllegalStateException("Duplicated GsonHttpBodySerializer and " +
                        "GsonHttpBodySerializer4Spring at the same time");
            }
        } catch (NoSuchBeanDefinitionException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> D addExceptionResolver(ExceptionResolver<T> resolver) {
        Class<T> t = (Class<T>) ClassUtils.findFirstGenericType(resolver.getClass(), ExceptionResolver.class)
                .orElse(Throwable.class);
        return addExceptionResolver(t, resolver);
    }

    private void configureInterceptors(ApplicationContext context) {
        // auto inject interceptor
        addMappingInterceptors(beansOfType(context, MappingInterceptor.class).values());
        addHandlerInterceptors(beansOfType(context, HandlerInterceptor.class).values());
        addRouteInterceptors(beansOfType(context, RouteInterceptor.class).values());
        addInterceptors(beansOfType(context, Interceptor.class).values());
        addInterceptorFactories(beansOfType(context, InterceptorFactory.class).values());
    }

    private void configureExceptionResolvers(ApplicationContext context) {
        // auto inject serializer
        beansOfType(context, ExceptionResolver.class).values().forEach(this::addExceptionResolver);
    }

    private void configureHandlerRegistryAwareness(ApplicationContext context) {
        beansOfType(context, HandlerRegistryAware.class).values().forEach(this::addHandlerRegistryAware);
        addHandlerRegistryAwareness(beansOfType(context, HandlerRegistryAwareFactory.class).values());
    }

    private <T> Optional<T> beanOfType(ApplicationContext context, Class<T> clz) {
        Map<String, T> beans = beansOfType(context, clz, restlight.name());
        if (beans.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(beans.values().iterator().next());
    }

    private <T> Map<String, T> beansOfType(ApplicationContext context, Class<T> clz) {
        return beansOfType(context, clz, restlight.name());
    }

    static <T> Map<String, T> beansOfType(ApplicationContext context, Class<T> clz, String serverName) {
        Map<String, T> beans = context.getBeansOfType(clz);
        if (beans.isEmpty()) {
            return beans;
        }
        Map<String, T> qualified = new LinkedHashMap<>(beans.size());
        beans.forEach((name, bean) -> {
            if (isMatchGroup(context, name, serverName)) {
                qualified.put(name, bean);
            }
        });
        return qualified;
    }

    private static boolean isMatchGroup(ApplicationContext context, String name, String serverName) {
        Feature feature = AnnotationUtils.findAnnotation(context.getType(name), Feature.class);
        String[] groups = {};
        if (feature != null) {
            groups = feature.groups();
        }
        if (groups.length == 0) {
            return true;
        }
        for (String g : groups) {
            if (serverName.equals(g)) {
                return true;
            }
        }
        return false;
    }


    public static class Impl extends Deployments4Spring<Restlight4Spring, Impl> {

        Impl(Restlight4Spring restlight, ApplicationContext context, RestlightOptions options) {
            super(restlight, context, options);
        }
    }

}
