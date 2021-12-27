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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.ConfigurableHandler;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.jaxrs.configure.OrderComponent;
import io.esastack.restlight.jaxrs.configure.ProvidersProxyFactory;
import io.esastack.restlight.jaxrs.configure.ProvidersProxyFactoryImpl;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.jaxrs.impl.container.DynamicFeatureContext;
import io.esastack.restlight.jaxrs.impl.container.ResourceInfoImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.impl.ext.ProvidersImpl;
import io.esastack.restlight.jaxrs.resolver.context.ConfigurationResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.context.ProvidersResolverAdapter;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.esastack.restlight.jaxrs.util.JaxrsUtils.ascendingOrdered;
import static io.esastack.restlight.jaxrs.util.JaxrsUtils.descendingOrder;

public class DynamicFeatureAdapter implements HandlerConfigure {

    private final DeployContext<? extends RestlightOptions> context;
    private final List<Class<? extends Annotation>> appNameBindings;
    private final DynamicFeature[] features;
    private final ConfigurationImpl parent;

    public DynamicFeatureAdapter(DeployContext<? extends RestlightOptions> context,
                                 List<Class<? extends Annotation>> appNameBindings,
                                 List<DynamicFeature> features,
                                 ConfigurationImpl parent) {
        Checks.checkNotNull(parent, "parent");
        Checks.checkNotNull(context, "context");
        this.context = context;
        this.appNameBindings = appNameBindings;
        this.features = features == null || features.isEmpty() ? new DynamicFeature[0]
                : features.toArray(new DynamicFeature[0]);
        this.parent = parent;
    }

    @Override
    public void configure(HandlerMethod handlerMethod, ConfigurableHandler configurable) {
        DynamicFeatureConfiguration current = new DynamicFeatureConfiguration(parent);
        ResourceInfo resourceInfo = new ResourceInfoImpl(handlerMethod.beanType(), handlerMethod.method());

        // bind filters and interceptors dynamically.
        FeatureContext context = new DynamicFeatureContext(current);
        for (DynamicFeature feature : features) {
            feature.configure(resourceInfo, context);
        }

        ProvidersProxyFactoryExt providers = new ProvidersProxyFactoryExtImpl(this.context, current);
        // handle features
        for (ProxyComponent<Feature> feature : providers.featuresAddedDynamically()) {
            if (feature.proxied().configure(context)) {
                current.addEnabledFeature(feature.underlying());
            }
        }

        // add context resolvers, which should have higher precedence than those added at
        // JaxrsExtensionsHandler which have no providers and configuration corresponding
        // with current method.
        configurable.addContextResolver(new ConfigurationResolverAdapter(current) {
            @Override
            public int getOrder() {
                return 0;
            }
        });
        configurable.addContextResolver(new ProvidersResolverAdapter(new ProvidersImpl(providers)) {
            @Override
            public int getOrder() {
                return 0;
            }
        });

        // bound ReaderInterceptors(only be active when handler method has matched)
        configurable.addRequestEntityResolverAdvices(Collections.singleton(
                new ReaderInterceptorsAdapter(ascendingOrdered(filterByNameBindings(handlerMethod,
                        current, providers.readerInterceptors(), false))
                .toArray(new ReaderInterceptor[0]), true)));

        // bound WriterInterceptors(only be active when handler method has matched)
        configurable.addResponseEntityResolverAdvices(Collections.singleton(
                new WriterInterceptorsAdapter(ascendingOrdered(filterByNameBindings(handlerMethod,
                        current, providers.writerInterceptors(), false))
                .toArray(new WriterInterceptor[0]), true)));

        // handle bound postMatch ContainerRequestFilters (only apply to resource method)
        if (JaxrsMappingUtils.getMethod(handlerMethod.method()) != null) {
            configurable.addRouteFilters(Collections.singleton(new PostMatchRequestFilters(
                    ascendingOrdered(filterByNameBindings(handlerMethod, current, providers.requestFilters(),
                            true))
                    .toArray(new ContainerRequestFilter[0]))));
        }

        // handle bound ContainerResponseFilters (only apply to resource method)
        if (JaxrsMappingUtils.getMethod(handlerMethod.method()) != null) {
            configurable.addRouteFilters(Collections.singleton(new JaxrsResponseFilters(descendingOrder(
                    filterByNameBindings(handlerMethod, current, providers.responseFilters(), false))
                    .toArray(new ContainerResponseFilter[0]))));
        }
    }

    private <T> List<OrderComponent<T>> filterByNameBindings(HandlerMethod method,
                                                             DynamicFeatureConfiguration configuration,
                                                             Collection<ProxyComponent<T>> all,
                                                             boolean skipPreMatching) {
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        List<Class<? extends Annotation>> methodAnnotations = JaxrsUtils
                .findNameBindings(method.method(), true);
        List<OrderComponent<T>> bound = new LinkedList<>();
        for (ProxyComponent<T> component : all) {
            // NOTE: follow specification description, filers or interceptors added by DynamicFeature is
            // bound to current resource method.
            if (isAddedDynamically(component, configuration)) {
                bound.add(new OrderComponent<>(component.proxied(), JaxrsUtils.getOrder(component.underlying())));
            }
            if (skipPreMatching && JaxrsUtils.isPreMatched(component.underlying())) {
                continue;
            }
            List<Class<? extends Annotation>> annotations = JaxrsUtils.findNameBindings(
                    ClassUtils.getUserType(component.underlying()));
            if (appNameBindings.containsAll(annotations)
                    || methodAnnotations.containsAll(annotations)) {
                // globally
                bound.add(new OrderComponent<>(component.proxied(), JaxrsUtils.getOrder(component.underlying())));
            }
        }
        return bound;
    }

    private static <T> boolean isAddedDynamically(ProxyComponent<T> component,
                                                  DynamicFeatureConfiguration configuration) {
        Object target = component.underlying();
        for (Object instance : configuration.instancesAddedDynamically) {
            if (instance.equals(target)) {
                return true;
            }
        }
        for (Class<?> clazz : configuration.classesAddedDynamically) {
            if (clazz.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private static class DynamicFeatureConfiguration extends ConfigurationImpl {

        /**
         * NOTE: provider class or instance added by {@link DynamicFeature} is bound to particular resource methods.
         */
        private final List<Class<?>> classesAddedDynamically = new LinkedList<>();

        /**
         * NOTE: provider class or instance added by {@link DynamicFeature} is bound to particular resource methods.
         */
        private final List<Object> instancesAddedDynamically = new LinkedList<>();

        public DynamicFeatureConfiguration(ConfigurationImpl from) {
            super(from);
        }

        @Override
        public boolean addProviderInstance(Object instance, Map<Class<?>, Integer> contracts) {
            boolean success = super.addProviderInstance(instance, contracts);
            if (success) {
                this.instancesAddedDynamically.add(instance);
            }
            return success;
        }

        @Override
        public boolean addProviderClass(Class<?> clazz, Map<Class<?>, Integer> contracts) {
            boolean success = super.addProviderClass(clazz, contracts);
            if (success) {
                this.classesAddedDynamically.add(clazz);
            }
            return success;
        }
    }

    private interface ProvidersProxyFactoryExt extends ProvidersProxyFactory {

        /**
         * Obtains an immutable collection of {@link Feature}s, which are proxied to instantiate and inject
         * fields and properties when the feature is firstly invoked.
         *
         * @return  features
         */
        Collection<ProxyComponent<Feature>> featuresAddedDynamically();

    }

    private static class ProvidersProxyFactoryExtImpl extends ProvidersProxyFactoryImpl
            implements ProvidersProxyFactoryExt {

        private final DynamicFeatureConfiguration configuration;

        private ProvidersProxyFactoryExtImpl(DeployContext<? extends RestlightOptions> deployContext,
                                             DynamicFeatureConfiguration configuration) {
            super(deployContext, configuration);
            this.configuration = configuration;
        }

        @Override
        public Collection<ProxyComponent<Feature>> featuresAddedDynamically() {
            List<ProxyComponent<Feature>> features = new LinkedList<>();
            features.addAll(getFromClasses(configuration.classesAddedDynamically, Feature.class).values());
            features.addAll(getFromInstances(configuration.instancesAddedDynamically, Feature.class).values());
            return Collections.unmodifiableList(features);
        }
    }

}

