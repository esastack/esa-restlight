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
package io.esastack.restlight.starter.actuator.meter;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.spring.util.RestlightBizExecutorAware;
import io.esastack.restlight.spring.util.RestlightDeployContextAware;
import io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestlightBizThreadPoolBinder implements RestlightBizExecutorAware, RestlightDeployContextAware {

    private DeployContext deployContext;
    private final AutoRestlightServerOptions config;
    private final MeterRegistry registry;
    private static final String CATEGORY = "category";

    public RestlightBizThreadPoolBinder(MeterRegistry registry,
                                        AutoRestlightServerOptions config) {
        Checks.checkNotNull(registry);
        Checks.checkNotNull(config);
        this.registry = registry;
        this.config = config;
    }

    @Override
    public void setRestlightBizExecutor(Executor bizExecutor) {
        final String restlightBizThread = "restlight.biz.thread";
        if (bizExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) bizExecutor;
            createBizThreadPoolGauge(restlightBizThread, registry, executor);
            Gauge.builder(restlightBizThread, config, (c) -> c.getBizThreads().getBlockingQueueLength())
                    .tag(CATEGORY, "biz")
                    .tag("id", "queue.length")
                    .register(registry);
            if (deployContext != null) {
                Optional<DispatcherHandler> dispatcherHandler =
                        deployContext.dispatcherHandler();
                dispatcherHandler.ifPresent(handler ->
                        FunctionCounter.builder(restlightBizThread, handler, DispatcherHandler::rejectCount)
                                .tag(CATEGORY, "biz")
                                .tag("id", "reject.task.count")
                                .register(registry));
            } else {
                Counter.builder(restlightBizThread)
                        .tag(CATEGORY, "biz")
                        .tag("id", "reject.task.count")
                        .register(registry);
            }
        }
    }

    @Override
    public void setDeployContext(DeployContext ctx) {
        this.deployContext = ctx;
    }

    private void createBizThreadPoolGauge(String restlightBizThread,
                                          MeterRegistry registry,
                                          ThreadPoolExecutor executor) {
        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getCorePoolSize)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "core.pool.size")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getMaximumPoolSize)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "max.pool.size")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, e -> e.getKeepAliveTime(TimeUnit.SECONDS))
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "keep.alive.time.seconds")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getActiveCount)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "active.count")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getPoolSize)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "pool.size")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getLargestPoolSize)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "largest.pool.size")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getTaskCount)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "task.count")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, e -> e.getQueue().size())
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "queue.count")
                .register(registry);

        Gauge.builder(restlightBizThread, executor, ThreadPoolExecutor::getCompletedTaskCount)
                .strongReference(true)
                .tag(CATEGORY, "biz")
                .tag("id", "completed.task.count")
                .register(registry);
    }
}
