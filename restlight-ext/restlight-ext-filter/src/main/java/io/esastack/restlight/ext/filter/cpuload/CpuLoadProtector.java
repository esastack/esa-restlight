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
package io.esastack.restlight.ext.filter.cpuload;

import esa.commons.concurrent.ThreadFactories;
import io.esastack.restlight.core.server.Connection;
import io.esastack.restlight.core.server.handler.ConnectionInitHandler;
import io.esastack.restlight.core.util.LoggerUtils;
import io.netty.util.internal.InternalThreadLocalMap;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuLoadProtector implements ConnectionInitHandler {

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

    private static final AtomicBoolean timerStarted = new AtomicBoolean(false);
    private static final com.sun.management.OperatingSystemMXBean bean;

    static {
        OperatingSystemMXBean mxBean = ManagementFactory
                .getOperatingSystemMXBean();
        if (mxBean instanceof com.sun.management.OperatingSystemMXBean) {
            bean = (com.sun.management.OperatingSystemMXBean) mxBean;
        } else {
            throw new Error("Could not get 'com.sun.management.OperatingSystemMXBean'.");
        }
    }

    private CpuLoadProtector(double cpuLoadThreshold,
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

    public static CpuLoadProtector newFilter(double cpuLoadThreshold,
                                             double initialDiscardRate,
                                             double maxDiscardRate) {
        return new CpuLoadProtector(cpuLoadThreshold, initialDiscardRate, maxDiscardRate);
    }

    @Override
    public void onConnectionInit(Connection connection) {
        double current = currentCpuLoad;
        if (current > cpuLoadThreshold
                && InternalThreadLocalMap.get().random().nextDouble(MAX_CPU_LOAD_VALUE) < getDiscardRate(current)) {
            final String conn = connection.toString();
            connection.close();
            LoggerUtils.logErrorPeriodically(
                    "Connection({}) discarded because cpu load (current: {}) is over than {}",
                    conn, current, cpuLoadThreshold);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 300;
    }

    /**
     * Compute the discard rate.
     *
     * @param current current cpu load
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
