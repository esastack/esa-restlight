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
package esa.restlight.ext.filter.cpuload;


import esa.commons.concurrent.ThreadFactories;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.LoggerUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.InternalThreadLocalMap;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuLoadProtectionFilter implements Filter {

    private static final double MAX_CPU_LOAD_VALUE = 100.0D;
    private static final double MIN_CPU_LOAD_VALUE = 0.0D;

    /**
     * Threshold of the cpu load, discard new connections when current cpu load is over than it.
     */
    private final double cpuLoadThreshold;

    /**
     * Min discard rate.
     */
    private final double initialDiscardRate;
    private final double discardRateUnit;

    /**
     * current cpu load value. A value of 0.0 means that all CPUs were idle during the recent period of time observed,
     * while a value of 100.0 means that all CPUs were actively running 100% of the time during the recent period being
     * observed.
     */
    volatile double currentCpuLoad;

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(1,
            ThreadFactories.namedThreadFactory("Cpu-Load-Timer"));

    private static AtomicBoolean timerStarted = new AtomicBoolean(false);
    private static com.sun.management.OperatingSystemMXBean bean;

    static {
        OperatingSystemMXBean mxBean = ManagementFactory
                .getOperatingSystemMXBean();
        if (mxBean instanceof com.sun.management.OperatingSystemMXBean) {
            bean = (com.sun.management.OperatingSystemMXBean) mxBean;
        } else {
            throw new Error("Could not get 'com.sun.management.OperatingSystemMXBean'.");
        }
    }

    private CpuLoadProtectionFilter(double cpuLoadThreshold,
                                    double initialDiscardRate,
                                    double maxDiscardRate) {
        if (cpuLoadThreshold <= MIN_CPU_LOAD_VALUE || cpuLoadThreshold > MAX_CPU_LOAD_VALUE) {
            throw new IllegalArgumentException("CpuLoadThreshold must be between 0 and 100.");
        }
        if (initialDiscardRate <= MIN_CPU_LOAD_VALUE || initialDiscardRate > MAX_CPU_LOAD_VALUE) {
            throw new IllegalArgumentException("InitialDiscardRate must be between 0 and 100.");
        }
        if (maxDiscardRate <= MIN_CPU_LOAD_VALUE || maxDiscardRate > MAX_CPU_LOAD_VALUE) {
            throw new IllegalArgumentException("MaxDiscardRate must be between 0 and 100.");
        }
        if (initialDiscardRate > maxDiscardRate) {
            throw new IllegalArgumentException("InitialDiscardRate must not be over than initialDiscardRate.");
        }
        this.initialDiscardRate = initialDiscardRate;
        this.discardRateUnit = (maxDiscardRate - initialDiscardRate) / (MAX_CPU_LOAD_VALUE - cpuLoadThreshold);
        this.cpuLoadThreshold = cpuLoadThreshold;
        if (timerStarted.compareAndSet(false, true)) {
            EXECUTOR.scheduleAtFixedRate(() -> {
                double load = bean.getSystemCpuLoad() * MAX_CPU_LOAD_VALUE;
                if (load >= MIN_CPU_LOAD_VALUE && load <= MAX_CPU_LOAD_VALUE) {
                    this.currentCpuLoad = load;
                } else {
                    LoggerUtils.logErrorPeriodically("Got illegal value '{}' by com.sun.management" +
                            ".OperatingSystemMXBean.getSystemCpuLoad()", load);
                }
            }, 0L, 1L, TimeUnit.SECONDS);
        }
    }

    public static CpuLoadProtectionFilter newFilter(double cpuLoadThreshold,
                                                    double initialDiscardRate,
                                                    double maxDiscardRate) {
        return new CpuLoadProtectionFilter(cpuLoadThreshold, initialDiscardRate, maxDiscardRate);
    }

    @Override
    public boolean onConnected(ChannelHandlerContext ctx) {
        double current = currentCpuLoad;
        if (current > cpuLoadThreshold
                && InternalThreadLocalMap.get().random().nextDouble(MAX_CPU_LOAD_VALUE) < getDiscardRate(current)) {
            final String conn = ctx.channel().toString();
            ctx.channel().close();
            LoggerUtils.logErrorPeriodically(
                    "Connection({}) discarded because cpu load (current: {}) is over than {}",
                    conn, current, cpuLoadThreshold);
            return false;
        }
        return true;
    }

    @Override
    public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
        return chain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 300;
    }

    /**
     * Compute the discard rate.
     *
     * @param current current cpu load
     *
     * @return discard rate
     */
    private double getDiscardRate(double current) {
        // y = ax + b
        // the discard rate and the cpu load are in a direct ratio
        // a means the discardRateUnit
        // b means the initialDiscardRate
        // x means the difference between cpuLoadThreshold and current cpu load
        return initialDiscardRate + discardRateUnit * (current - cpuLoadThreshold);
    }

}
