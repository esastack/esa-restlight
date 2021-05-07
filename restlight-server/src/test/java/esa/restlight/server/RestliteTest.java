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
package esa.restlight.server;

import esa.restlight.server.bootstrap.RestlightServer;
import esa.restlight.server.config.SchedulingOptionsConfigure;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.config.ServerOptionsConfigure;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.route.Route;
import esa.restlight.server.schedule.ExecutorScheduler;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.schedule.Schedulers;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static esa.restlight.server.route.Mapping.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestliteTest {

    @Test
    void testArgs() {
        final ServerOptions ops = ServerOptionsConfigure.defaultOpts();
        final Restlite restlite = Restlite.forServer(ops)
                .daemon(false);
        assertFalse(restlite.daemon);
        assertEquals(ops, restlite.options);
    }

    @Test
    void testImmutable() {
        final Restlite restlite = Restlite0.forServer();
        restlite.start();
        assertThrows(IllegalStateException.class, restlite::checkImmutable);
    }

    @Test
    void testDeployments() {
        final ServerOptions ops =
                ServerOptionsConfigure.newOpts()
                        .scheduling(SchedulingOptionsConfigure.newOpts()
                                .defaultScheduler("custom")
                                .configured())
                        .configured();
        final Route r1 = Route.route(get("/foo")).schedule(Schedulers.io());
        final Route r2 = Route.route(get("/bar")).schedule(Schedulers.biz());
        final Route r3 = Route.route(get("/baz"));
        final ThreadPoolExecutor custom = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        final Restlite restlite = Restlite0.forServer(ops)
                .deployments()
                .addScheduler(Schedulers.fromExecutor("custom", custom))
                .addRoute(r1)
                .addRoutes(Arrays.asList(r2, r3))
                .server();
        restlite.start();
        final ServerDeployContext<ServerOptions> ctx = restlite.deployments().deployContext();
        assertNotNull(ctx);
        assertEquals(ops, ctx.options());
        assertTrue(ctx.routeRegistry().isPresent());
        assertNotNull(ctx.routeRegistry().get().routes());
        assertEquals(3, ctx.routeRegistry().get().routes().size());
        assertEquals(3, ctx.schedulers().size());
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
        assertTrue(custom.getRejectedExecutionHandler() instanceof BaseDeployments.BizRejectedHandler);
    }

    private static class Restlite0 extends Restlite {

        /**
         * Creates a HTTP server by default options.
         *
         * @return Restlite
         */
        public static Restlite0 forServer() {
            return forServer(ServerOptionsConfigure.defaultOpts());
        }

        /**
         * Creates a HTTP server by given options.
         *
         * @return Restlite
         */
        public static Restlite0 forServer(ServerOptions options) {
            return new Restlite0(options);
        }


        Restlite0(ServerOptions options) {
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
