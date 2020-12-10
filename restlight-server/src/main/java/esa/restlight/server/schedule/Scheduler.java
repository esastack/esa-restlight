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

/**
 * Uses to schedule a task(probably a request task). Every {@link Scheduler} should have a name which should be returned
 * by {@link #name()}.
 */
public interface Scheduler {

    /**
     * Name of the current {@link Scheduler}.
     *
     * @return name.
     */
    String name();

    /**
     * Executes the given command at some time in the future. The command may execute in a new thread, in a pooled
     * thread, or in the calling thread, at the discretion of the {@code Executor} implementation.
     *
     * @param cmd cmd
     */
    void schedule(Runnable cmd);

    /**
     * Shutdowns the Scheduler, if there are
     */
    void shutdown();
}
