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
package esa.restlight.jaxrs.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.HandlerAdvicesFactory;
import esa.restlight.core.handler.locate.MappingLocator;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.interceptor.InterceptorFactory;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.resolver.exception.ExceptionMapper;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.util.Constants;
import esa.restlight.jaxrs.ResolverUtils;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;
import esa.restlight.server.bootstrap.DispatcherExceptionHandler;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanParamArgumentResolverTest {

    private static final BeanParamArgumentResolver resolverFactory = new BeanParamArgumentResolver(new Context());

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final AsyncRequest request = MockAsyncRequest
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


    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                parameter.method()).get().path()).test(request);
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
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

    private static class Context implements DeployContext<RestlightOptions> {

        @Override
        public Optional<List<Object>> controllers() {
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
                    Arrays.asList(new CookieParamArgumentResolver(),
                            new HeaderParamArgumentResolver(),
                            new MatrixParamArgumentResolver(),
                            new PathParamArgumentResolver(),
                            new QueryParamArgumentResolver()),
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
        public Optional<RouteHandlerLocator> routeHandlerLocator() {
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

        @Override
        public String name() {
            return Constants.SERVER;
        }

        @Override
        public RestlightOptions options() {
            return null;
        }

        @Override
        public Optional<ReadOnlyRouteRegistry> routeRegistry() {
            return Optional.empty();
        }

        @Override
        public Map<String, Scheduler> schedulers() {
            return null;
        }

        @Override
        public Optional<DispatcherHandler> dispatcherHandler() {
            return Optional.empty();
        }

        @Override
        public void attribute(String key, Object value) {

        }

        @Override
        public Optional<List<DispatcherExceptionHandler>> dispatcherExceptionHandlers() {
            return Optional.empty();
        }

        @Override
        public Object attribute(String key) {
            return null;
        }

        @Override
        public Object removeAttribute(String key) {
            return null;
        }
    }

}
