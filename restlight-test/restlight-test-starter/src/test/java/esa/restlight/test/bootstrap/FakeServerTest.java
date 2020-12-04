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
package esa.restlight.test.bootstrap;

import esa.restlight.server.handler.RestlightHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FakeServerTest {

    @Test
    void testAll() {
        final RestlightHandler handler = mock(RestlightHandler.class);
        final FakeServer fake = new FakeServer(handler);
        assertFalse(fake.isStarted());
        fake.start();
        assertTrue(fake.isStarted());
        verify(handler).onStart();
        CompletableFuture<Void> cf = CompletableFuture.runAsync(fake::await);
        assertFalse(cf.isDone());
        assertNull(fake.ioExecutor());
        assertNull(fake.bizExecutor());
        assertNull(fake.address());
        fake.shutdown();
        cf.join();
    }

}
