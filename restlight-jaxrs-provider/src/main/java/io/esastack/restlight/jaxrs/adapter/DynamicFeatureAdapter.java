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
import io.esastack.restlight.jaxrs.configure.ProvidersFactory;
import io.esastack.restlight.jaxrs.configure.ProvidersFactoryImpl;
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
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static io.esastack.restlight.jaxrs.util.JaxrsUtils.ascendingOrdered;
import static io.esastack.restlight.jaxrs.util.JaxrsUtils.descendingOrder;

public class DynamicFeatureAdapter implements HandlerConfigure {

    private final DeployContext<? extends RestlightOptions> context;
    private final List<Class<? extends Annotation>> appNameBindings;
    private final Collection<ProxyComponent<DynamicFeature>> features;
    private final ConfigurationImpl parent;

    public DynamicFeatureAdapter(DeployContext<? extends RestlightOptions> context,
                                 List<Class<? extends Annotation>> appNameBindings,
                                 Collection<ProxyComponent<DynamicFeature>> features,
                                 ConfigurationImpl parent) {
        Checks.checkNotNull(parent, "parent");
        Checks.checkNotNull(context, "context");
        this.context = context;
        this.appNameBindings = appNameBindings;
        this.features = features == null || features.isEmpty()
                ? Collections.emptyList() : Collections.unmodifiableCollection(features);
        this.parent = parent;
    }

    @Override
    public void configure(HandlerMethod handlerMethod, ConfigurableHandler configurable) {
        ConfigurationImpl current = new ConfigurationImpl(parent);
        ResourceInfo resourceInfo = new ResourceInfoImpl(handlerMethod.beanType(), handlerMethod.method());

        // bind filters and interceptors dynamically.
        for (ProxyComponent<DynamicFeature> feature : features) {
            feature.proxied().configure(resourceInfo,
                    new DynamicFeatureContext(ClassUtils.getUserType(feature.underlying()), current));
        }

        ProvidersFactory providers = new ProvidersFactoryImpl(this.context, current);
        // bound ReaderInterceptors(only be active when handler method has matched)
        List<OrderComponent<ReaderInterceptor>> readerInterceptors = filterByNameBindings(handlerMethod,
                providers.readerInterceptors(), false);
        if (!readerInterceptors.isEmpty()) {
            configurable.addRequestEntityResolverAdvices(Collections.singleton(
                    new ReaderInterceptorsAdapter(ascendingOrdered(readerInterceptors)
                            .toArray(new ReaderInterceptor[0]), ProvidersPredicate.BINDING_HANDLER)));
        }

        // bound WriterInterceptors(only be active when handler method has matched)
        List<OrderComponent<WriterInterceptor>> writerInterceptors = filterByNameBindings(handlerMethod,
                providers.writerInterceptors(), false);
        if (!writerInterceptors.isEmpty()) {
            configurable.addResponseEntityResolverAdvices(Collections.singleton(
                    new WriterInterceptorsAdapter(ascendingOrdered(writerInterceptors)
                            .toArray(new WriterInterceptor[0]), ProvidersPredicate.BINDING_HANDLER)));
        }

        // handle bound postMatch ContainerRequestFilters (only apply to resource method)
        if (JaxrsMappingUtils.isMethod(handlerMethod.method())) {
            List<OrderComponent<ContainerRequestFilter>> filters =
                    filterByNameBindings(handlerMethod, providers.requestFilters(),
                    true);
            if (!filters.isEmpty()) {
                configurable.addRouteFilters(Collections.singleton(new PostMatchRequestFiltersAdapter(
                        ascendingOrdered(filters).toArray(new ContainerRequestFilter[0]))));
            }
        }

        // handle bound ContainerResponseFilters (only apply to resource method)
        if (JaxrsMappingUtils.isMethod(handlerMethod.method())) {
            ContainerResponseFilter[] filters = descendingOrder(
                    filterByNameBindings(handlerMethod, providers.responseFilters(), false))
                    .toArray(new ContainerResponseFilter[0]);
            configurable.addRouteFilters(Collections.singleton(
                    new JaxrsResponseFiltersAdapter.ContainerResponseFilterBinder(filters)));
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
    }

    private <T> List<OrderComponent<T>> filterByNameBindings(HandlerMethod method,
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
}

