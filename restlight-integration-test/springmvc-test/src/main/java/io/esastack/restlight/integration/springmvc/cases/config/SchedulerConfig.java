/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.springmvc.cases.config;

import esa.commons.concurrent.ThreadFactories;
import io.esastack.restlight.core.config.TimeoutOptions;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class SchedulerConfig {

    @Bean
    public Scheduler customScheduler() {
        return Schedulers.fromExecutor("custom",
                Executors.newSingleThreadExecutor(ThreadFactories.namedThreadFactory("custom")));
    }

    @Bean
    public Scheduler failFastQueuedScheduler() {
        Executor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(4),
                ThreadFactories.namedThreadFactory("fail-fast-queued"));
        executor.execute(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // ignore it.
            }
        });
        TimeoutOptions timeoutOptions = new TimeoutOptions();
        timeoutOptions.setType(TimeoutOptions.Type.QUEUED);
        timeoutOptions.setTimeMillis(100);
        return Schedulers.wrapped(Schedulers.fromExecutor("fail-fast-queued", executor), timeoutOptions);
    }

    @Bean
    public Scheduler failFastFFTBScheduler() {
        Executor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(4),
                ThreadFactories.namedThreadFactory("fail-fast-ttfb"));
        TimeoutOptions timeoutOptions = new TimeoutOptions();
        timeoutOptions.setType(TimeoutOptions.Type.TTFB);
        timeoutOptions.setTimeMillis(20);
        return Schedulers.wrapped(Schedulers.fromExecutor("fail-fast-ttfb", executor), timeoutOptions);
    }
}
