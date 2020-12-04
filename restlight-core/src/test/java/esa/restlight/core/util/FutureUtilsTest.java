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
package esa.restlight.core.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import esa.restlight.server.util.Futures;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class FutureUtilsTest {

    private static final ListeningExecutorService guavaExecutor =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));

    private static final EventExecutor nettyExecutor = new DefaultEventExecutor();

    @Test
    void testGetFutureResult() {
        assertNull(FutureUtils.getFutureResult(null));
        final Object obj = new Object();
        assertEquals(obj, FutureUtils.getFutureResult(obj));

        assertEquals("foo", FutureUtils.getFutureResult(CompletableFuture.completedFuture("foo")));
        assertTrue(FutureUtils.hasGuavaFuture());
        assertEquals("foo", FutureUtils.getFutureResult(guavaExecutor.submit(() -> "foo")));
        assertEquals("foo", FutureUtils.getFutureResult(new DefaultPromise<>(nettyExecutor).setSuccess("foo")));

        CompletableFuture<String> cf = Futures.completedExceptionally(new IllegalStateException());
        assertEquals(cf, FutureUtils.getFutureResult(cf));
    }

    @Test
    void testTransferNettyFuture() {
        final CompletableFuture<String> cf = FutureUtils
                .transferNettyFuture(new DefaultPromise<String>(nettyExecutor).setSuccess("foo"));
        assertNotNull(cf);
        assertEquals("foo", cf.join());

        final IllegalStateException e = new IllegalStateException("bar");
        final CompletableFuture<String> cf1 = FutureUtils
                .transferNettyFuture(new DefaultPromise<String>(nettyExecutor).setFailure(e));
        assertNotNull(cf1);
        try {
            cf1.join();
        } catch (Exception ex) {
            Throwable err = Futures.unwrapCompletionException(ex);
            assertEquals(e, err);
            assertEquals("bar", err.getMessage());
        }
    }

    @Test
    void testTransferListenableFuture() {
        final CompletableFuture<String> cf = FutureUtils
                .transferListenableFuture(guavaExecutor.submit(() -> "foo"));
        assertNotNull(cf);
        assertEquals("foo", cf.join());

        final IllegalStateException e = new IllegalStateException("bar");
        final CompletableFuture<String> cf1 = FutureUtils
                .transferListenableFuture(guavaExecutor.submit(() -> {
                    throw e;
                }));
        assertNotNull(cf1);
        try {
            cf1.join();
        } catch (Exception ex) {
            Throwable err = Futures.unwrapCompletionException(ex);
            assertEquals(e, err);
            assertEquals("bar", err.getMessage());
        }
    }

}
