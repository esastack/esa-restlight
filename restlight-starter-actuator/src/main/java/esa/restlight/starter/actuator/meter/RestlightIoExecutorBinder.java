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
package esa.restlight.starter.actuator.meter;

import esa.commons.Checks;
import esa.restlight.spring.util.RestlightIoExecutorAware;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.netty.channel.EventLoopGroup;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static esa.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint.EventLoopGroupMetrics;
import static esa.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint.EventLoopMetrics;
import static esa.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint.getMetrics;


public class RestlightIoExecutorBinder implements RestlightIoExecutorAware {

    private final MeterRegistry registry;

    private static final String CATEGORY = "category";

    public RestlightIoExecutorBinder(MeterRegistry registry) {
        Checks.checkNotNull(registry);
        this.registry = registry;
    }

    @Override
    public void setRestlightIoExecutor(Executor ioExecutor) {
        if (ioExecutor instanceof EventLoopGroup) {
            createIoExecutorMetrics(registry, ioExecutor);
        }
    }

    private static void createIoExecutorMetrics(MeterRegistry registry, Executor ioExecutor) {
        EventLoopGroupMetrics metrics = getMetrics(ioExecutor);
        assert metrics != null;

        final String restlightIoThread = "restlight.io.thread";
        createIoGlobalMetric(restlightIoThread, registry, ioExecutor, metrics);

        List<EventLoopMetrics> childExecutors = metrics.getChildExecutors();

        if (childExecutors != null && childExecutors.size() > 0) {
            Gauge.builder(restlightIoThread, metrics,
                    m -> metrics.getChildExecutors().get(0).getMaxPendingTasks())
                    .tag(CATEGORY, "io")
                    .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "max.pending.tasks")))
                    .register(registry);
            Gauge.builder(restlightIoThread, metrics,
                    m -> metrics.getChildExecutors().get(0).getIoRatio())
                    .tag(CATEGORY, "io")
                    .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "io.ratio")))
                    .register(registry);
            Gauge.builder(restlightIoThread, metrics,
                    m -> metrics.getChildExecutors().get(0).getThreadPriority())
                    .tag(CATEGORY, "io")
                    .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "thread.priority")))
                    .register(registry);
            for (int i = 0; i < childExecutors.size(); i++) {
                int finalI = i;
                EventLoopMetrics eventLoopMetrics = childExecutors.get(i);
                Gauge.builder(restlightIoThread, ioExecutor,
                        io -> Checks.checkNotNull(getMetrics(ioExecutor))
                                .getChildExecutors()
                                .get(finalI)
                                .getPendingTasks())
                        .strongReference(true)
                        .tag(CATEGORY, "io")
                        .tags(Arrays.asList(Tag.of("threadName", eventLoopMetrics.getThreadName()),
                                Tag.of("id", "pending.tasks")))
                        .register(registry);

                Gauge.builder(restlightIoThread, ioExecutor,
                        io -> Checks.checkNotNull(getMetrics(ioExecutor))
                                .getChildExecutors()
                                .get(finalI)
                                .getTaskQueueSize())
                        .strongReference(true)
                        .tag(CATEGORY, "io")
                        .tags(Arrays.asList(Tag.of("threadName", eventLoopMetrics.getThreadName()),
                                Tag.of("id", "task.queue.size")))
                        .register(registry);

                Gauge.builder(restlightIoThread, ioExecutor,
                        io -> "RUNNABLE".equals(Checks.checkNotNull(getMetrics(ioExecutor))
                                .getChildExecutors()
                                .get(finalI)
                                .getThreadState()) ? 1 : 0)
                        .strongReference(true)
                        .tag(CATEGORY, "io")
                        .tags(Arrays.asList(Tag.of("threadName", eventLoopMetrics.getThreadName()),
                                Tag.of("id", "thread.state")))
                        .register(registry);
            }
        }
    }

    private static void createIoGlobalMetric(String restlightIoThread,
                                             MeterRegistry registry,
                                             Executor ioExecutor,
                                             EventLoopGroupMetrics metrics) {
        Gauge.builder(restlightIoThread, metrics, EventLoopGroupMetrics::getThreadCount)
                .strongReference(true)
                .tag(CATEGORY, "io")
                .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "thread.count")))
                .description("Restlight IO threads metrics info")
                .register(registry);

        Gauge.builder(restlightIoThread, ioExecutor, io -> Checks.checkNotNull(getMetrics(ioExecutor))
                .getPendingTasks())
                .tag(CATEGORY, "io")
                .strongReference(true)
                .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "pending.tasks")))
                .register(registry);

        Gauge.builder(restlightIoThread, ioExecutor,
                io -> Checks.checkNotNull(getMetrics(ioExecutor)).getThreadStates().get("RUNNABLE") != null
                        ? Checks.checkNotNull(getMetrics(ioExecutor)).getThreadStates().get("RUNNABLE")
                        : Checks.checkNotNull(getMetrics(ioExecutor)).getThreadStates().get("TERMINATED"))
                .tag(CATEGORY, "io")
                .strongReference(true)
                .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "thread.states.runnable")))
                .register(registry);

        Gauge.builder(restlightIoThread, ioExecutor,
                io -> Checks.checkNotNull(getMetrics(ioExecutor)).isTerminated() ? 1 : 0)
                .tag(CATEGORY, "io")
                .strongReference(true)
                .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "terminated")))
                .register(registry);

        Gauge.builder(restlightIoThread, ioExecutor,
                io -> Checks.checkNotNull(getMetrics(ioExecutor)).isShutDown() ? 1 : 0)
                .tag(CATEGORY, "io")
                .strongReference(true)
                .tags(Arrays.asList(Tag.of("threadName", "ioGlobal"), Tag.of("id", "shutdown")))
                .register(registry);
    }
}
