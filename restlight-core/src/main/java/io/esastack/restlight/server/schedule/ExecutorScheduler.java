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
package io.esastack.restlight.server.schedule;

import java.util.concurrent.Executor;

/**
 * Indicates that this {@link Scheduler} is implemented by a underlying instance of {@link Executor}.
 */
public interface ExecutorScheduler extends Scheduler {

    /**
     * Returns an instance of {@link Executor} that current {@link Scheduler} is underlying or current instance itself
     * if there's no underlying {@link Executor}.
     *
     * @return carrying {@link Executor} or current instance itself.
     */
    Executor executor();

}
