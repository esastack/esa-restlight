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
package io.esastack.restlight.core;

import esa.commons.function.Function3;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteFilterAdapter;
import io.esastack.restlight.core.handler.RouteFilterChain;
import io.esastack.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.*;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.handler.RestlightHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentsTest {

    @Test
    void testDeployContext() throws Throwable {
        final RestlightOptions ops = RestlightOptionsConfigure.defaultOpts();
        final Restlight restlight = Restlight0.forServer(ops);
        DeployContext ctx = restlight.deployments().deployContext();
        assertEquals(ops, ctx.options());
        assertFalse(ctx.resolverFactory().isPresent());
        assertFalse(ctx.routeRegistry().isPresent());
        assertFalse(ctx.exceptionResolverFactory().isPresent());
        assertFalse(ctx.exceptionMappers().isPresent());
        assertFalse(ctx.mappingLocator().isPresent());
        assertFalse(ctx.handlerAdvicesFactory().isPresent());
        assertFalse(ctx.advices().isPresent());
        assertFalse(ctx.prototypeControllers().isPresent());
        assertFalse(ctx.singletonControllers().isPresent());
        assertFalse(ctx.interceptors().isPresent());

        final MockBean pojo = new MockBean();

        restlight.deployments()
//                .addHandlerMapping(RouteUtils.extractHandlerMapping(
//                        new HandlerContext(ctx),
//                        pojo,
//                        MockBean.class,
//                        MockBean.class.getMethod("index")
//                ).get())
//                .addHandlerMapping(RouteUtils.extractHandlerMapping(
//                        new HandlerContext(ctx),
//                        pojo,
//                        MockBean.class,
//                        MockBean.class.getMethod("list")
//                ).get())
//                .addHandlerMappings(
//                        Collections.singletonList(RouteUtils.extractHandlerMapping(
//                                new HandlerContext(ctx),
//                                pojo,
//                                MockBean.class,
//                                MockBean.class.getMethod("hello")
//                        ).get()))
//                .addHandlerMappings(null)
//                .addHandlerMappings(Collections.emptyList())
//                .addHandlerMappingProvider(ctx0 -> {
//                    try {
//                        return Collections.singletonList(RouteUtils.extractHandlerMapping(
//                                new HandlerContext(ctx0),
//                                pojo,
//                                MockBean.class,
//                                MockBean.class.getMethod("index0")
//                        ).get());
//                    } catch (Throwable th) {
//                        return Collections.emptyList();
//                    }
//                })
//                .addHandlerMappingProviders(Collections.singletonList(ctx0 -> {
//                    try {
//                        return Collections.singleton(RouteUtils.extractHandlerMapping(
//                                new HandlerContext(ctx0),
//                                pojo,
//                                MockBean.class,
//                                MockBean.class.getMethod("list0")
//                        ).get());
//                    } catch (Throwable th) {
//                        return Collections.emptyList();
//                    }
//                }))
                .addHandlerMappingProviders(null)
                .addHandlerMappingProviders(Collections.emptyList())
//                .addParamResolverAdvice(new ArgAdvice())
//                .addArgumentResolverAdvice(new ArgAdviceFactory())
//                .addReturnValueResolverAdvice(new RetAdvice())
//                .addReturnValueResolverAdvice(new RetAdviceFactory())
                .addController(new A())
                .addFilter((context, chain) -> chain.doFilter(context))
                .addFilter(ctx0 -> Optional.of((context, chain) -> chain.doFilter(context)))
                .addFilters(Collections.singletonList(ctx0 -> Optional.of((context, chain) -> chain.doFilter(context))))
                .addRouteFilter(new RouteFilterAdapter() {
                    @Override
                    public boolean supports(HandlerMethod method) {
                        return true;
                    }

                    @Override
                    public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
                        return next.doNext(mapping, context);
                    }
                })
                .addRouteFilter(new RouteFilterFactory() {
                    @Override
                    public Optional<RouteFilter> create(HandlerMethod method) {
                        return Optional.of((mapping, context, next) -> next.doNext(mapping, context));
                    }

                    @Override
                    public boolean supports(HandlerMethod method) {
                        return true;
                    }
                })
                .addRouteFilters(Collections.singletonList(new RouteFilterFactory() {
                    @Override
                    public Optional<RouteFilter> create(HandlerMethod method) {
                        return Optional.of((mapping, context, next) -> next.doNext(mapping, context));
                    }

                    @Override
                    public boolean supports(HandlerMethod method) {
                        return true;
                    }
                }))
                .addControllers(Arrays.asList(new B(), new C()))
                .addControllers(null)
                .addControllers(Collections.emptyList())
                .addControllerAdvice(new A())
                .addControllerAdvices(Arrays.asList(new B(), new C()))
                .addControllerAdvices(null)
                .addControllerAdvices(Collections.emptyList())
                .addInterceptor(() -> request -> true)
                .addInterceptors(Arrays.asList(() -> request -> true, () -> request -> true))
                .addInterceptors(null)
                .addInterceptors(Collections.emptyList())
                .addRouteInterceptor((ctx1, route) -> true)
                .addRouteInterceptors(Arrays.asList((ctx1, route) -> true, (ctx1, route) -> true))
                .addRouteInterceptors(null)
                .addRouteInterceptors(Collections.emptyList())
                .addMappingInterceptor(request -> true)
                .addMappingInterceptors(Arrays.asList(request -> true, request -> true))
                .addMappingInterceptors(null)
                .addMappingInterceptors(Collections.emptyList())
                .addHandlerInterceptor(new HandlerInterceptor() {
                })
                .addHandlerInterceptors(Arrays.asList(new HandlerInterceptor() {
                }, new HandlerInterceptor() {
                }))
                .addHandlerInterceptors(null)
                .addHandlerInterceptors(Collections.emptyList())
                .addInterceptorFactory((ctx12, route) -> Optional.empty())
                .addInterceptorFactories(Arrays.asList((ctx12, route) -> Optional.of(() -> request -> true),
                        (ctx12, route) -> Optional.of(() -> request -> true)))
                .addInterceptorFactories(null)
                .addInterceptorFactories(Collections.emptyList())
//                .addExceptionResolver(RuntimeException.class, (request, response, e) -> Futures.completedFuture())
//                .addArgumentResolver(new Arg())
//                .addArgumentResolver(new ArgFactory())
//                .addArgumentResolvers(Arrays.asList(new ArgFactory(), new ArgFactory()))
//                .addArgumentResolverAdvices(null)
//                .addArgumentResolverAdvices(Collections.emptyList())
//                .addArgumentResolvers(null)
//                .addArgumentResolvers(Collections.emptyList())
//                .addReturnValueResolver(new Ret())
//                .addReturnValueResolver(new RetFactory())
//                .addReturnValueResolvers(Arrays.asList(new RetFactory(), new RetFactory()))
//                .addReturnValueResolvers(null)
//                .addReturnValueResolvers(Collections.emptyList())
//                .addReturnValueResolverAdvices(null)
//                .addReturnValueResolverAdvices(Collections.emptyList())
//                .addRequestSerializer(new Rx())
//                .addRequestSerializers(Arrays.asList(new Rx(), new Rx()))
//                .addRequestSerializers(null)
//                .addRequestSerializers(Collections.emptyList())
//                .addResponseSerializer(new Tx())
//                .addResponseSerializers(Arrays.asList(new Tx(), new Tx()))
//                .addResponseSerializers(null)
//                .addResponseSerializers(Collections.emptyList())
//                .addSerializer(new RxBody())
//                .addSerializers(Collections.singleton(new TxBody()))
                .server()
                .start();
        ctx = restlight.deployments().deployContext();
        assertEquals(ops, ctx.options());
//        assertTrue(ctx.resolverFactory().isPresent());
//        assertEquals(8, ctx.resolverFactory().get().argumentResolvers().size());
//        assertEquals(5, ctx.resolverFactory().get().returnValueResolvers().size());
//        assertEquals(5, ctx.resolverFactory().get().rxSerializers().size());
//        assertEquals(5, ctx.resolverFactory().get().txSerializers().size());


//        assertFalse(ctx.routeHandlerLocator().isPresent());
//        assertTrue(ctx.exceptionResolverFactory().isPresent());
//        assertTrue(ctx.exceptionMappers().isPresent());
//        assertEquals(1, ctx.exceptionMappers().get().size());
//        assertFalse(ctx.mappingLocator().isPresent());
        assertTrue(ctx.handlerAdvicesFactory().isPresent());
        assertTrue(ctx.handlerAdvicesFactory().get() instanceof HandlerAdvicesFactoryImpl);
        assertTrue(ctx.advices().isPresent());
        assertEquals(3, ctx.advices().get().size());
        assertEquals(3, restlight.deployments().filters().size());

//        assertTrue(ctx.prototypeControllers().isPresent());
//        assertEquals(3, ctx.prototypeControllers().get().size());
        assertTrue(ctx.interceptors().isPresent());
        assertEquals(15, ctx.interceptors().get().size());

        assertTrue(ctx.routeRegistry().isPresent());
//        final RouteRegistry registry = ctx.routeRegistry().get();
//        assertEquals(5, registry.routes().size());
    }

    private static class A {
    }

    private static class B {
    }

    private static class C {
    }

    private static class MockBean {

        public String hello() {
            return "";
        }

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

    private static class ParamResolverAdapterImpl implements ParamResolverAdapter {

        @Override
        public boolean supports(Param param) {
            return false;
        }

        @Override
        public Object resolve(Param param, RequestContext context) throws Exception {
            return null;
        }
    }

    private static class ParamResolverAdviceImpl implements ParamResolverAdvice {

        @Override
        public Object aroundResolve(ParamResolverContext context) throws Exception {
            return context.proceed();
        }
    }

    private static class ParamResolverFactoryImpl implements ParamResolverFactory {

        @Override
        public boolean supports(Param param) {
            return false;
        }


        @Override
        public ParamResolver createResolver(Param param, Function3<Class<?>, Type, Param, StringConverter> converterFunc, List<? extends HttpRequestSerializer> serializers) {
            return null;
        }
    }

    private static class ParamResolverAdviceFactoryImpl implements ParamResolverAdviceFactory {

        @Override
        public boolean supports(Param param) {
            return false;
        }

        @Override
        public ParamResolverAdvice createResolverAdvice(Param param, ParamResolver resolver) {
            return null;
        }
    }

//    private static class Ret implements ReturnValueResolverAdapter {
//
//        @Override
//        public byte[] resolve(Object returnValue, HttpRequest request, HttpResponse response) throws Exception {
//            return new byte[0];
//        }
//
//        @Override
//        public boolean supports(InvocableMethod invocableMethod) {
//            return false;
//        }
//    }
//
//    private static class RetAdvice implements ReturnValueResolverAdviceAdapter {
//        @Override
//        public boolean supports(InvocableMethod invocableMethod) {
//            return false;
//        }
//    }
//
//    private static class RetFactory implements ReturnValueResolverFactory {
//
//        @Override
//        public ReturnValueResolver createResolver(InvocableMethod method,
//                                                  List<? extends HttpResponseSerializer> serializers) {
//            return null;
//        }
//
//        @Override
//        public boolean supports(InvocableMethod invocableMethod) {
//            return false;
//        }
//    }
//
//    private static class RetAdviceFactory implements ReturnValueResolverAdviceFactory {
//        @Override
//        public ReturnValueResolverAdvice createResolverAdvice(InvocableMethod method, ReturnValueResolver resolver) {
//            return null;
//        }
//
//        @Override
//        public boolean supports(InvocableMethod invocableMethod) {
//            return false;
//        }
//    }
//
//    private static class Rx implements HttpRequestSerializer {
//
//        @Override
//        public boolean supportsRead(MediaType mediaType, Type type) {
//            return false;
//        }
//
//        @Override
//        public <T> T deSerialize(byte[] data, Type type) throws Exception {
//            return null;
//        }
//
//        @Override
//        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
//            return null;
//        }
//    }
//
//    private static class Tx implements HttpResponseSerializer {
//
//        @Override
//        public boolean supportsWrite(MediaType mediaType, Type type) {
//            return false;
//        }
//
//        @Override
//        public Object customResponse(HttpRequest request, HttpResponse response, Object returnValue) {
//            return null;
//        }
//
//        @Override
//        public byte[] serialize(Object target) throws Exception {
//            return new byte[0];
//        }
//
//        @Override
//        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
//
//        }
//    }
//
//    private static class RxBody implements HttpBodySerializer {
//        @Override
//        public Object customResponse(HttpRequest request, HttpResponse response, Object returnValue) {
//            return null;
//        }
//
//        @Override
//        public <T> T deSerialize(byte[] data, Type type) throws Exception {
//            return null;
//        }
//
//        @Override
//        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
//            return null;
//        }
//
//        @Override
//        public byte[] serialize(Object target) throws Exception {
//            return new byte[0];
//        }
//
//        @Override
//        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
//
//        }
//    }
//
//    private static class TxBody implements HttpBodySerializer {
//        @Override
//        public Object customResponse(HttpRequest request, HttpResponse response, Object returnValue) {
//            return null;
//        }
//
//        @Override
//        public <T> T deSerialize(byte[] data, Type type) throws Exception {
//            return null;
//        }
//
//        @Override
//        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
//            return null;
//        }
//
//        @Override
//        public byte[] serialize(Object target) throws Exception {
//            return new byte[0];
//        }
//
//        @Override
//        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
//
//        }
//    }

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
