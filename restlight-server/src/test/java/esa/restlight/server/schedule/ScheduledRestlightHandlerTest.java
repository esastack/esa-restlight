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
package esa.restlight.server.schedule;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.config.ServerOptionsConfigure;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.util.LoggerUtils;
import esa.restlight.server.util.PromiseUtils;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static esa.restlight.server.route.Mapping.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduledRestlightHandlerTest {

    @Test
    void testSchedule() {
        forTest(Schedulers.IO, Schedulers.IO);
        forTest(Schedulers.IO, Schedulers.BIZ);
        forTest(Schedulers.IO, "other");
        forTest(Schedulers.BIZ, Schedulers.IO);
        forTest(Schedulers.BIZ, Schedulers.BIZ);
        forTest(Schedulers.BIZ, "other");
        forTest("other", Schedulers.IO);
        forTest("other", Schedulers.BIZ);
        forTest("other", "other");
    }

    @Test
    void testSchedulers() {
        final ScheduledRestlightHandler handler =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(),
                        new ForScheduleAssertion(Schedulers.IO, Schedulers.BIZ));
        handler.onStart();
        // just pass a null value for empty operation
        handler.onConnected(null);
        assertEquals(2, handler.schedulers().size());
        handler.shutdown();

        final ScheduledRestlightHandler handler1 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(),
                        new ForScheduleAssertion(Schedulers.IO, Schedulers.IO));
        handler1.onStart();
        // just pass a null value for empty operation
        handler1.onConnected(null);
        assertEquals(1, handler1.schedulers().size());
        handler1.shutdown();
    }

    @Test
    void testUnfinished() {
        final CompletableFuture<Void> hook = new CompletableFuture<>();
        final ForScheduleAssertion dispatcher =
                new ForScheduleAssertion(Schedulers.BIZ, Schedulers.BIZ, hook);
        final ScheduledRestlightHandler handler =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(),
                        dispatcher);
        handler.setTerminationTimeoutSeconds(0L);
        handler.onStart();
        // just pass a null value for empty operation
        handler.onConnected(null);
        for (int i = 0; i < 10; i++) {
            final AsyncRequest req1 = MockAsyncRequest.aMockRequest().withUri("/foo").build();
            final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
            handler.process(req1, res1);
        }
        handler.shutdown();
        assertTrue(dispatcher.unfinished.get().size() >= 8);
        hook.complete(null);
    }

    @Test
    void testNotFound() {
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();
        ScheduledRestlightHandler.notFound(req, res, cf);
        assertTrue(res.isCommitted());
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), res.status());
        assertTrue(cf.isDone());
    }

    @Test
    void testProcess() {
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final ForRouteAssertion handler = new ForRouteAssertion();
        handler.found = false;
        final ScheduledRestlightHandler scheduled1 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler);
        scheduled1.onStart();
        // just pass a null value for empty operation
        scheduled1.onConnected(null);
        scheduled1.process(req1, res1).join();
        assertTrue(res1.isCommitted());
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), res1.status());

        handler.found = true;

        final AsyncRequest req2 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res2 = MockAsyncResponse.aMockResponse().build();

        final ScheduledRestlightHandler scheduled2 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler);
        scheduled2.onStart();
        // just pass a null value for empty operation
        scheduled2.onConnected(null);
        scheduled2.process(req2, res2);
        assertTrue(res2.isCommitted());
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), res2.status());
    }

    @Test
    void testRequestTaskHookWithFixedScheduler() {
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final ForRouteAssertion handler = new ForRouteAssertion();
        handler.found = false;

        final List<Integer> advicesRet = new CopyOnWriteArrayList<>();
        List<RequestTaskHook> advices = Collections.singletonList(task -> new RequestTaskWrap(task) {
            @Override
            public void run() {
                advicesRet.add(1);
                delegate.run();
            }
        });

        final ScheduledRestlightHandler scheduled1 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled1.onStart();
        // just pass a null value for empty operation
        scheduled1.onConnected(null);
        scheduled1.process(req1, res1).join();
        assertEquals(1, advicesRet.size());
        assertEquals(1, advicesRet.get(0));

        handler.found = true;
        advicesRet.clear();

        final AsyncRequest req2 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res2 = MockAsyncResponse.aMockResponse().build();


        advices = Arrays.asList(new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return new RequestTaskWrap(task) {

                    @Override
                    public void run() {
                        advicesRet.add(1);
                        delegate.run();
                    }
                };
            }

            @Override
            public int getOrder() {
                return 0;
            }
        }, new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return new RequestTaskWrap(task) {

                    @Override
                    public void run() {
                        advicesRet.add(0);
                        delegate.run();
                    }
                };
            }

            @Override
            public int getOrder() {
                return -1;
            }
        });

        final ScheduledRestlightHandler scheduled2 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled2.onStart();
        // just pass a null value for empty operation
        scheduled2.onConnected(null);
        scheduled2.process(req2, res2);
        assertEquals(2, advicesRet.size());
        assertEquals(1, advicesRet.get(0));
        assertEquals(0, advicesRet.get(1));
    }

    @Test
    void testRequestTaskHookWithSpecifiedScheduler() {
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final ForRouteAssertion handler = new ForRouteAssertion() {
            @Override
            public List<Route> routes() {
                List<Route> routes = new LinkedList<>(super.routes());
                routes.add(Route.route(Mapping.get("/bar")).schedule(Schedulers.biz()));
                return routes;
            }
        };
        handler.found = false;

        final List<Integer> advicesRet = new CopyOnWriteArrayList<>();
        List<RequestTaskHook> advices = Collections.singletonList(task -> new RequestTaskWrap(task) {
            @Override
            public void run() {
                advicesRet.add(1);
                delegate.run();
            }
        });

        final ScheduledRestlightHandler scheduled1 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled1.onStart();
        // just pass a null value for empty operation
        scheduled1.onConnected(null);
        scheduled1.process(req1, res1).join();
        assertTrue(advicesRet.isEmpty());

        handler.found = true;
        advicesRet.clear();

        final AsyncRequest req2 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res2 = MockAsyncResponse.aMockResponse().build();


        advices = Arrays.asList(new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return new RequestTaskWrap(task) {

                    @Override
                    public void run() {
                        advicesRet.add(1);
                        delegate.run();
                    }
                };
            }

            @Override
            public int getOrder() {
                return 0;
            }
        }, new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return new RequestTaskWrap(task) {

                    @Override
                    public void run() {
                        advicesRet.add(0);
                        delegate.run();
                    }
                };
            }

            @Override
            public int getOrder() {
                return -1;
            }
        });

        final ScheduledRestlightHandler scheduled2 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled2.onStart();
        // just pass a null value for empty operation
        scheduled2.onConnected(null);
        scheduled2.process(req2, res2);
        assertEquals(2, advicesRet.size());
        assertEquals(1, advicesRet.get(0));
        assertEquals(0, advicesRet.get(1));
    }

    @Test
    void testRequestTaskHookWithNullReturn() {
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final ForRouteAssertion handler = new ForRouteAssertion();
        handler.found = false;

        List<RequestTaskHook> advices = Collections.singletonList(task -> null);

        final ScheduledRestlightHandler scheduled1 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled1.onStart();
        // just pass a null value for empty operation
        scheduled1.onConnected(null);
        assertTrue(scheduled1.process(req1, res1).isDone());
        assertTrue(res1.isCommitted());

        handler.found = true;


        final AsyncRequest req2 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res2 = MockAsyncResponse.aMockResponse().build();

        final List<Integer> advicesRet = new CopyOnWriteArrayList<>();
        advices = Arrays.asList(new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return new RequestTaskWrap(task) {

                    @Override
                    public void run() {
                        advicesRet.add(1);
                        delegate.run();
                    }
                };
            }

            @Override
            public int getOrder() {
                return 0;
            }
        }, new RequestTaskHook() {
            @Override
            public RequestTask onRequest(RequestTask task) {
                return null;
            }

            @Override
            public int getOrder() {
                return -1;
            }
        });

        final ScheduledRestlightHandler scheduled2 =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), handler, advices);
        scheduled2.onStart();
        // just pass a null value for empty operation
        scheduled2.onConnected(null);
        assertTrue(scheduled2.process(req2, res2).isDone());
        assertTrue(advicesRet.isEmpty());
        assertTrue(res2.isCommitted());
    }

    private void forTest(String s1, String s2) {
        doProcess(new ForScheduleAssertion(s1, s2));
    }

    private void doProcess(ForScheduleAssertion dispatcher) {
        final ScheduledRestlightHandler handler =
                new ScheduledRestlightHandler(ServerOptionsConfigure.defaultOpts(), dispatcher);
        handler.onStart();
        // just pass a null value for empty operation
        handler.onConnected(null);
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().withUri("/foo").build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final AsyncRequest req2 = MockAsyncRequest.aMockRequest().withUri("/bar").build();
        final AsyncResponse res2 = MockAsyncResponse.aMockResponse().build();
        handler.process(req1, res1);
        handler.process(req2, res2);
        handler.shutdown();
        assertTrue(dispatcher.ret.get());
    }

    private static class ForScheduleAssertion implements DispatcherHandler {

        private final boolean route1Phase1OnIo;
        private final boolean route2Phase1OnIo;
        private final Thread io = Thread.currentThread();
        private final Route r1;
        private final Route r2;
        private final CompletableFuture<Void> hook;
        private final String s1;
        private final String s2;

        private final AtomicBoolean ret = new AtomicBoolean(true);
        private final AtomicReference<List<RequestTask>> unfinished = new AtomicReference<>();


        private ForScheduleAssertion(String s1,
                                     String s2) {
            this(s1, s2, null);
        }

        private ForScheduleAssertion(String s1,
                                     String s2,
                                     CompletableFuture<Void> hook) {
            this.s1 = s1;
            this.s2 = s2;
            Executor e = new ThreadPoolExecutor(1,
                    1,
                    0L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(32),
                    r -> new Thread(r, s1));
            r1 = Route.route(get("/foo"))
                    .schedule("foo", e);
            if (s1.equals(s2)) {
                this.route1Phase1OnIo = false;
                this.route2Phase1OnIo = false;
                r2 = Route.route(get("/bar"))
                        .schedule("foo", e);
            } else {
                this.route1Phase1OnIo = true;
                this.route2Phase1OnIo = true;
                r2 = Route.route(get("/bar"))
                        .schedule("bar", new ThreadPoolExecutor(1,
                                1,
                                0L,
                                TimeUnit.SECONDS,
                                new LinkedBlockingQueue<>(32),
                                r -> new Thread(r, s2)));
            }
            this.hook = hook;
        }

        @Override
        public List<Route> routes() {
            return Arrays.asList(r1, r2);
        }

        @Override
        public Route route(AsyncRequest request, AsyncResponse response) {
            if (request.uri().equals("/foo")) {
                if (route1Phase1OnIo ^ Thread.currentThread() == io) {
                    ret.set(false);
                }
                return r1;
            }
            if (request.uri().equals("/bar")) {
                if (route2Phase1OnIo ^ Thread.currentThread() == io) {
                    ret.set(false);
                }
                return r2;
            }
            return null;
        }

        @Override
        public void service(AsyncRequest request,
                            AsyncResponse response,
                            CompletableFuture<Void> promise,
                            Route route) {
            if (route == r1) {
                if (!Thread.currentThread().getName().equals(s1)) {
                    ret.set(false);
                }
            }
            if (route == r2) {
                if (!Thread.currentThread().getName().equals(s2)) {
                    ret.set(false);
                }
            }
            if (hook != null) {
                hook.join();
            }
            LoggerUtils.logger().error(route.toString());
            PromiseUtils.setSuccess(promise);
        }

        @Override
        public void handleUnexpectedError(AsyncRequest request, AsyncResponse response, Throwable error,
                                          CompletableFuture<Void> promise) {

        }

        @Override
        public void handleRejectedWork(RequestTask task, String errorMsg) {
        }

        @Override
        public void handleUnfinishedWorks(List<RequestTask> unfinishedWorkList) {
            unfinished.set(unfinishedWorkList);
        }

        @Override
        public long rejectCount() {
            return 0L;
        }
    }

    private static class ForRouteAssertion implements DispatcherHandler {

        private final Route r = Route.route(Mapping.get("/foo")).schedule(Schedulers.io());
        private boolean found = true;

        @Override
        public List<Route> routes() {
            return Collections.singletonList(r);
        }

        @Override
        public Route route(AsyncRequest request, AsyncResponse response) {
            return found ? r : null;
        }

        @Override
        public void service(AsyncRequest request, AsyncResponse response, CompletableFuture<Void> promise,
                            Route route) {
            response.sendResult(204);
            promise.complete(null);
        }

        @Override
        public void handleUnexpectedError(AsyncRequest request, AsyncResponse response, Throwable error,
                                          CompletableFuture<Void> promise) {

        }

        @Override
        public void handleRejectedWork(RequestTask task, String reason) {

        }

        @Override
        public void handleUnfinishedWorks(List<RequestTask> unfinishedWorkList) {

        }

        @Override
        public long rejectCount() {
            return 0;
        }
    }

    private abstract static class RequestTaskWrap implements RequestTask {

        final RequestTask delegate;

        private RequestTaskWrap(RequestTask delegate) {
            this.delegate = delegate;
        }

        @Override
        public AsyncRequest request() {
            return delegate.request();
        }

        @Override
        public AsyncResponse response() {
            return delegate.response();
        }

        @Override
        public CompletableFuture<Void> promise() {
            return delegate.promise();
        }
    }

}
