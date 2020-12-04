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
package esa.restlight.server.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LinkedFilterChainTest {

    @Test
    void testImmutable() {
        final AtomicInteger ret = new AtomicInteger(0);
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final Filter filter1 = (request, response, chain) -> {
            if (req == request && response == res) {
                ret.compareAndSet(0, 1);
            }
            return chain.doFilter(request, response);
        };
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final Filter filter2 = (request, response, chain) -> CompletableFuture.runAsync(() -> {
            if (req == request && response == res) {
                ret.compareAndSet(1, 2);
            }
        }).thenCompose(v -> chain.doFilter(req1, res1));

        final Filter filter3 = (request, response, chain) -> CompletableFuture.runAsync(() -> {
            if (request == req1 && response == res1) {
                ret.compareAndSet(2, 3);
            }
        }).thenCompose(v -> chain.doFilter(request, response));

        final LinkedFilterChain chain = LinkedFilterChain.immutable(new Filter[]{filter1, filter2, filter3},
                ((request, response) -> {
                    ret.compareAndSet(3, 4);
                    return Futures.completedFuture();
                }));
        chain.doFilter(req, res).join();
        assertEquals(4, ret.get());
    }


    @Test
    void testUncompleted() {
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final Filter filter1 = (request, response, chain) -> new CompletableFuture<>();

        final AtomicBoolean bizExecuted = new AtomicBoolean(false);
        final LinkedFilterChain chain = LinkedFilterChain.immutable(new Filter[]{filter1},
                ((request, response) -> {
                    bizExecuted.set(true);
                    return Futures.completedFuture();
                }));
        assertFalse(chain.doFilter(req, res).isDone());
        assertFalse(bizExecuted.get());
    }


    @Test
    void testBreakChain() {
        final AtomicInteger ret = new AtomicInteger(0);
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final Filter filter1 = (request, response, chain) -> CompletableFuture.runAsync(ret::incrementAndGet);
        final Filter filter2 = (request, response, chain) -> {
            ret.incrementAndGet();
            return chain.doFilter(request, response);
        };

        final AtomicBoolean bizExecuted = new AtomicBoolean(false);
        final LinkedFilterChain chain = LinkedFilterChain.immutable(new Filter[]{filter1, filter2},
                ((request, response) -> {
                    bizExecuted.set(true);
                    return Futures.completedFuture();
                }));
        chain.doFilter(req, res).join();
        assertEquals(1, ret.get());
        assertFalse(bizExecuted.get());
    }


    @Test
    void testBreakChainWithError() {
        final AtomicInteger ret = new AtomicInteger(0);
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final Filter filter1 = (request, response, chain) ->
                Futures.completedExceptionally(new IllegalArgumentException());
        final Filter filter2 = (request, response, chain) -> {
            ret.incrementAndGet();
            return chain.doFilter(request, response);
        };

        final AtomicBoolean bizExecuted = new AtomicBoolean(false);
        final LinkedFilterChain chain = LinkedFilterChain.immutable(new Filter[]{filter1, filter2},
                ((request, response) -> {
                    bizExecuted.set(true);
                    return Futures.completedFuture();
                }));
        final CompletableFuture<Void> cf = chain.doFilter(req, res);
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertEquals(0, ret.get());
        assertFalse(bizExecuted.get());
        // throw it
        assertThrows(CompletionException.class, cf::join);
    }

    @Test
    void testChangeArgs() {
        final AsyncRequest req = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        final AsyncRequest req1 = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse res1 = MockAsyncResponse.aMockResponse().build();
        final Filter filter1 = (request, response, chain) -> chain.doFilter(req1, res1);
        final LinkedFilterChain chain = LinkedFilterChain.immutable(new Filter[]{filter1},
                ((request, response) -> {
                    assertEquals(req1, request);
                    assertEquals(res1, response);
                    return Futures.completedFuture();
                }));
        chain.doFilter(req, res).join();
    }
}
