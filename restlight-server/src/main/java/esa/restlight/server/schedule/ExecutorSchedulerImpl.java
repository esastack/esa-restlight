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

import esa.commons.Checks;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class ExecutorSchedulerImpl implements ExecutorScheduler {

    private final String name;
    private final Executor executor;

    ExecutorSchedulerImpl(String name, Executor executor) {
        Checks.checkNotEmptyArg(name, "name");
        Checks.checkNotNull(executor, "executor");
        this.name = name;
        this.executor = executor;
    }

    @Override
    public void schedule(Runnable command) {
        executor.execute(command);
    }

    @Override
    public void shutdown() {
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Executor executor() {
        return executor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutorSchedulerImpl that = (ExecutorSchedulerImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ExecutorScheduler{name='" + name + "'}";
    }
}
