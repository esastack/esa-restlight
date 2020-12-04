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

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class SchedulersTest {

    @Test
    void testSimple() {
        assertTrue(Schedulers.isIo(Schedulers.io()));
        assertTrue(Schedulers.isBiz(Schedulers.biz()));
        assertTrue(Schedulers.isBiz(Schedulers.fromExecutor(Schedulers.BIZ, Executors.newCachedThreadPool())));

        assertFalse(Schedulers.isIo(Schedulers.biz()));
        assertFalse(Schedulers.isBiz(Schedulers.io()));
    }

    @Test
    void testFromExecutor() {
        final String name = "foo";
        final ExecutorService e = Executors.newCachedThreadPool();
        final ExecutorScheduler scheduler = Schedulers.fromExecutor(name, e);
        assertNotNull(scheduler);
        assertEquals(name, scheduler.name());
        assertEquals(e, scheduler.executor());
        scheduler.shutdown();
        assertTrue(e.isShutdown());

        final Scheduler scheduler1 = Schedulers.fromExecutor("another", e);
        final Scheduler scheduler2 = Schedulers.fromExecutor(name, Executors.newCachedThreadPool());
        assertEquals(scheduler, scheduler2);
        assertEquals(scheduler.hashCode(), scheduler2.hashCode());
        assertEquals(scheduler.toString(), scheduler2.toString());
        assertNotEquals(scheduler, scheduler1);
    }

}
