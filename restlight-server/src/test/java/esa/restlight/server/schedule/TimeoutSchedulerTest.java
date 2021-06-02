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
import esa.restlight.server.config.TimeoutOptionsConfigure;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeoutSchedulerTest {

    @Test
    void testDelegate() {
        final Scheduler delegate = mock(Scheduler.class);
        final TimeoutScheduler scheduler = new TimeoutScheduler(delegate, TimeoutOptionsConfigure.defaultOpts());

        scheduler.name();
        verify(delegate).name();

        scheduler.shutdown();
        verify(delegate).shutdown();

        final Runnable runnable = () -> {};
        scheduler.schedule(runnable);
        verify(delegate).schedule(runnable);
    }

    @Test
    void testRejectedBeforeExecution() {
        final AtomicBoolean flag = new AtomicBoolean();

        // actualCost < timeoutMillis
        final TimeoutScheduler scheduler0 = new TimeoutScheduler(Schedulers.io(),
                TimeoutOptionsConfigure.newOpts().millisTime(5L).configured());
        final AsyncResponse response0 = MockAsyncResponse.aMockResponse().build();
        final Runnable runnable0 = () -> {
            flag.set(true);
            response0.sendResult(1000);
        };
        final RequestTask task0 = RequestTaskImpl.newRequestTask(MockAsyncRequest.aMockRequest().build(),
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
        final RequestTask task1 = RequestTaskImpl.newRequestTask(MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build(), new CompletableFuture<>(), runnable1);
        final TimeoutScheduler scheduler1 = new TimeoutScheduler(new Scheduler() {

            @Override
            public String name() {
                return "test";
            }

            @Override
            public void schedule(Runnable cmd) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10L);
                } catch (Throwable ignore) {
                }
                cmd.run();
            }

            @Override
            public void shutdown() {

            }
        }, TimeoutOptionsConfigure.newOpts().millisTime(5L).configured());

        scheduler1.schedule(task1);
        assertFalse(flag.get());
        assertEquals(200, response1.status());
    }

}

