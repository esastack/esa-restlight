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

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.filter.RouteFilterAdapter;
import io.esastack.restlight.core.filter.RouteFilterChain;
import io.esastack.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.core.filter.RouteContext;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentsTest {

    @Test
    void testDeployContext() {
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

        restlight.deployments()
                .addHandlerMappingProviders(null)
                .addHandlerMappingProviders(Collections.emptyList())
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
                    public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context,
                                                        RouteFilterChain next) {
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
                .server()
                .start();
        ctx = restlight.deployments().deployContext();
        assertEquals(ops, ctx.options());

        assertTrue(ctx.handlerAdvicesFactory().isPresent());
        assertTrue(ctx.handlerAdvicesFactory().get() instanceof HandlerAdvicesFactoryImpl);
        assertTrue(ctx.advices().isPresent());
        assertEquals(3, ctx.advices().get().size());
        assertEquals(3, restlight.deployments().filters().size());

        assertTrue(ctx.interceptors().isPresent());
        assertEquals(15, ctx.interceptors().get().size());

        assertTrue(ctx.routeRegistry().isPresent());
    }

    private static class A {
    }

    private static class B {
    }

    private static class C {
    }

    private static class Restlight0 extends Restlight {

        Restlight0(RestlightOptions options) {
            super(options);
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
