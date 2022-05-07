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
import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.core.config.RouteOptionsConfigure;
import io.esastack.restlight.core.config.SchedulingOptionsConfigure;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.server.processor.schedule.ExecutorScheduler;
import io.esastack.restlight.core.server.processor.schedule.RequestTaskHook;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import io.esastack.restlight.core.spi.RequestTaskHookFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.esastack.restlight.core.route.Mapping.get;
import static org.junit.jupiter.api.Assertions.*;

class RestlightTest {

    @Test
    void testArgs() {
        final RestlightOptions ops = RestlightOptionsConfigure.defaultOpts();
        final AbstractRestlight restlight = Restlight.forServer(ops)
                .daemon(false);
        assertFalse(restlight.daemon);
        assertEquals(ops, restlight.options);
    }

    @Test
    void testImmutable() {
        final AbstractRestlight restlight = Restlight0.forServer();
        restlight.start();
        assertThrows(IllegalStateException.class, restlight::checkImmutable);
    }

    @Test
    void testDeployments() {
        final RestlightOptions ops =
                RestlightOptionsConfigure.newOpts()
                        .scheduling(SchedulingOptionsConfigure.newOpts()
                                .defaultScheduler("custom")
                                .configured())
                        .route(RouteOptionsConfigure.newOpts().useCachedRouting(true).computeRate(20).configured())
                        .configured();
        final Route r1 = Route.route(get("/foo")).scheduler(Schedulers.io());
        final Route r2 = Route.route(get("/bar")).scheduler(Schedulers.biz());
        final Route r3 = Route.route(get("/baz"));
        final ThreadPoolExecutor custom = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        final AbstractRestlight restlight = Restlight0.forServer(ops)
                .deployments()
                .addScheduler(Schedulers.fromExecutor("custom", custom))
                .addSchedulers(Collections.singleton(Schedulers.fromExecutor("custom0",
                        GlobalEventExecutor.INSTANCE)))
                .addRoute(r1)
                .addRoutes(Arrays.asList(r2, r3))
                .addRequestTaskHook((RequestTaskHook) task -> null)
                .addRequestTaskHook((RequestTaskHookFactory) ctx -> Optional.empty())
                .addRequestTaskHooks(Collections.singletonList(ctx -> Optional.empty()))
                .server();
        restlight.start();
        final DeployContext ctx = restlight.deployments().deployContext();
        assertNotNull(ctx);
        assertEquals(ops, ctx.options());
        assertTrue(ctx.routeRegistry().isPresent());
        assertNotNull(ctx.routeRegistry().get().routes());
        assertEquals(3, ctx.routeRegistry().get().routes().size());
        assertEquals(4, ctx.schedulers().size());
        assertEquals(Schedulers.io(), ctx.schedulers().get(Schedulers.IO));
        final Scheduler biz = ctx.schedulers().get(Schedulers.BIZ);
        assertTrue(biz instanceof ExecutorScheduler);
        assertTrue(((ExecutorScheduler) biz).executor() instanceof ThreadPoolExecutor);
        final ThreadPoolExecutor bizPool = (ThreadPoolExecutor) ((ExecutorScheduler) biz).executor();
        assertEquals(ops.getBizThreads().getCore(), bizPool.getCorePoolSize());
        assertEquals(ops.getBizThreads().getMax(), bizPool.getMaximumPoolSize());
        assertEquals(ops.getBizThreads().getKeepAliveTimeSeconds(), bizPool.getKeepAliveTime(TimeUnit.SECONDS));
        final Scheduler customScheduler = ctx.schedulers().get("custom");
        assertTrue(customScheduler instanceof ExecutorScheduler);
        assertEquals(custom, ((ExecutorScheduler) customScheduler).executor());
        assertTrue(custom.getRejectedExecutionHandler() instanceof Deployments.BizRejectedHandler);
    }

    private static class Restlight0 extends Restlight {

        /**
         * Creates a HTTP server by default options.
         *
         * @return restlight
         */
        public static Restlight0 forServer() {
            return forServer(RestlightOptionsConfigure.defaultOpts());
        }

        /**
         * Creates a HTTP server by given options.
         *
         * @return restlight
         */
        public static Restlight0 forServer(RestlightOptions options) {
            return new Restlight0(options);
        }


        Restlight0(RestlightOptions options) {
            super(options);
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
