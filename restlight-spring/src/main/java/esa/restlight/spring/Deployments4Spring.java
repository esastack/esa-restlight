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
package esa.restlight.spring;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.commons.spi.Feature;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.Deployments;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.HandlerMapping;
import esa.restlight.core.handler.HandlerMappingProvider;
import esa.restlight.core.interceptor.HandlerInterceptor;
import esa.restlight.core.interceptor.Interceptor;
import esa.restlight.core.interceptor.InterceptorFactory;
import esa.restlight.core.interceptor.MappingInterceptor;
import esa.restlight.core.interceptor.RouteInterceptor;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceFactory;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceFactory;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.GsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.spi.ArgumentResolverProvider;
import esa.restlight.core.spi.ReturnValueResolverProvider;
import esa.restlight.core.spi.impl.JacksonDefaultSerializerFactory;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.server.route.Route;
import esa.restlight.server.schedule.RequestTaskHook;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.spi.RequestTaskHookFactory;
import esa.restlight.spring.serialize.GsonHttpBodySerializerAdapter;
import esa.restlight.spring.spi.AdviceLocator;
import esa.restlight.spring.spi.ControllerLocator;
import esa.restlight.spring.util.DeployContextConfigure;
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

/**
 * This Deployments will auto-configure itself from the {@link ApplicationContext}.
 */
public class Deployments4Spring<R extends AbstractRestlight4Spring<R, D, O>, D extends Deployments4Spring<R, D, O>,
        O extends RestlightOptions>
        extends Deployments<R, D, O> {

    final List<DeployContextConfigure> contextConfigures = new LinkedList<>();

    protected Deployments4Spring(R restlight, ApplicationContext context, O options) {
        super(restlight, options);
        autoConfigureFromSpringContext(context);
    }

    void autoConfigureFromSpringContext(ApplicationContext context) {
        configureContextConfigures(context);
        configureSchedulers(context);
        configureRequestTaskHooks(context);
        configureRoutes(context);
        configureMappings(context);
        configureController(context);
        configureAdvices(context);
        configureResolvers(context);
        configureSerializers(context);
        configureExceptionResolvers(context);
        configureInterceptors(context);
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
        Collection<Route> routes = beansOfType(context, Route.class).values();
        if (!routes.isEmpty()) {
            this.addRoutes(routes);
        }
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

    private void configureAdvices(ApplicationContext context) {
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

    private void configureResolvers(ApplicationContext context) {
        // auto inject argument resolver
        beansOfType(context, ArgumentResolverAdapter.class)
                .values()
                .forEach(this::addArgumentResolver);
        beansOfType(context, ArgumentResolverFactory.class)
                .values()
                .forEach(this::addArgumentResolver);
        beansOfType(context, ArgumentResolverProvider.class)
                .values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addArgumentResolver);
        // auto inject return value resolver
        beansOfType(context, ReturnValueResolverAdapter.class)
                .values()
                .forEach(this::addReturnValueResolver);
        beansOfType(context, ReturnValueResolverFactory.class)
                .values()
                .forEach(this::addReturnValueResolver);
        beansOfType(context, ReturnValueResolverProvider.class).values()
                .stream()
                .map(p -> p.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addReturnValueResolver);
        // auto inject argument resolver advice
        beansOfType(context, ArgumentResolverAdviceAdapter.class)
                .values()
                .forEach(this::addArgumentResolverAdvice);
        beansOfType(context, ArgumentResolverAdviceFactory.class)
                .values()
                .forEach(this::addArgumentResolverAdvice);
        // auto inject return value resolver advice
        beansOfType(context, ReturnValueResolverAdviceAdapter.class)
                .values()
                .forEach(this::addReturnValueResolverAdvice);
        beansOfType(context, ReturnValueResolverAdviceFactory.class)
                .values()
                .forEach(this::addReturnValueResolverAdvice);
    }

    private void configureSerializers(ApplicationContext context) {
        // auto inject ObjectMapper
        Class<?> objectMapperClz = ClassUtils.forName("com.fasterxml.jackson.databind.ObjectMapper");
        if (objectMapperClz != null) {
            beanOfType(context, objectMapperClz)
                    .ifPresent(objectMapper -> ctx()
                            .attribute(JacksonDefaultSerializerFactory.OBJECT_MAPPER, objectMapper));
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


    public static class Impl extends Deployments4Spring<Restlight4Spring, Impl, RestlightOptions> {

        Impl(Restlight4Spring restlight, ApplicationContext context, RestlightOptions options) {
            super(restlight, context, options);
        }
    }

}
