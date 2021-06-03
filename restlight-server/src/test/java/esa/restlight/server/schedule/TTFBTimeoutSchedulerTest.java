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
package esa.restlight.server.schedule;

import esa.httpserver.core.AsyncResponse;
import esa.httpserver.utils.Constants;
import esa.restlight.server.config.TimeoutOptions;
import esa.restlight.server.config.TimeoutOptionsConfigure;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TTFBTimeoutSchedulerTest {

    @Test
    void testScheduleDirectly() {
        final AtomicBoolean flag = new AtomicBoolean();
        final Runnable runnable = () -> flag.set(true);

        TimeoutScheduler scheduler = new TTFBTimeoutScheduler(Schedulers.io(),
                TimeoutOptionsConfigure.newOpts().configured());
        scheduler.schedule(runnable);
        assertTrue(flag.get());
        flag.set(false);

        scheduler = new TTFBTimeoutScheduler(Schedulers.io(),
                TimeoutOptionsConfigure.defaultOpts());
        final RequestTask task = RequestTaskImpl.newRequestTask(MockAsyncRequest.aMockRequest()
                        .withHeader(Constants.TTFB.toString(),
                                String.valueOf(System.currentTimeMillis() + 1000L)).build(),
                MockAsyncResponse.aMockResponse().build(), new CompletableFuture<>(), runnable);
        scheduler.schedule(task);
        assertTrue(flag.get());
    }

    @Test
    void testRejectedBeforeSubmitting() {
        final AtomicBoolean flag = new AtomicBoolean();

        // actualCost < timeoutMillis
        final TimeoutScheduler scheduler0 = new TTFBTimeoutScheduler(Schedulers.io(),
                TimeoutOptionsConfigure.newOpts()
                        .timeMillis(5L)
                        .type(TimeoutOptions.Type.TTFB)
                        .configured());
        final AsyncResponse response0 = MockAsyncResponse.aMockResponse().build();
        final Runnable runnable0 = () -> {
            flag.set(true);
            response0.sendResult(1000);
        };
        final RequestTask task0 = RequestTaskImpl.newRequestTask(MockAsyncRequest.aMockRequest()
                        .withHeader(Constants.TTFB.toString(),
                                String.valueOf(System.currentTimeMillis() + 100L)).build(),
                response0, new CompletableFuture<>(), runnable0);
        scheduler0.schedule(task0);
        assertTrue(flag.get());
        assertEquals(1000, response0.status());
        flag.set(false);

        // actualCost >= timeoutMillis
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        final Runnable runnable1 = () -> {
            response1.sendResult(1000);
            flag.set(true);
        };
        final RequestTask task1 = RequestTaskImpl.newRequestTask(MockAsyncRequest.aMockRequest()
                        .withHeader(Constants.TTFB.toString(),
                                String.valueOf(System.currentTimeMillis() - 100L)).build(),
                MockAsyncResponse.aMockResponse().build(), new CompletableFuture<>(), runnable1);
        final TimeoutScheduler scheduler1 = new TTFBTimeoutScheduler(Schedulers.io(),
                TimeoutOptionsConfigure.newOpts()
                        .timeMillis(5L)
                        .type(TimeoutOptions.Type.TTFB)
                        .configured());

        scheduler1.schedule(task1);
        assertFalse(flag.get());
        assertEquals(200, response1.status());
    }

}

