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

import esa.commons.concurrent.DirectExecutor;
import esa.restlight.server.config.TimeoutOptions;

import java.util.concurrent.Executor;

public final class Schedulers {

    /**
     * Reserved word. Please do not use it as your own {@link Scheduler#name()}
     */
    public static final String IO = "IO";
    /**
     * Reserved word. Please do not use it as your own {@link Scheduler#name()}
     */
    public static final String BIZ = "BIZ";

    private static final Scheduler IO_SCHEDULER = fromExecutor(IO, DirectExecutor.INSTANCE);
    private static final Scheduler BIZ_SCHEDULER = fromExecutor(BIZ, r -> {
    });

    /**
     * IO {@link Scheduler}
     *
     * @return io scheduler
     */
    public static Scheduler io() {
        return IO_SCHEDULER;
    }

    /**
     * BIZ {@link Scheduler}, this is just a marker instance of biz {@link Scheduler}, the real biz {@link Scheduler}
     * will be instantiate by the configuration and will be replaced when server is about to starting.
     *
     * @return biz scheduler
     */
    public static Scheduler biz() {
        return BIZ_SCHEDULER;
    }

    /**
     * Whether the given {@link Scheduler} is a instance of biz {@link Scheduler}.
     * <p>
     * Note: this method will always returns {@code true} if the name of give {@link Scheduler} is {@link
     * Schedulers#BIZ}
     *
     * @param scheduler scheduler
     *
     * @return {@code true} if given scheduler is biz scheduler, otherwise {@code false}.
     */
    public static boolean isBiz(Scheduler scheduler) {
        return BIZ.equals(scheduler.name());
    }

    /**
     * Creates an instance of {@link Scheduler} by given {@code name} and {@code executor}.
     *
     * @param name     name of scheduler
     * @param executor underlying executor
     *
     * @return delegated scheduler
     */
    public static ExecutorScheduler fromExecutor(String name, Executor executor) {
        return new ExecutorSchedulerImpl(name, executor);
    }

    /**
     * Wraps the given {@code scheduler} by {@code timeoutOptions}.
     *
     * @param scheduler scheduler
     * @param timeoutOptions    timeout options
     * @return  scheduler
     */
    public static Scheduler wrapped(Scheduler scheduler, TimeoutOptions timeoutOptions) {
        if (timeoutOptions != null && timeoutOptions.getMillisTime() > 0L && timeoutOptions.getType() != null) {
            TimeoutScheduler wrapped;
            if (TimeoutOptions.Type.TTFB == timeoutOptions.getType()) {
                wrapped = new TTFBTimeoutScheduler(scheduler, timeoutOptions);
            } else {
                wrapped = new TimeoutScheduler(scheduler, timeoutOptions);
            }
            return scheduler instanceof ExecutorScheduler
                    ? new TimeoutExecutorScheduler((ExecutorScheduler) scheduler, wrapped)
                    : wrapped;
        } else {
            return scheduler;
        }
    }

    static boolean isIo(Scheduler scheduler) {
        return IO.equals(scheduler.name());
    }

    private Schedulers() {
        throw new Error();
    }
}
