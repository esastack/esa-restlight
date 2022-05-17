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
package io.esastack.restlight.jaxrs.configure;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.Deployments;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.deploy.ExtensionsHandler;
import io.esastack.restlight.core.deploy.MiniConfigurableDeployments;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.util.ConstructorUtils;
import io.esastack.restlight.core.util.LoggerUtils;
import io.esastack.restlight.jaxrs.adapter.DynamicFeatureAdapter;
import io.esastack.restlight.jaxrs.adapter.JaxrsContextResolverFactory;
import io.esastack.restlight.jaxrs.adapter.JaxrsExceptionMapperAdapter;
import io.esastack.restlight.jaxrs.adapter.JaxrsResponseFiltersAdapter;
import io.esastack.restlight.jaxrs.adapter.MessageBodyReaderAdapter;
import io.esastack.restlight.jaxrs.adapter.MessageBodyWriterAdapter;
import io.esastack.restlight.jaxrs.adapter.PreMatchRequestFiltersAdapter;
import io.esastack.restlight.jaxrs.adapter.ProvidersPredicate;
import io.esastack.restlight.jaxrs.adapter.ReaderInterceptorsAdapter;
import io.esastack.restlight.jaxrs.adapter.StringConverterProviderAdapter;
import io.esastack.restlight.jaxrs.adapter.WriterInterceptorsAdapter;
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.impl.core.FeatureContextImpl;
import io.esastack.restlight.jaxrs.impl.ext.ProvidersImpl;
import io.esastack.restlight.jaxrs.impl.ext.RuntimeDelegateImpl;
import io.esastack.restlight.jaxrs.resolver.context.ApplicationResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.context.ConfigurationResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.context.ProvidersResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.param.ResourceContextParamResolver;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.esastack.restlight.jaxrs.util.JaxrsUtils.ascendingOrdered;
import static io.esastack.restlight.jaxrs.util.JaxrsUtils.descendingOrder;

/**
 * This extension is designed to handle the extensions which are added by {@link Deployments#addExtension(Object)}
 * and {@link Deployments#addExtensions(Collection)} before.
 */
@Internal
public class JaxrsExtensionsHandler implements ExtensionsHandler {

    private final ConfigurationImpl configuration = new ConfigurationImpl();
    private final MiniConfigurableDeployments deployments;
    private final ProvidersFactory factory;
    private final Providers providers;

    public JaxrsExtensionsHandler(MiniConfigurableDeployments deployments) {
        Checks.checkNotNull(deployments, "deployments");
        this.deployments = deployments;
        this.factory = new ProvidersFactoryImpl(deployments.deployContext(), configuration);
        this.providers = new ProvidersImpl(factory);
    }

    @Override
    public void handle(List<Object> extensions) {
        List<Object> handleableExtensions = new LinkedList<>();
        Object applicationObj = null;
        for (Object extension : extensions) {
            Class<?> userType = ClassUtils.getUserType(extension);
            if (JaxrsUtils.isComponent(userType) || JaxrsUtils.isRootResource(userType)) {
                handleableExtensions.add(extension);
            }
            if (Application.class.isAssignableFrom(userType)) {
                applicationObj = extension;
            }
        }

        ProxyComponent<Application> application = null;
        if (applicationObj != null) {
            application = getOrInstantiate(applicationObj, deployments, configuration, providers);
        }

        if (application != null) {
            handleApplication(application, deployments, configuration, handleableExtensions);
        }
        // be different from same resolvers added at DynamicFeatureAdapter, you can think
        // the between two as fallback which have no configurations corresponding with specified resource method.
        // eg. if there are some providers and configurations added by DynamicFeatures, then those
        // information can't be known here.
        deployments.addContextResolver(new ConfigurationResolverAdapter(configuration));
        deployments.addContextResolver(new ProvidersResolverAdapter(providers));

        deployments.addParamResolver(new ResourceContextParamResolver(deployments.deployContext()));

        convertThenAddExtensions(configuration, deployments, handleableExtensions);

        Set<Class<? extends Annotation>> appNameBindings;
        if (application == null) {
            appNameBindings = Collections.emptySet();
        } else {
            appNameBindings = JaxrsUtils.findNameBindings(ClassUtils.getUserType(application.underlying()));
        }
        this.convertThenAddProviders(appNameBindings, factory, configuration, providers, deployments);
    }

    void handleApplication(ProxyComponent<Application> application,
                           MiniConfigurableDeployments deployments,
                           ConfigurationImpl configuration,
                           List<Object> extensions) {
        ApplicationPath pathAnn = AnnotationUtils.findAnnotation(ClassUtils
                .getUserType(application.underlying()), ApplicationPath.class);
        if (pathAnn != null) {
            /*
             * NOTE: If the external context-path is empty, try to use the value defined in @ApplicationPath.
             */
            RestlightOptions options = deployments.deployContext().options();
            String contextPath = options.getContextPath();
            String annPath = pathAnn.value();
            if (StringUtils.isEmpty(contextPath)) {
                if (StringUtils.isNotEmpty(annPath)) {
                    options.setContextPath(annPath);
                }
            } else if (StringUtils.isNotEmpty(annPath)) {
                LoggerUtils.logger().warn("The path:[{}] defined in @ApplicationPath has been discarded" +
                        " caused by the existed context-path:[{}]", annPath, contextPath);
            }
        }
        Set<Class<?>> classes = application.proxied().getClasses();
        if (classes != null) {
            extensions.addAll(classes);
        }
        Set<Object> singletons = application.proxied().getSingletons();
        if (singletons != null) {
            extensions.addAll(singletons);
        }
        Map<String, Object> properties = application.proxied().getProperties();
        if (properties != null) {
            properties.forEach(configuration::setProperty);
        }
        deployments.addContextResolver(new ApplicationResolverAdapter(application.proxied()));
    }

    void convertThenAddExtensions(ConfigurationImpl configuration,
                                  MiniConfigurableDeployments deployments,
                                  List<Object> extensions) {
        for (Object extension : extensions) {
            final Class<?> userType = ClassUtils.getUserType(extension);
            final boolean isClazz = (userType == extension);
            if (JaxrsUtils.isRootResource(userType)) {
                if (isClazz) {
                    configuration.addResourceClass(userType);
                    deployments.addController(userType, false);
                } else {
                    configuration.addResourceInstance(extension);
                    deployments.addController(extension);
                }
            } else {
                if (isClazz) {
                    configuration.addProviderClass(userType, JaxrsUtils.extractContracts(userType,
                            JaxrsUtils.defaultOrder()));
                } else {
                    // handle HeaderDelegate and HeaderDelegateFactory here.
                    if (extension instanceof RuntimeDelegate.HeaderDelegate) {
                        RuntimeDelegateImpl.addHeaderDelegate((RuntimeDelegate.HeaderDelegate<?>) extension);
                        continue;
                    }
                    if (extension instanceof HeaderDelegateFactory) {
                        RuntimeDelegateImpl.addHeaderDelegateFactory((HeaderDelegateFactory) extension);
                        continue;
                    }
                    configuration.addProviderInstance(extension, JaxrsUtils.extractContracts(extension,
                            JaxrsUtils.defaultOrder()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    void convertThenAddProviders(Set<Class<? extends Annotation>> appNameBindings,
                                 ProvidersFactory factory,
                                 ConfigurationImpl configuration,
                                 Providers providers,
                                 MiniConfigurableDeployments deployments) {
        Collection<ProxyComponent<Feature>> features = factory.features();
        if (!features.isEmpty()) {
            ConfigurableImpl configurable = new ConfigurableImpl(configuration);
            for (ProxyComponent<Feature> feature : features) {
                FeatureContext context = new FeatureContextImpl(ClassUtils.getUserType(feature.underlying()),
                        configurable);
                if (feature.proxied().configure(context)) {
                    configuration.addEnabledFeature(feature.underlying());
                }
            }
        }

        deployments.addParamResolver(new MessageBodyReaderAdapter<>(providers));
        deployments.addResponseEntityResolver(new MessageBodyWriterAdapter<>(providers));
        for (ProxyComponent<ExceptionMapper<Throwable>> mapper : factory.exceptionMappers()) {
            deployments.addExceptionResolver((Class<Throwable>) ClassUtils.findFirstGenericType(
                    ClassUtils.getUserType(mapper.underlying())).orElse(Throwable.class),
                    new JaxrsExceptionMapperAdapter<>(mapper));
        }
        deployments.addParamResolver(new JaxrsContextResolverFactory(providers));
        for (ProxyComponent<ParamConverterProvider> provider : factory.paramConverterProviders()) {
            deployments.addStringConverter(new StringConverterProviderAdapter(provider.proxied(),
                    JaxrsUtils.getOrder(provider.underlying())));
        }

        this.convertThenAddFilters(appNameBindings, deployments, factory);
        this.convertThenAddInterceptors(appNameBindings, deployments, factory);
        deployments.addHandlerConfigure(new DynamicFeatureAdapter(deployments.deployContext(), appNameBindings,
                factory.dynamicFeatures(), configuration));
    }

    void convertThenAddFilters(Set<Class<? extends Annotation>> appNameBindings,
                               MiniConfigurableDeployments deployments,
                               ProvidersFactory factory) {
        // convert @PreMatching ContainerRequestFilters
        List<OrderComponent<ContainerRequestFilter>> reqFilters = new LinkedList<>();
        for (ProxyComponent<ContainerRequestFilter> filter : factory.requestFilters()) {
            if (JaxrsUtils.isPreMatched(filter.underlying())) {
                reqFilters.add(new OrderComponent<>(filter.proxied(), JaxrsUtils.getOrder(filter.underlying())));
            }
        }
        if (!reqFilters.isEmpty()) {
            deployments.addFilter(new PreMatchRequestFiltersAdapter(ascendingOrdered(reqFilters)
                    .toArray(new ContainerRequestFilter[0])));
        }

        // convert global ContainerResponseFilters
        List<OrderComponent<ContainerResponseFilter>> rspFilters = new LinkedList<>();
        for (ProxyComponent<ContainerResponseFilter> filter : factory.responseFilters()) {
            if (isGlobalComponent(appNameBindings, filter.underlying())) {
                rspFilters.add(new OrderComponent<>(filter.proxied(), JaxrsUtils.getOrder(filter.underlying())));
            }
        }
        deployments.addFilter(new JaxrsResponseFiltersAdapter(descendingOrder(rspFilters)
                .toArray(new ContainerResponseFilter[0])));
    }

    void convertThenAddInterceptors(Set<Class<? extends Annotation>> appNameBindings,
                                    MiniConfigurableDeployments deployments,
                                    ProvidersFactory factory) {
        // covert ReaderInterceptors which can apply to all methods(even if it's null).
        List<OrderComponent<ReaderInterceptor>> readInterceptors = new LinkedList<>();
        for (ProxyComponent<ReaderInterceptor> interceptor : factory.readerInterceptors()) {
            if (isGlobalComponent(appNameBindings, interceptor.underlying())) {
                readInterceptors.add(new OrderComponent<>(interceptor.proxied(),
                        JaxrsUtils.getOrder(interceptor.underlying())));
            }
        }
        if (!readInterceptors.isEmpty()) {
            deployments.addParamResolverAdvice(new ReaderInterceptorsAdapter(ascendingOrdered(
                    readInterceptors).toArray(new ReaderInterceptor[0]), ProvidersPredicate.BINDING_GLOBAL));
        }

        // convert WriterInterceptors which can apply to all methods(even if it's null).
        List<OrderComponent<WriterInterceptor>> writerInterceptors = new LinkedList<>();
        for (ProxyComponent<WriterInterceptor> interceptor : factory.writerInterceptors()) {
            if (isGlobalComponent(appNameBindings, interceptor.underlying())) {
                writerInterceptors.add(new OrderComponent<>(interceptor.proxied(),
                        JaxrsUtils.getOrder(interceptor.underlying())));
            }
        }
        if (!writerInterceptors.isEmpty()) {
            deployments.addResponseEntityResolverAdvice(new WriterInterceptorsAdapter(ascendingOrdered(
                    writerInterceptors).toArray(new WriterInterceptor[0]), ProvidersPredicate.BINDING_GLOBAL));
        }
    }

    ProxyComponent<Application> getOrInstantiate(Object object, MiniConfigurableDeployments deployments,
                                                 ConfigurationImpl configuration,
                                                 Providers providers) {
        if (object instanceof Application) {
            return new ProxyComponent<>(object, (Application) object);
        }
        Class<?> userType = ClassUtils.getUserType(object);
        if (!deployments.deployContext().paramPredicate().isPresent()) {
            throw new IllegalStateException("Failed to instantiate class: [" + userType + "], because there is" +
                    " no [" + ResolvableParamPredicate.class + "] exist!");
        }
        Constructor<?> constructor = ConstructorUtils.extractResolvable(userType, deployments.deployContext()
                .paramPredicate().get());
        if (constructor == null) {
            throw new IllegalStateException("There is no suitable constructor to instantiate class: "
                    + userType.getName());
        }

        Object[] args = new Object[constructor.getParameterCount()];
        int index = 0;
        for (Parameter parameter : constructor.getParameters()) {
            if (!parameter.isAnnotationPresent(Context.class)) {
                throw new IllegalStateException("Failed to instantiate class: [" + userType + "] caused by" +
                        " unresolvable parameter: [" + parameter.getName() + "], maybe @Context is absent?");
            }
            if (Configuration.class.isAssignableFrom(parameter.getType())) {
                args[index++] = configuration;
            } else if (Providers.class.isAssignableFrom(parameter.getType())) {
                args[index++] = providers;
            } else {
                throw new IllegalStateException("Failed to instantiate class: [" + userType + "] caused by" +
                        " unsupported parameter: [" + parameter.getName() + "], only Application and Providers are" +
                        " supported!");
            }
        }
        try {
            return new ProxyComponent<>(object, (Application) constructor.newInstance(args));
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("Failed to instantiate class: [" + userType + "]",
                    ex.getTargetException());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate class: [" + userType + "]", ex);
        }
    }

    private static boolean isGlobalComponent(Set<Class<? extends Annotation>> appNameBindings, Object target) {
        Set<Class<? extends Annotation>> targetNameBindings = JaxrsUtils
                .findNameBindings(ClassUtils.getUserType(target));
        return targetNameBindings.isEmpty() || appNameBindings.containsAll(targetNameBindings);
    }

}

