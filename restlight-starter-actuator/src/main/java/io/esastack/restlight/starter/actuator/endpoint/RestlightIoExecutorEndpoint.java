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
package io.esastack.restlight.starter.actuator.endpoint;

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.spring.util.RestlightIoExecutorAware;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;

import static io.esastack.restlight.starter.actuator.endpoint.Utils.findField;

@Endpoint(id = "ioexecutor")
public class RestlightIoExecutorEndpoint implements RestlightIoExecutorAware {

    private Executor ioExecutor;

    @ReadOperation(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public synchronized EventLoopGroupMetrics ioExecutorMetrics() {
        return getMetrics(ioExecutor);
    }

    public static EventLoopGroupMetrics getMetrics(Object eventLoopGroup) {
        if (eventLoopGroup instanceof EventLoopGroup) {
            EventLoopGroupMetrics metrics = new EventLoopGroupMetrics();
            if (eventLoopGroup instanceof MultithreadEventLoopGroup) {
                MultithreadEventLoopGroup multiGroup = (MultithreadEventLoopGroup) eventLoopGroup;
                metrics.setShutDown(multiGroup.isShutdown());
                metrics.setTerminated(multiGroup.isTerminated());
                Iterator<EventExecutor> iterator = multiGroup.iterator();
                int executorNum = 0;
                int pendingTasks = 0;
                while (iterator.hasNext()) {
                    //get metrics for single scheduler
                    EventExecutor executor = iterator.next();
                    EventLoopMetrics subMetrics = new EventLoopMetrics();
                    if (executor instanceof SingleThreadEventLoop) {
                        SingleThreadEventLoop exe = (SingleThreadEventLoop) executor;
                        subMetrics.setPendingTasks(exe.pendingTasks());
                        pendingTasks += subMetrics.getPendingTasks();
                        findField(exe, "maxPendingTasks", int.class).ifPresent(subMetrics::setMaxPendingTasks);
                        findField(exe, "ioRatio", int.class).ifPresent(subMetrics::setIoRatio);
                        findField(exe, "taskQueue", Queue.class)
                                .ifPresent(taskQueue -> subMetrics.setTaskQueueSize(taskQueue.size()));
                        ThreadProperties threadProperties = exe.threadProperties();
                        subMetrics.setThreadName(threadProperties.name());
                        subMetrics.setThreadPriority(threadProperties.priority());
                        subMetrics.setThreadState(threadProperties.state().name());
                    }
                    executorNum++;
                    metrics.childExecutors.add(subMetrics);
                }
                metrics.setThreadCount(executorNum);
                metrics.setPendingTasks(pendingTasks);
                //compute thread state map
                Map<String, Integer> threadStateStats = new HashMap<>(16);
                metrics.getChildExecutors().forEach(e -> {
                    Integer num = threadStateStats.get(e.threadState);
                    threadStateStats.put(e.threadState, num == null ? 1 : ++num);
                });
                metrics.setThreadStates(threadStateStats);
            }
            return metrics;
        } else {
            return null;
        }
    }

    @Override
    public void setRestlightIoExecutor(Executor ioExecutor) {
        this.ioExecutor = ioExecutor;
    }

    public static class EventLoopGroupMetrics implements Serializable {
        private static final long serialVersionUID = -5280249911995445067L;

        private final List<EventLoopMetrics> childExecutors = new ArrayList<>();
        private boolean isShutDown;
        private boolean isTerminated;
        private int threadCount;
        protected int pendingTasks;
        private Map<String, Integer> threadStates;

        public boolean isShutDown() {
            return isShutDown;
        }

        public void setShutDown(boolean shutDown) {
            isShutDown = shutDown;
        }

        public boolean isTerminated() {
            return isTerminated;
        }

        public void setTerminated(boolean terminated) {
            isTerminated = terminated;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }

        public int getPendingTasks() {
            return pendingTasks;
        }

        public void setPendingTasks(int pendingTasks) {
            this.pendingTasks = pendingTasks;
        }

        public List<EventLoopMetrics> getChildExecutors() {
            return childExecutors;
        }

        public Map<String, Integer> getThreadStates() {
            return threadStates;
        }

        public void setThreadStates(Map<String, Integer> threadStates) {
            this.threadStates = threadStates;
        }
    }

    public static class EventLoopMetrics implements Serializable {

        private static final long serialVersionUID = 3399435404412955429L;

        private int pendingTasks;
        private int maxPendingTasks;
        private int ioRatio;
        private int taskQueueSize;
        private int tailTaskQueueSize;
        private String threadName;
        private int threadPriority;
        private String threadState;

        public int getPendingTasks() {
            return pendingTasks;
        }

        public void setPendingTasks(int pendingTasks) {
            this.pendingTasks = pendingTasks;
        }

        public int getMaxPendingTasks() {
            return maxPendingTasks;
        }

        public void setMaxPendingTasks(int maxPendingTasks) {
            this.maxPendingTasks = maxPendingTasks;
        }

        public int getIoRatio() {
            return ioRatio;
        }

        public void setIoRatio(int ioRatio) {
            this.ioRatio = ioRatio;
        }

        public int getTaskQueueSize() {
            return taskQueueSize;
        }

        public void setTaskQueueSize(int taskQueueSize) {
            this.taskQueueSize = taskQueueSize;
        }

        public int getTailTaskQueueSize() {
            return tailTaskQueueSize;
        }

        public void setTailTaskQueueSize(int tailTaskQueueSize) {
            this.tailTaskQueueSize = tailTaskQueueSize;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        public int getThreadPriority() {
            return threadPriority;
        }

        public void setThreadPriority(int threadPriority) {
            this.threadPriority = threadPriority;
        }

        public String getThreadState() {
            return threadState;
        }

        public void setThreadState(String threadState) {
            this.threadState = threadState;
        }
    }
}
