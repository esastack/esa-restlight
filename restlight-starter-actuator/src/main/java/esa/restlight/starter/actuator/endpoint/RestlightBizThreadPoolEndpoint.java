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
package esa.restlight.starter.actuator.endpoint;

import esa.commons.Checks;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.spring.util.RestlightBizExecutorAware;
import esa.restlight.spring.util.RestlightDeployContextAware;
import esa.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
@Endpoint(id = "bizthreadpool")
public class RestlightBizThreadPoolEndpoint implements RestlightBizExecutorAware, RestlightDeployContextAware {

    private static final Logger logger =
            LoggerFactory.getLogger(RestlightBizThreadPoolEndpoint.class);
    private DeployContext<? extends RestlightOptions> deployContext;

    @Autowired(required = false)
    AutoRestlightServerOptions config;
    private Executor bizExecutor;

    @ReadOperation
    public ThreadPoolMetric threadPoolMetric() {
        if (bizExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) bizExecutor;
            ThreadPoolMetric metric = new ThreadPoolMetric();
            metric.setCorePoolSize(executor.getCorePoolSize());
            metric.setMaxPoolSize(executor.getMaximumPoolSize());
            metric.setQueueLength(config.getBlockingQueueLength());
            metric.setKeepAliveTimeSeconds(executor.getKeepAliveTime(TimeUnit.SECONDS));
            metric.setActiveCount(executor.getActiveCount());
            metric.setPoolSize(executor.getPoolSize());
            metric.setLargestPoolSize(executor.getLargestPoolSize());
            metric.setTaskCount(executor.getTaskCount());
            metric.setQueueCount(executor.getQueue().size());
            metric.setCompletedTaskCount(executor.getCompletedTaskCount());
            if (deployContext != null) {
                deployContext.dispatcherHandler()
                        .ifPresent(dispatcherHandler -> metric.setRejectTaskCount(dispatcherHandler.rejectCount()));
            } else {
                metric.setRejectTaskCount(-1L);
            }
            return metric;
        }
        return null;
    }

    @WriteOperation
    public synchronized void update(int corePoolSize, int maxPoolSize) {
        Checks.checkArg(corePoolSize > 0,
                "Core pool size must be over than 0");
        Checks.checkArg(maxPoolSize >= corePoolSize,
                "Max pool size must not be less than core pool size.");
        if (bizExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) bizExecutor;
            executor.setCorePoolSize(corePoolSize);
            executor.setMaximumPoolSize(maxPoolSize);
            logger.info("Set biz thread pool size to core: {}, max: {}", corePoolSize, maxPoolSize);
        }
    }

    static class ThreadPoolMetric implements Serializable {

        private static final long serialVersionUID = 9177590225061508912L;

        /**
         * core biz thread count
         */
        private int corePoolSize;

        /**
         * maximum biz thread count
         */
        private int maxPoolSize;

        /**
         * maximum waiting queue length
         */
        private int queueLength;

        /**
         * thread pool keepAlive time(default to 180s)
         */
        private long keepAliveTimeSeconds;
        private int activeCount;
        private int poolSize;
        private int largestPoolSize;
        private long taskCount;
        private long queueCount;
        private long completedTaskCount;
        private long rejectTaskCount;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueLength() {
            return queueLength;
        }

        public void setQueueLength(int queueLength) {
            this.queueLength = queueLength;
        }

        public long getKeepAliveTimeSeconds() {
            return keepAliveTimeSeconds;
        }

        public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
            this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }

        public int getLargestPoolSize() {
            return largestPoolSize;
        }

        public void setLargestPoolSize(int largestPoolSize) {
            this.largestPoolSize = largestPoolSize;
        }

        public long getTaskCount() {
            return taskCount;
        }

        public void setTaskCount(long taskCount) {
            this.taskCount = taskCount;
        }

        public long getQueueCount() {
            return queueCount;
        }

        public void setQueueCount(long queueCount) {
            this.queueCount = queueCount;
        }

        public long getCompletedTaskCount() {
            return completedTaskCount;
        }

        public void setCompletedTaskCount(long completedTaskCount) {
            this.completedTaskCount = completedTaskCount;
        }

        public long getRejectTaskCount() {
            return rejectTaskCount;
        }

        public void setRejectTaskCount(long rejectTaskCount) {
            this.rejectTaskCount = rejectTaskCount;
        }
    }


    @Override
    public void setRestlightBizExecutor(Executor bizExecutor) {
        this.bizExecutor = bizExecutor;
    }

    @Override
    public void setDeployContext(DeployContext<? extends RestlightOptions> ctx) {
        this.deployContext = ctx;
    }
}
