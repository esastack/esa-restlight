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
package esa.restlight.server.schedule;

import esa.commons.Checks;

import java.util.concurrent.Executor;

class TimeoutExecutorScheduler implements ExecutorScheduler {

    private final TimeoutScheduler scheduler;
    private final Executor executor;

    TimeoutExecutorScheduler(TimeoutScheduler scheduler, Executor executor) {
        Checks.checkNotNull(scheduler, "scheduler");
        Checks.checkNotNull(executor, "executor");
        this.scheduler = scheduler;
        this.executor = executor;
    }

    @Override
    public String name() {
        return scheduler.name();
    }

    @Override
    public void schedule(Runnable cmd) {
        scheduler.schedule(cmd);
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    public Executor executor() {
        return executor;
    }
}

