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
package esa.restlight.core;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import esa.restlight.core.handler.impl.HandlerMappingImpl;
import esa.restlight.core.handler.impl.RouteHandlerImpl;
import esa.restlight.core.interceptor.HandlerInterceptor;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdvice;
import esa.restlight.core.resolver.ArgumentResolverAdviceAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceFactory;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdvice;
import esa.restlight.core.resolver.ReturnValueResolverAdviceAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceFactory;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.RestlightServer;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.util.Futures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentsTest {

    @Test
    void testDeployContext() throws Throwable {
        final RestlightOptions ops = RestlightOptionsConfigure.defaultOpts();
        final Restlight restlight = Restlight0.forServer(ops);
        DeployContext<RestlightOptions> ctx = restlight.deployments().deployContext();
        assertEquals(ops, ctx.options());
        assertFalse(ctx.resolverFactory().isPresent());
        assertFalse(ctx.routeHandlerLocator().isPresent());
        assertFalse(ctx.exceptionResolverFactory().isPresent());
        assertFalse(ctx.exceptionMappers().isPresent());
        assertFalse(ctx.mappingLocator().isPresent());
        assertFalse(ctx.handlerAdvicesFactory().isPresent());
        assertFalse(ctx.advices().isPresent());
        assertFalse(ctx.controllers().isPresent());
        assertFalse(ctx.interceptors().isPresent());

        final MockBean pojo = new MockBean();
        restlight.deployments()
                .addHandlerMapping(new HandlerMappingImpl(Mapping.mapping(),
                        new RouteHandlerImpl(new HandlerMethodImpl(MockBean.class,
                                MockBean.class.getMethod("index"), pojo),
                                false,
                                Schedulers.BIZ)))
                .addHandlerMapping(Collections.singleton(new HandlerMappingImpl(Mapping.mapping(),
                        new RouteHandlerImpl(new HandlerMethodImpl(MockBean.class,
                                MockBean.class.getMethod("list"), pojo),
                                false,
                                Schedulers.BIZ))))
                .addHandlerMappingProvider(ctx0 -> {
                    try {
                        return Collections.singleton(new HandlerMappingImpl(Mapping.mapping(),
                                new RouteHandlerImpl(new HandlerMethodImpl(MockBean.class,
                                        MockBean.class.getMethod("index0"), pojo),
                                        false,
                                        Schedulers.BIZ)));
                    } catch (Throwable th) {
                        return Collections.emptyList();
                    }
                })
                .addHandlerMappingProviders(Collections.singletonList(ctx0 -> {
                    try {
                        return Collections.singleton(new HandlerMappingImpl(Mapping.mapping(),
                                new RouteHandlerImpl(new HandlerMethodImpl(MockBean.class,
                                        MockBean.class.getMethod("list0"), pojo),
                                        false,
                                        Schedulers.BIZ)));
                    } catch (Throwable th) {
                        return Collections.emptyList();
                    }
                }))
                .addArgumentResolverAdvice(new ArgAdvice())
                .addArgumentResolverAdvice(new ArgAdviceFactory())
                .addReturnValueResolverAdvice(new RetAdvice())
                .addReturnValueResolverAdvice(new RetAdviceFactory())
                .addController(new A())
                .addControllers(Arrays.asList(new B(), new C()))
                .addControllerAdvice(new A())
                .addControllerAdvices(Arrays.asList(new B(), new C()))
                .addInterceptor(() -> request -> true)
                .addInterceptors(Arrays.asList(() -> request -> true, () -> request -> true))
                .addRouteInterceptor((ctx1, route) -> true)
                .addRouteInterceptors(Arrays.asList((ctx1, route) -> true, (ctx1, route) -> true))
                .addMappingInterceptor(request -> true)
                .addMappingInterceptors(Arrays.asList(request -> true, request -> true))
                .addHandlerInterceptor(new HandlerInterceptor() {
                })
                .addHandlerInterceptors(Arrays.asList(new HandlerInterceptor() {
                }, new HandlerInterceptor() {
                }))
                .addInterceptorFactory((ctx12, route) -> Optional.empty())
                .addInterceptorFactories(Arrays.asList((ctx12, route) -> Optional.of(() -> request -> true),
                        (ctx12, route) -> Optional.of(() -> request -> true)))
                .addExceptionResolver(RuntimeException.class, (request, response, e) -> Futures.completedFuture())
                .addArgumentResolver(new Arg())
                .addArgumentResolver(new ArgFactory())
                .addArgumentResolvers(Arrays.asList(new ArgFactory(), new ArgFactory()))
                .addReturnValueResolver(new Ret())
                .addReturnValueResolver(new RetFactory())
                .addReturnValueResolvers(Arrays.asList(new RetFactory(), new RetFactory()))
                .addRequestSerializer(new Rx())
                .addRequestSerializers(Arrays.asList(new Rx(), new Rx()))
                .addResponseSerializer(new Tx())
                .addResponseSerializers(Arrays.asList(new Tx(), new Tx()))
                .addSerializer(new RxBody())
                .addSerializers(Collections.singleton(new TxBody()))
                .server()
                .start();
        ctx = restlight.deployments().deployContext();
        assertEquals(ops, ctx.options());
        assertTrue(ctx.resolverFactory().isPresent());
        assertEquals(8, ctx.resolverFactory().get().argumentResolvers().size());
        assertEquals(5, ctx.resolverFactory().get().returnValueResolvers().size());
        assertEquals(5, ctx.resolverFactory().get().rxSerializers().size());
        assertEquals(5, ctx.resolverFactory().get().txSerializers().size());


        assertFalse(ctx.routeHandlerLocator().isPresent());
        assertTrue(ctx.exceptionResolverFactory().isPresent());
        assertTrue(ctx.exceptionMappers().isPresent());
        assertEquals(1, ctx.exceptionMappers().get().size());
        assertFalse(ctx.mappingLocator().isPresent());
        assertTrue(ctx.handlerAdvicesFactory().isPresent());
        assertTrue(ctx.handlerAdvicesFactory().get() instanceof HandlerAdvicesFactoryImpl);
        assertTrue(ctx.advices().isPresent());
        assertEquals(3, ctx.advices().get().size());
        assertTrue(ctx.controllers().isPresent());
        assertEquals(3, ctx.controllers().get().size());
        assertTrue(ctx.interceptors().isPresent());
        assertEquals(15, ctx.interceptors().get().size());

        assertTrue(ctx.routeRegistry().isPresent());
        final ReadOnlyRouteRegistry registry = ctx.routeRegistry().get();
        assertEquals(4, registry.routes().size());
    }

    private static class A {
    }

    private static class B {
    }

    private static class C {
    }

    private static class MockBean {

        public String index() {
            return "";
        }

        public String list() {
            return "";
        }

        public String index0() {
            return "";
        }

        public String list0() {
            return "";
        }
    }

    private static class HandlerMethodImpl extends HandlerMethod {
        private HandlerMethodImpl(Class<?> userType, Method method, Object bean) {
            super(userType, method, bean);
        }
    }

    private static class Arg implements ArgumentResolverAdapter {

        @Override
        public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ArgAdvice implements ArgumentResolverAdviceAdapter {
        @Override
        public void beforeResolve(AsyncRequest request, AsyncResponse response) {

        }

        @Override
        public Object afterResolved(Object arg, AsyncRequest request, AsyncResponse response) {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ArgFactory implements ArgumentResolverFactory {

        @Override
        public boolean supports(Param param) {
            return false;
        }

        @Override
        public ArgumentResolver createResolver(Param param, List<? extends HttpRequestSerializer> serializers) {
            return null;
        }
    }

    private static class ArgAdviceFactory implements ArgumentResolverAdviceFactory {
        @Override
        public ArgumentResolverAdvice createResolverAdvice(Param param, ArgumentResolver resolver) {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class Ret implements ReturnValueResolverAdapter {

        @Override
        public byte[] resolve(Object returnValue, AsyncRequest request, AsyncResponse response) throws Exception {
            return new byte[0];
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class RetAdvice implements ReturnValueResolverAdviceAdapter {
        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class RetFactory implements ReturnValueResolverFactory {

        @Override
        public ReturnValueResolver createResolver(InvocableMethod method,
                                                  List<? extends HttpResponseSerializer> serializers) {
            return null;
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class RetAdviceFactory implements ReturnValueResolverAdviceFactory {
        @Override
        public ReturnValueResolverAdvice createResolverAdvice(InvocableMethod method, ReturnValueResolver resolver) {
            return null;
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class Rx implements HttpRequestSerializer {

        @Override
        public boolean supportsRead(MediaType mediaType, Type type) {
            return false;
        }

        @Override
        public <T> T deSerialize(byte[] data, Type type) throws Exception {
            return null;
        }

        @Override
        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
            return null;
        }
    }

    private static class Tx implements HttpResponseSerializer {

        @Override
        public boolean supportsWrite(MediaType mediaType, Type type) {
            return false;
        }

        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return new byte[0];
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {

        }
    }

    private static class RxBody implements HttpBodySerializer {
        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public <T> T deSerialize(byte[] data, Type type) throws Exception {
            return null;
        }

        @Override
        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return new byte[0];
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {

        }
    }

    private static class TxBody implements HttpBodySerializer {
        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public <T> T deSerialize(byte[] data, Type type) throws Exception {
            return null;
        }

        @Override
        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return new byte[0];
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {

        }
    }

    private static class Restlight0 extends Restlight {

        Restlight0(RestlightOptions options) {
            super(options);
        }

        /**
         * Creates a HTTP server of Restlight by default options.
         *
         * @return Restlight
         */
        public static Restlight forServer() {
            return forServer(RestlightOptionsConfigure.defaultOpts());
        }

        /**
         * Creates a HTTP server of Restlight by given {@link RestlightOptions}.
         *
         * @return Restlight
         */
        public static Restlight forServer(RestlightOptions options) {
            return new Restlight0(options);
        }


        @Override
        protected RestlightServer doBuildServer(RestlightHandler handler) {
            return new RestlightServer() {
                @Override
                public boolean isStarted() {
                    return false;
                }

                @Override
                public void start() {

                }

                @Override
                public void shutdown() {

                }

                @Override
                public void await() {

                }

                @Override
                public Executor ioExecutor() {
                    return null;
                }

                @Override
                public Executor bizExecutor() {
                    return null;
                }

                @Override
                public SocketAddress address() {
                    return null;
                }
            };
        }
    }

}
