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
package esa.restlight.server.util;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromiseUtilsTest {

    @Test
    void testSetSuccess() {
        final Promise<Void> promise0 = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        PromiseUtils.setSuccess(promise0);
        assertTrue(promise0.isSuccess());

        final Promise<Void> promise1 = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        PromiseUtils.setSuccess(promise1, true);
        assertTrue(promise1.isSuccess());

        final CompletableFuture<Void> future0 = new CompletableFuture<>();
        PromiseUtils.setSuccess(future0);
        assertTrue(future0.isDone());
        assertFalse(future0.isCompletedExceptionally());

        final CompletableFuture<Void> future1 = new CompletableFuture<>();
        PromiseUtils.setSuccess(future1, true);
        assertTrue(future1.isDone());
        assertFalse(future1.isCompletedExceptionally());
    }

    @Test
    void testSetFailure() {
        final RuntimeException th = new RuntimeException();

        final Promise<Void> promise0 = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        PromiseUtils.setFailure(promise0, th);
        assertFalse(promise0.isSuccess());
        assertSame(th, promise0.cause());

        final Promise<Void> promise1 = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        PromiseUtils.setFailure(promise1, th, true);
        assertFalse(promise1.isSuccess());
        assertSame(th, promise1.cause());

        final CompletableFuture<Void> future0 = new CompletableFuture<>();
        PromiseUtils.setFailure(future0, th);
        assertTrue(future0.isDone());
        assertTrue(future0.isCompletedExceptionally());
    }

}

