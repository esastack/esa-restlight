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
package esa.restlight.server.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class FuturesTest {

    @Test
    void testCompletedExceptionally() {
        final Exception ex = new IllegalStateException();
        final CompletableFuture<String> cf = Futures.completedExceptionally(ex);
        assertNotNull(cf);
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertThrows(CompletionException.class, cf::join);
    }

    @Test
    void testCompletedFuture() {
        final CompletableFuture<String> cf = Futures.completedFuture();
        assertNotNull(cf);
        assertTrue(cf.isDone());
        assertNull(cf.join());

        final CompletableFuture<String> cf1 = Futures.completedFuture("foo");
        assertNotNull(cf1);
        assertTrue(cf1.isDone());
        assertEquals("foo", cf1.join());
    }

    @Test
    void testUnwrapCompletionException() {
        final Exception ex = new IllegalStateException();
        assertEquals(ex, Futures.unwrapCompletionException(new CompletionException(ex)));
        assertEquals(ex, Futures.unwrapCompletionException(new ExecutionException(ex)));
        assertEquals(ex, Futures.unwrapCompletionException(ex));
    }

}
