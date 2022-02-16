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
package io.esastack.restlight.jaxrs.impl.container;

import io.netty.util.Timeout;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.TimeoutHandler;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncResponseImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new AsyncResponseImpl(null));
        new AsyncResponseImpl(new CompletableFuture<>());
    }

    @Test
    void testResumeResponse() throws Throwable {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final AsyncResponse response = new AsyncResponseImpl(future);
        assertTrue(response.isSuspended());
        assertFalse(response.isDone());
        assertFalse(response.isCancelled());
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertFalse(future.isCancelled());

        final Object result = new Object();
        assertTrue(response.resume(result));
        assertTrue(response.isDone());
        assertFalse(response.isSuspended());
        assertFalse(response.isCancelled());
        assertSame(result, future.get());

        assertFalse(response.resume(result));

        // other operations after resuming
        assertFalse(response.cancel());
        assertFalse(response.cancel(1));
        assertFalse(response.cancel(new Date()));
    }

    @Test
    void testResumeThrowable() {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final AsyncResponse response = new AsyncResponseImpl(future);
        assertTrue(response.isSuspended());
        assertFalse(response.isDone());
        assertFalse(response.isCancelled());
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertFalse(future.isCancelled());

        final RuntimeException result = new RuntimeException();
        assertTrue(response.resume(result));
        assertTrue(response.isDone());
        assertFalse(response.isSuspended());
        assertFalse(response.isCancelled());
        assertTrue(future.isCompletedExceptionally());

        assertFalse(response.resume(result));

        // other operations after resuming
        assertFalse(response.cancel());
        assertFalse(response.cancel(1));
        assertFalse(response.cancel(new Date()));
        assertFalse(response.setTimeout(0, TimeUnit.SECONDS));
    }

    @Test
    void testCancel1() throws Throwable {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final AsyncResponse response = new AsyncResponseImpl(future);
        assertTrue(response.isSuspended());
        assertFalse(response.isDone());
        assertFalse(response.isCancelled());
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertFalse(future.isCancelled());

        assertTrue(response.cancel());
        assertTrue(future.isDone());
        Response result = (Response) future.get();
        assertNotNull(result);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatus());

        assertTrue(response.cancel());

        // cancel when RESUMED
        final AsyncResponse response1 = new AsyncResponseImpl(new CompletableFuture<>());
        response1.resume(new Object());
        assertFalse(response1.cancel());
    }

    @Test
    void testCancel2() throws Throwable {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final AsyncResponse response = new AsyncResponseImpl(future);
        assertTrue(response.isSuspended());
        assertFalse(response.isDone());
        assertFalse(response.isCancelled());
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertFalse(future.isCancelled());

        assertTrue(response.cancel(3));
        assertTrue(future.isDone());
        Response result = (Response) future.get();
        assertNotNull(result);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatus());
        assertEquals(3, result.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));

        assertTrue(response.cancel(30));

        // cancel when RESUMED
        final AsyncResponse response1 = new AsyncResponseImpl(new CompletableFuture<>());
        response1.resume(new Object());
        assertFalse(response1.cancel(300));
    }

    @Test
    void testCancel3() throws Throwable {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final AsyncResponse response = new AsyncResponseImpl(future);
        assertTrue(response.isSuspended());
        assertFalse(response.isDone());
        assertFalse(response.isCancelled());
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertFalse(future.isCancelled());

        final Date date = new Date();
        assertTrue(response.cancel(date));
        assertTrue(future.isDone());
        Response result = (Response) future.get();
        assertNotNull(result);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatus());
        assertSame(date, result.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));

        assertTrue(response.cancel(30));

        // cancel when RESUMED
        final AsyncResponse response1 = new AsyncResponseImpl(new CompletableFuture<>());
        response1.resume(new Object());
        assertFalse(response1.cancel(date));
    }

    @Test
    void setTimeout() throws Throwable {
        // response has been resumed.
        final AsyncResponse response1 = new AsyncResponseImpl(new CompletableFuture<>());
        response1.resume(new Object());
        assertFalse(response1.setTimeout(100, TimeUnit.SECONDS));

        // timeout handler has not been set.
        final AsyncResponseImpl response2 = new AsyncResponseImpl(new CompletableFuture<>());
        assertTrue(response2.setTimeout(100, TimeUnit.MICROSECONDS));
        AsyncResponseImpl.TimeoutTask task21 = response2.timeoutTask();
        assertNotNull(task21);
        assertEquals(100, task21.timeout);
        assertEquals(TimeUnit.MICROSECONDS, task21.unit);
        assertNotNull(task21.task);
        assertNull(task21.handler);

        // timeout handler has been set.
        final TimeoutHandler handler2 = asyncResponse -> { };
        response2.setTimeoutHandler(handler2);
        response2.setTimeout(500, TimeUnit.SECONDS);
        AsyncResponseImpl.TimeoutTask task22 = response2.timeoutTask();
        assertNotNull(task22);
        assertEquals(500, task22.timeout);
        assertEquals(TimeUnit.SECONDS, task22.unit);
        assertSame(handler2, task22.handler);
        final Timeout timeout22 = task22.task;
        assertNotNull(timeout22);

        // update timeout handler to -1
        assertFalse(timeout22.isCancelled());
        response2.setTimeout(-1, TimeUnit.SECONDS);
        assertTrue(timeout22.isCancelled());
        AsyncResponseImpl.TimeoutTask task23 = response2.timeoutTask();
        assertNotNull(task23);
        assertEquals(-1, task23.timeout);
        assertEquals(TimeUnit.SECONDS, task23.unit);
        assertSame(handler2, task23.handler);
        assertNull(task23.task);

        // just handle timeout task
        final CompletableFuture<Object> future3 = new CompletableFuture<>();
        final AsyncResponseImpl response3 = new AsyncResponseImpl(future3);
        final CountDownLatch latch = new CountDownLatch(1);
        final TimeoutHandler handler3 = asyncResponse -> latch.countDown();
        response3.setTimeoutHandler(handler3);
        response3.setTimeout(10L, TimeUnit.MILLISECONDS);
        latch.await();
        future3.isDone();
        assertTrue(future3.isDone());
        assertNull(future3.get());
    }

    @Test
    void setTimeoutHandler() {
        // pre task is null
        final AsyncResponseImpl response = new AsyncResponseImpl(new CompletableFuture<>());
        final TimeoutHandler handler1 = asyncResponse -> { };
        response.setTimeoutHandler(handler1);
        AsyncResponseImpl.TimeoutTask task1 = response.timeoutTask();
        assertNull(task1.task);
        assertEquals(handler1, task1.handler);

        response.setTimeout(100, TimeUnit.MICROSECONDS);
        final TimeoutHandler handler2 = asyncResponse -> { };
        response.setTimeoutHandler(handler2);
        AsyncResponseImpl.TimeoutTask task2 = response.timeoutTask();
        assertNotNull(task2);
        assertNotNull(task2.task);
        assertSame(handler2, task2.handler);
    }

    @Test
    void testRegister() {
        final AsyncResponse response = new AsyncResponseImpl(new CompletableFuture<>());
        assertThrows(UnsupportedOperationException.class, () -> response.register(new Object()));
        assertThrows(UnsupportedOperationException.class, () -> response.register(TimeoutHandler.class));
        assertThrows(UnsupportedOperationException.class, () -> response.register(new Object(), new Object[0]));
        assertThrows(UnsupportedOperationException.class, () -> response.register(Object.class, new Object[0]));
    }
}

