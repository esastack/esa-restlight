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
package esa.restlight.core.handler.impl;

import esa.restlight.server.util.Futures;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CompletableFutureRouteExecutionTest {

    @Test
    void testTransferFuture() {
        final CompletableFutureRouteExecution execution =
                new CompletableFutureRouteExecution(mock(RouteHandlerAdapter.class), null);
        final CompletableFuture<Void> cf = Futures.completedFuture();
        assertEquals(cf, execution.transferToFuture(cf));
    }

}
