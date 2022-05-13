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
package io.esastack.restlight.jaxrs.resolver.param;

import esa.commons.StringUtils;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.deploy.HandlerConfigure;
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.locator.HandlerValueResolverLocator;
import io.esastack.restlight.core.locator.MappingLocator;
import io.esastack.restlight.core.locator.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactoryImpl;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.exception.ExceptionMapper;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.spi.impl.DefaultStringConverterFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.route.RouteRegistry;
import io.esastack.restlight.core.route.predicate.PatternsPredicate;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanParamResolverTest {

    private static final BeanParamResolver resolverFactory = new BeanParamResolver(new Context());

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/baz;qux=qux")
                .withCookie("foo", "foo")
                .withHeader("bar", "bar")
                .withParameter("zet", "zet")
                .build();
        final Bean resolved = (Bean) createResolverAndResolve(request, "method");
        assertEquals("foo", resolved.foo);
        assertEquals("bar", resolved.bar);
        assertEquals("baz", resolved.baz);
        assertEquals("qux", resolved.qux);
        assertEquals("zet", resolved.zet);
        assertNull(resolved.other);
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                param.method(), StringUtils.empty()).get().path()).test(context);
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverters(param), null);
        return resolver.resolve(new ParamResolverContextImpl(context));
    }

    private static class Subject {

        @GET
        @Path("/{baz}")
        public void method(@BeanParam Bean pojo) {
        }
    }

    private static class Bean {
        @CookieParam("foo")
        private String foo;
        @HeaderParam("bar")
        private String bar;

        @PathParam("baz")
        private String baz;
        @MatrixParam("qux")
        private String qux;
        @QueryParam("zet")
        private String zet;
        private String other;
    }

    private static class Context implements DeployContext {

        @Override
        public Attributes attrs() {
            return null;
        }

        @Override
        public Optional<List<Object>> singletonControllers() {
            return Optional.empty();
        }

        @Override
        public Optional<List<Class<?>>> prototypeControllers() {
            return Optional.empty();
        }

        @Override
        public Optional<List<Object>> extensions() {
            return Optional.empty();
        }

        @Override
        public Optional<RouteMethodLocator> methodLocator() {
            return Optional.empty();
        }

        @Override
        public Optional<HandlerValueResolverLocator> handlerResolverLocator() {
            return Optional.empty();
        }

        @Override
        public Optional<ResolvableParamPredicate> paramPredicate() {
            return Optional.empty();
        }

        @Override
        public Optional<List<HandlerConfigure>> handlerConfigures() {
            return Optional.empty();
        }

        @Override
        public Optional<HandlerFactory> handlerFactory() {
            return Optional.empty();
        }

        @Override
        public Optional<HandlerContextProvider> handlerContexts() {
            return Optional.empty();
        }

        @Override
        public Optional<Handlers> handlers() {
            return Optional.empty();
        }

        @Override
        public Optional<List<Object>> advices() {
            return Optional.empty();
        }

        @Override
        public Optional<List<InterceptorFactory>> interceptors() {
            return Optional.empty();
        }

        @Override
        public Optional<List<ExceptionMapper>> exceptionMappers() {
            return Optional.empty();
        }

        @Override
        public Optional<HandlerResolverFactory> resolverFactory() {
            return Optional.of(new HandlerResolverFactoryImpl(
                    Collections.singletonList(new JacksonHttpBodySerializer()),
                    Collections.singletonList(new JacksonHttpBodySerializer()),
                    null,
                    null,
                    null,
                    Collections.singletonList(new DefaultStringConverterFactory()),
                    null,
                    Arrays.asList(new CookieValueParamResolver(),
                            new RequestHeaderParamResolver(),
                            new MatrixVariableParamResolver(),
                            new PathParamResolver(),
                            new QueryParamResolver()),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null));
        }

        @Override
        public Optional<HandlerAdvicesFactory> handlerAdvicesFactory() {
            return Optional.empty();
        }

        @Override
        public String name() {
            return Constants.SERVER;
        }

        @Override
        public RestlightOptions options() {
            return null;
        }

        @Override
        public Map<String, Scheduler> schedulers() {
            return null;
        }

        @Override
        public Optional<RouteRegistry> routeRegistry() {
            return Optional.empty();
        }

        @Override
        public Optional<DispatcherHandler> dispatcherHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<MappingLocator> mappingLocator() {
            return Optional.empty();
        }

        @Override
        public Optional<ExceptionResolverFactory> exceptionResolverFactory() {
            return Optional.empty();
        }
    }

}

