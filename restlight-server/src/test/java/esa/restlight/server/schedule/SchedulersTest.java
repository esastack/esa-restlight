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

import esa.restlight.server.config.TimeoutOptions;
import esa.restlight.server.config.TimeoutOptionsConfigure;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testFrom() {
        final String name = "foo";
        final ExecutorService e = Executors.newCachedThreadPool();
        final ExecutorScheduler scheduler = Schedulers.fromExecutor(name, e);
        assertNotNull(scheduler);
        assertEquals(name, scheduler.name());
        assertEquals(e, scheduler.executor());
        scheduler.shutdown();
        assertTrue(e.isShutdown());

        final ExecutorScheduler scheduler1 = Schedulers.fromExecutor("another", e);
        final ExecutorScheduler scheduler2 = Schedulers.fromExecutor(name, Executors.newCachedThreadPool());
        assertEquals(scheduler, scheduler2);
        assertEquals(scheduler.hashCode(), scheduler2.hashCode());
        assertEquals(scheduler.toString(), scheduler2.toString());
        assertNotEquals(scheduler, scheduler1);
    }

    @Test
    void testWrapped() {
        final String name = "foo";
        final ExecutorService e = Executors.newCachedThreadPool();
        final ExecutorScheduler scheduler0 = Schedulers.fromExecutor(name, e);
        final Scheduler wrapped0 = Schedulers.wrapped(scheduler0, null);
        assertNotNull(wrapped0);
        assertSame(scheduler0, wrapped0);

        final ExecutorScheduler scheduler1 = Schedulers.fromExecutor(name, e);
        final Scheduler wrapped1 = Schedulers.wrapped(scheduler1, TimeoutOptionsConfigure.defaultOpts());
        assertNotNull(wrapped1);
        assertSame(scheduler1, wrapped1);

        final ExecutorScheduler scheduler2 = Schedulers.fromExecutor(name, e);
        final Scheduler wrapped2 = Schedulers.wrapped(scheduler2, TimeoutOptionsConfigure
                .newOpts().type(null).configured());
        assertNotNull(wrapped2);
        assertSame(scheduler2, wrapped2);

        // wraps to TimeoutExecutorScheduler
        final ExecutorScheduler scheduler3 = Schedulers.fromExecutor(name, e);
        final Scheduler wrapped3 = Schedulers.wrapped(scheduler3, TimeoutOptionsConfigure.newOpts()
                .timeMillis(100L).type(TimeoutOptions.Type.TTFB).configured());
        assertNotNull(wrapped3);
        assertNotSame(scheduler3, wrapped3);
        assertTrue(wrapped3 instanceof TimeoutExecutorScheduler);

        // wraps to TTFBTimeoutScheduler
        final Scheduler scheduler4 = new Scheduler() {
            @Override
            public String name() {
                return null;
            }

            @Override
            public void schedule(Runnable cmd) {

            }

            @Override
            public void shutdown() {

            }
        };
        final Scheduler wrapped4 = Schedulers.wrapped(scheduler4, TimeoutOptionsConfigure.newOpts()
                .timeMillis(100L).type(TimeoutOptions.Type.TTFB).configured());
        assertNotNull(wrapped4);
        assertNotSame(scheduler4, wrapped4);
        assertTrue(wrapped4 instanceof TTFBTimeoutScheduler);

        // wraps to TimeoutExecutorScheduler
        final ExecutorScheduler scheduler5 = Schedulers.fromExecutor(name, e);
        final Scheduler wrapped5 = Schedulers.wrapped(scheduler3, TimeoutOptionsConfigure.newOpts()
                .timeMillis(100L).configured());
        assertNotNull(wrapped5);
        assertNotSame(scheduler5, wrapped5);
        assertTrue(wrapped5 instanceof TimeoutExecutorScheduler);

        // wraps to TTFBTimeoutScheduler
        final Scheduler scheduler6 = new Scheduler() {
            @Override
            public String name() {
                return null;
            }

            @Override
            public void schedule(Runnable cmd) {

            }

            @Override
            public void shutdown() {

            }
        };
        final Scheduler wrapped6 = Schedulers.wrapped(scheduler4, TimeoutOptionsConfigure.newOpts()
                .timeMillis(100L).configured());
        assertNotNull(wrapped6);
        assertNotSame(scheduler6, wrapped6);
        assertTrue(wrapped6 instanceof TimeoutScheduler);
    }

}
