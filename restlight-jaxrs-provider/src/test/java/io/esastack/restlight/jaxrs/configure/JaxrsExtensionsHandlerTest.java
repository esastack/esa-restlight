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
package io.esastack.restlight.jaxrs.configure;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.deploy.HandlerConfigure;
import io.esastack.restlight.core.deploy.MiniConfigurableDeployments;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.jaxrs.adapter.DynamicFeatureAdapter;
import io.esastack.restlight.jaxrs.adapter.JaxrsContextResolverFactory;
import io.esastack.restlight.jaxrs.adapter.JaxrsExceptionMapperAdapter;
import io.esastack.restlight.jaxrs.adapter.JaxrsResponseFiltersAdapter;
import io.esastack.restlight.jaxrs.adapter.MessageBodyReaderAdapter;
import io.esastack.restlight.jaxrs.adapter.MessageBodyWriterAdapter;
import io.esastack.restlight.jaxrs.adapter.StringConverterProviderAdapter;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.resolver.context.ApplicationResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.context.ConfigurationResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.context.ProvidersResolverAdapter;
import io.esastack.restlight.jaxrs.resolver.param.ResourceContextParamResolver;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import io.esastack.restlight.core.filter.Filter;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsExtensionsHandlerTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new JaxrsExtensionsHandler(null));
        assertThrows(NullPointerException.class,
                () -> new JaxrsExtensionsHandler(mock(MiniConfigurableDeployments.class)));
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        when(deployments.deployContext()).thenReturn(mock(DeployContext.class));
        assertDoesNotThrow(() -> new JaxrsExtensionsHandler(deployments));
    }

    @Test
    void testHandle() {
        final List<ContextResolverAdapter> contextResolvers = new LinkedList<>();
        final List<ParamResolverFactory> paramResolvers = new LinkedList<>();
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);
        when(deployContext.paramPredicate()).thenReturn(Optional.of(param -> true));
        when(deployments.addContextResolver(any(ContextResolverAdapter.class))).thenAnswer(invocationOnMock -> {
            contextResolvers.add(invocationOnMock.getArgument(0));
            return null;
        });
        when(deployments.addParamResolver(any(ParamResolverFactory.class))).thenAnswer(invocationOnMock -> {
            paramResolvers.add(invocationOnMock.getArgument(0));
            return null;
        });

        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        List<Object> extensions = new LinkedList<>();
        extensions.add(ApplicationImpl.class);
        extensions.add(HelloResource.class);
        extensions.add(RequestFilter.class);

        handler.handle(extensions);
        assertEquals(3, contextResolvers.size());
        assertTrue(contextResolvers.get(0) instanceof ApplicationResolverAdapter);
        assertTrue(contextResolvers.get(1) instanceof ConfigurationResolverAdapter);
        assertTrue(contextResolvers.get(2) instanceof ProvidersResolverAdapter);
        assertEquals(2, paramResolvers.size());
        assertTrue(paramResolvers.get(0) instanceof ResourceContextParamResolver);
        assertTrue(paramResolvers.get(1) instanceof JaxrsContextResolverFactory);
    }

    @Test
    void testHandleApplication() {
        final RestlightOptions options = new RestlightOptions();
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final List<Object> extensions = new LinkedList<>();
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployContext.options()).thenReturn(options);
        when(deployments.deployContext()).thenReturn(deployContext);

        final List<ContextResolverAdapter> contextResolvers = new LinkedList<>();
        when(deployments.addContextResolver(any(ContextResolverAdapter.class))).thenAnswer(invocationOnMock -> {
            contextResolvers.add(invocationOnMock.getArgument(0));
            return null;
        });

        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        handler.handleApplication(new ProxyComponent<>(ApplicationImpl1.class, new ApplicationImpl1()),
                deployments, configuration, extensions);

        assertEquals("/abc/def", options.getContextPath());
        assertEquals(1, contextResolvers.size());
        assertTrue(contextResolvers.get(0) instanceof ApplicationResolverAdapter);
        assertEquals(1, configuration.getProperties().size());
        assertEquals("value", configuration.getProperty("name"));
        assertEquals(2, extensions.size());
        assertEquals(HelloResource.class, extensions.get(0));
        assertTrue(extensions.get(1) instanceof RequestFilter);

        options.setContextPath("/xyz");
        handler.handleApplication(new ProxyComponent<>(ApplicationImpl1.class, new ApplicationImpl1()),
                deployments, configuration, extensions);
    }

    @Test
    void testConvertThenAddExtensions() {
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);

        final ConfigurationImpl configuration = new ConfigurationImpl();
        final List<Object> extensions = new LinkedList<>();
        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        final HelloResource resource = new HelloResource();
        extensions.add(resource);
        extensions.add(HelloResource1.class);
        final RequestFilter filter = new RequestFilter();
        extensions.add(filter);
        extensions.add(RequestFilter1.class);

        final RuntimeDelegate.HeaderDelegate<HelloResource> delegate =
                new RuntimeDelegate.HeaderDelegate<HelloResource>() {
            @Override
            public HelloResource fromString(String value) {
                return null;
            }

            @Override
            public String toString(HelloResource value) {
                return null;
            }
        };

        final HeaderDelegateFactory delegateFactory = () -> new RuntimeDelegate.HeaderDelegate<HelloResource1>() {
            @Override
            public HelloResource1 fromString(String value) {
                return null;
            }

            @Override
            public String toString(HelloResource1 value) {
                return null;
            }
        };

        extensions.add(delegate);
        extensions.add(delegateFactory);
        final List<Object> controllers = new LinkedList<>();
        when(deployments.addController(any())).thenAnswer(invocationOnMock -> {
            controllers.add(invocationOnMock.getArgument(0));
            return null;
        });
        when(deployments.addController(any(), anyBoolean())).thenAnswer(invocationOnMock -> {
            controllers.add(invocationOnMock.getArguments()[0]);
            return null;
        });
        handler.convertThenAddExtensions(configuration, deployments, extensions);
        assertEquals(2, controllers.size());
        assertEquals(resource, controllers.get(0));
        assertEquals(HelloResource1.class, controllers.get(1));

        assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(HelloResource.class));
        assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(HelloResource1.class));
        assertEquals(2, configuration.getClasses().size());
        assertTrue(configuration.getClasses().contains(HelloResource1.class));
        assertEquals(2, configuration.getInstances().size());
        assertTrue(configuration.getInstances().contains(resource));

        assertEquals(1, configuration.getProviderClasses().size());
        assertTrue(configuration.getProviderClasses().contains(RequestFilter1.class));
        assertEquals(1, configuration.getProviderInstances().size());
        assertTrue(configuration.getProviderInstances().contains(filter));
    }

    @Test
    void testConvertThenAddProviders() {
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);

        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        final ProvidersFactory factory = mock(ProvidersFactory.class);
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final Providers providers = mock(Providers.class);
        final Feature feature = context -> true;
        when(factory.features()).thenReturn(Collections.singletonList(new ProxyComponent<>(feature, feature)));
        final ExceptionMapper<RuntimeException> exceptionMapper = new ExceptionMapper<RuntimeException>() {
            @Override
            public Response toResponse(RuntimeException exception) {
                return null;
            }
        };
        when(factory.exceptionMappers()).thenReturn(Collections.singleton(new ProxyComponent(exceptionMapper,
                exceptionMapper)));
        final ParamConverterProvider paramConverter = new ParamConverterProvider() {
            @Override
            public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
                return null;
            }
        };
        when(factory.paramConverterProviders()).thenReturn(Collections.singleton(
                new ProxyComponent<>(paramConverter, paramConverter)));

        final List<RequestEntityResolverAdapter> requestResolvers = new LinkedList<>();
        final List<ResponseEntityResolverAdapter> responseResolvers = new LinkedList<>();
        final AtomicReference<Class<?>> exceptionClass = new AtomicReference<>();
        final AtomicReference<ExceptionResolver<?>> exceptionResolver = new AtomicReference<>();
        final List<ParamResolverFactory> paramResolvers = new LinkedList<>();
        final List<StringConverterFactory> stringConverters = new LinkedList<>();
        final List<HandlerConfigure> configures = new LinkedList<>();
        when(deployments.addRequestEntityResolver(any(RequestEntityResolverAdapter.class)))
                .thenAnswer(invocationOnMock -> {
                    requestResolvers.add((RequestEntityResolverAdapter) invocationOnMock.getArguments()[0]);
                    return null;
                });
        when(deployments.addResponseEntityResolver(any(ResponseEntityResolverAdapter.class)))
                .thenAnswer(invocationOnMock -> {
                    responseResolvers.add((ResponseEntityResolverAdapter) invocationOnMock.getArguments()[0]);
                    return null;
                });
        when(deployments.addExceptionResolver(any(), any())).thenAnswer(invocationOnMock -> {
            exceptionClass.set((Class<?>) invocationOnMock.getArguments()[0]);
            exceptionResolver.set((ExceptionResolver<?>) invocationOnMock.getArguments()[1]);
            return null;
        });
        when(deployments.addParamResolver(any(ParamResolverFactory.class)))
                .thenAnswer(invocationOnMock -> {
                    paramResolvers.add((ParamResolverFactory) invocationOnMock.getArguments()[0]);
                    return null;
                });
        when(deployments.addStringConverter(any(StringConverterFactory.class)))
                .thenAnswer(invocationOnMock -> {
                    stringConverters.add((StringConverterFactory) invocationOnMock.getArguments()[0]);
                    return null;
                });
        when(deployments.addHandlerConfigure(any())).thenAnswer(invocationOnMock -> {
            configures.add((HandlerConfigure) invocationOnMock.getArguments()[0]);
            return null;
        });

        handler.convertThenAddProviders(Collections.emptySet(), factory, configuration, providers, deployments);
        assertTrue(configuration.isEnabled(feature));
        assertEquals(1, requestResolvers.size());
        assertTrue(requestResolvers.get(0) instanceof MessageBodyReaderAdapter);
        assertEquals(1, responseResolvers.size());
        assertTrue(responseResolvers.get(0) instanceof MessageBodyWriterAdapter);
        assertEquals(RuntimeException.class, exceptionClass.get());
        assertTrue(exceptionResolver.get() instanceof JaxrsExceptionMapperAdapter);
        assertEquals(1, paramResolvers.size());
        assertTrue(paramResolvers.get(0) instanceof JaxrsContextResolverFactory);
        assertEquals(1, stringConverters.size());
        assertTrue(stringConverters.get(0) instanceof StringConverterProviderAdapter);
        assertEquals(1, configures.size());
        assertTrue(configures.get(0) instanceof DynamicFeatureAdapter);
    }

    @Test
    void testConvertThenAddFilters() {
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);

        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        final ProvidersFactory factory = mock(ProvidersFactory.class);
        when(factory.requestFilters()).thenReturn(Collections.emptyList());
        when(factory.responseFilters()).thenReturn(Collections.emptyList());
        final List<Filter> filters = new LinkedList<>();
        when(deployments.addFilter(any(Filter.class))).thenAnswer(invocationOnMock -> {
            filters.add(invocationOnMock.getArgument(0));
            return null;
        });
        handler.convertThenAddFilters(Collections.emptySet(), deployments, factory);
        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof JaxrsResponseFiltersAdapter);

        filters.clear();

        final List<ProxyComponent<ContainerRequestFilter>> requestFilters = new LinkedList<>();
        final List<ProxyComponent<ContainerResponseFilter>> responseFilters = new LinkedList<>();
        requestFilters.add(new ProxyComponent<>(RequestFilter.class, new RequestFilter()));
        requestFilters.add(new ProxyComponent<>(RequestFilter1.class, new RequestFilter1()));
        responseFilters.add(new ProxyComponent<>(ResponseFilter.class, new ResponseFilter()));
        responseFilters.add(new ProxyComponent<>(ResponseFilter1.class, new ResponseFilter1()));
        when(factory.requestFilters()).thenReturn(requestFilters);
        when(factory.responseFilters()).thenReturn(responseFilters);

        final Set<Class<? extends Annotation>> appNameBindings = new HashSet<>();
        appNameBindings.add(NameBinding1.class);
        appNameBindings.add(NameBinding2.class);
        appNameBindings.add(NameBinding3.class);

        handler.convertThenAddFilters(appNameBindings, deployments, factory);
        assertEquals(2, filters.size());
    }

    @Test
    void testConvertThenAddInterceptors() {
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);

        final Set<Class<? extends Annotation>> appNameBindings = new HashSet<>();
        appNameBindings.add(NameBinding1.class);
        appNameBindings.add(NameBinding2.class);
        appNameBindings.add(NameBinding3.class);

        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        final ProvidersFactory factory = mock(ProvidersFactory.class);
        when(factory.readerInterceptors()).thenReturn(Collections.emptyList());
        when(factory.writerInterceptors()).thenReturn(Collections.emptyList());

        final List<RequestEntityResolverAdviceAdapter> requestEntityResolverAdvices = new LinkedList<>();
        final List<ResponseEntityResolverAdviceAdapter> responseEntityResolverAdvices = new LinkedList<>();
        when(deployments.addRequestEntityResolverAdvice(any(RequestEntityResolverAdviceAdapter.class)))
                .thenAnswer(invocationOnMock -> {
                    requestEntityResolverAdvices.add(invocationOnMock.getArgument(0));
                    return null;
                });
        when(deployments.addResponseEntityResolverAdvice(any(ResponseEntityResolverAdviceAdapter.class)))
                .thenAnswer(invocationOnMock -> {
                    responseEntityResolverAdvices.add(invocationOnMock.getArgument(0));
                    return null;
                });

        handler.convertThenAddFilters(appNameBindings, deployments, factory);
        assertEquals(0, requestEntityResolverAdvices.size());
        assertEquals(0, responseEntityResolverAdvices.size());

        final List<ProxyComponent<jakarta.ws.rs.ext.ReaderInterceptor>> readerInterceptors = new LinkedList<>();
        readerInterceptors.add(new ProxyComponent<>(ReaderInterceptor.class, new ReaderInterceptor()));
        readerInterceptors.add(new ProxyComponent<>(ReaderInterceptor1.class, new ReaderInterceptor1()));
        when(factory.readerInterceptors()).thenReturn(readerInterceptors);

        final List<ProxyComponent<jakarta.ws.rs.ext.WriterInterceptor>> writerInterceptors = new LinkedList<>();
        writerInterceptors.add(new ProxyComponent<>(WriterInterceptor.class, new WriterInterceptor()));
        writerInterceptors.add(new ProxyComponent<>(WriterInterceptor1.class, new WriterInterceptor1()));
        when(factory.writerInterceptors()).thenReturn(writerInterceptors);
        handler.convertThenAddInterceptors(appNameBindings, deployments, factory);
        assertEquals(1, requestEntityResolverAdvices.size());
        assertEquals(1, responseEntityResolverAdvices.size());
    }

    @Test
    void testGetOrInstantiate() {
        final MiniConfigurableDeployments deployments = mock(MiniConfigurableDeployments.class);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployments.deployContext()).thenReturn(deployContext);

        final ConfigurationImpl configuration = new ConfigurationImpl();
        final Providers providers = mock(Providers.class);
        final JaxrsExtensionsHandler handler = new JaxrsExtensionsHandler(deployments);
        final Application application = new ApplicationImpl();
        assertSame(application,
                handler.getOrInstantiate(application, deployments, configuration, providers).underlying());
        assertSame(application,
                handler.getOrInstantiate(application, deployments, configuration, providers).proxied());

        when(deployContext.paramPredicate()).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> handler.getOrInstantiate(ApplicationImpl1.class, deployments, configuration, providers));

        when(deployContext.paramPredicate()).thenReturn(Optional.of(param -> false));
        assertThrows(IllegalStateException.class,
                () -> handler.getOrInstantiate(ApplicationImpl2.class, deployments, configuration, providers));

        when(deployContext.paramPredicate()).thenReturn(Optional.of(param -> true));
        ProxyComponent<Application> proxied = handler.getOrInstantiate(ApplicationImpl3.class,
                deployments, configuration, providers);
        assertEquals(ApplicationImpl3.class, proxied.underlying());
        assertSame(configuration, ((ApplicationImpl3) proxied.proxied()).configuration);
        assertSame(providers, ((ApplicationImpl3) proxied.proxied()).providers);
    }

    private static final class ApplicationImpl extends Application {

        public ApplicationImpl() {
        }

    }

    @ApplicationPath("/abc/def")
    private static final class ApplicationImpl1 extends Application {
        public ApplicationImpl1() {
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(HelloResource.class);
        }

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new RequestFilter());
        }

        @Override
        public Map<String, Object> getProperties() {
            return Collections.singletonMap("name", "value");
        }
    }

    private static final class ApplicationImpl2 extends Application {
        public ApplicationImpl2(@Context Providers providers) {

        }
    }

    private static final class ApplicationImpl3 extends Application {

        private final Configuration configuration;
        private final Providers providers;

        public ApplicationImpl3(@Context Configuration configuration, @Context Providers providers) {
            this.configuration = configuration;
            this.providers = providers;
        }
    }

    @Path("hello")
    private static final class HelloResource {

        @Path("say")
        public String sayHello() {
            return "Hello!";
        }
    }

    @Path("hello1")
    private static final class HelloResource1 {
        @Path("say")
        public String sayHello() {
            return "Hello!";
        }
    }

    @PreMatching
    private static final class RequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    private static final class RequestFilter1 implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }
    }

    @NameBinding1
    @NameBinding2
    private static final class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class ResponseFilter1 implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }
    }

    @NameBinding1
    @NameBinding2
    private static final class ReaderInterceptor implements jakarta.ws.rs.ext.ReaderInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class ReaderInterceptor1 implements jakarta.ws.rs.ext.ReaderInterceptor {
        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }
    }

    @NameBinding1
    @NameBinding2
    private static final class WriterInterceptor implements jakarta.ws.rs.ext.WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }

    @NameBinding1
    @NameBinding2
    @NameBinding3
    private static final class WriterInterceptor1 implements jakarta.ws.rs.ext.WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

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

