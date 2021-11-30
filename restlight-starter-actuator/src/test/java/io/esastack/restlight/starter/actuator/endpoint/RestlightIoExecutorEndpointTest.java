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

import io.esastack.restlight.starter.actuator.meter.RestlightIoExecutorBinder;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static io.esastack.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint.EventLoopGroupMetrics;
import static io.esastack.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint.EventLoopMetrics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RestlightIoExecutorEndpointTest {

    @Test
    void testMetrics() {
        final RestlightIoExecutorEndpoint endpoint = new RestlightIoExecutorEndpoint();
        endpoint.setRestlightIoExecutor(command -> {
        });
        assertNull(endpoint.ioExecutorMetrics());
        final EventLoopGroup io = new NioEventLoopGroup(2);
        endpoint.setRestlightIoExecutor(io);
        EventLoopGroupMetrics metrics = endpoint.ioExecutorMetrics();
        assertNotNull(metrics);
        assertEquals(io.isShutdown(), metrics.isShutDown());
        assertEquals(io.isTerminated(), metrics.isTerminated());
        assertEquals(2, metrics.getThreadCount());
        assertEquals(0, metrics.getPendingTasks());
        final List<EventLoopMetrics> childExecutors = metrics.getChildExecutors();
        assertNotNull(childExecutors);
        assertEquals(2, childExecutors.size());
        io.shutdownGracefully().awaitUninterruptibly();
        metrics = endpoint.ioExecutorMetrics();
        assertEquals(io.isShutdown(), metrics.isShutDown());
        assertEquals(io.isTerminated(), metrics.isTerminated());
    }

    @Test
    void testMetrics4Prometheus() {
        final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        final RestlightIoExecutorBinder endpoint = new RestlightIoExecutorBinder(registry);
        endpoint.setRestlightIoExecutor(command -> {
        });
        assertEquals("", registry.scrape());

        final EventLoopGroup io = new NioEventLoopGroup(2,
                new ThreadFactory() {
                    final AtomicInteger index = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("netty_io_1#" + index.getAndIncrement());
                        return thread;
                    }
                });
        endpoint.setRestlightIoExecutor(io);

        String[] metrics = registry.scrape().split("\\n");
        Map<String, String> metricsMap = new HashMap<>(16);
        getMetric(metricsMap, metrics);
        boolean isShutdown = !metricsMap.get("shutdown").equals("0.0");
        assertEquals(io.isShutdown(), isShutdown);
        boolean isterminated = !metricsMap.get("terminated").equals("0.0");
        assertEquals(io.isTerminated(), isterminated);
        assertEquals("2.0", metricsMap.get("thread.count"));
        assertEquals("0.0", metricsMap.get("pending.tasks"));
        assertEquals("2.147483647E9", metricsMap.get("max.pending.tasks"));
        assertEquals("5.0", metricsMap.get("thread.priority"));
        assertEquals("50.0", metricsMap.get("io.ratio"));
        assertEquals("0.0", metricsMap.get("task.queue.size"));

        io.shutdownGracefully().awaitUninterruptibly();
        String[] terminateMetrics = registry.scrape().split("\\n");
        Map<String, String> terminateMetricsMap = new HashMap<>(16);
        getMetric(terminateMetricsMap, terminateMetrics);
        boolean isShutdown1 = !terminateMetricsMap.get("shutdown").equals("0.0");
        assertEquals(io.isShutdown(), isShutdown1);
        boolean isterminated1 = !terminateMetricsMap.get("terminated").equals("0.0");
        assertEquals(io.isTerminated(), isterminated1);
    }

    private void getMetric(Map<String, String> map, String[] metrics) {
        Arrays.stream(metrics)
                .filter(metric -> metric.charAt(0) != '#')
                .forEach(metric -> {
                    String[] s = metric.split(" ");
                    map.put(s[0].substring(s[0].indexOf("id=\"") + 4, s[0].indexOf("\",threadName=")), s[1]);
                });
    }

}
