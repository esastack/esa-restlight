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

import esa.restlight.starter.actuator.meter.RestlightBizThreadPoolBinder;
import esa.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static esa.restlight.starter.actuator.endpoint.RestlightBizThreadPoolEndpoint.ThreadPoolMetric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RestlightBizThreadPoolEndpointTest {

    @Test
    void testMetrics() throws InterruptedException {
        final RestlightBizThreadPoolEndpoint endpoint = new RestlightBizThreadPoolEndpoint();
        final AutoRestlightServerOptions ops = new AutoRestlightServerOptions();
        endpoint.config = ops;
        endpoint.setRestlightBizExecutor(command -> {
        });
        assertNull(endpoint.threadPoolMetric());

        final ThreadPoolExecutor executor =
                new ThreadPoolExecutor(1,
                        2,
                        60L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(ops.getBlockingQueueLength()));
        endpoint.setRestlightBizExecutor(executor);
        final CountDownLatch firstTaskLatch = new CountDownLatch(1);
        executor.execute(firstTaskLatch::countDown);
        firstTaskLatch.await();

        final CountDownLatch exeLatch = new CountDownLatch(1);
        final CountDownLatch awaitLatch = new CountDownLatch(1);
        executor.execute(() -> {
            try {
                exeLatch.countDown();
                awaitLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        exeLatch.await();
        final ThreadPoolMetric metric = endpoint.threadPoolMetric();
        assertEquals(executor.getCorePoolSize(), metric.getCorePoolSize());
        assertEquals(executor.getMaximumPoolSize(), metric.getMaxPoolSize());
        assertEquals(ops.getBlockingQueueLength(), metric.getQueueLength());
        assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS), metric.getKeepAliveTimeSeconds());
        assertEquals(1, metric.getActiveCount());
        assertEquals(executor.getLargestPoolSize(), metric.getLargestPoolSize());
        assertEquals(executor.getQueue().size(), metric.getQueueCount());
        assertEquals(1, metric.getCompletedTaskCount());
        endpoint.update(2, 4);
        assertEquals(executor.getCorePoolSize(), 2);
        assertEquals(executor.getMaximumPoolSize(), 4);

        awaitLatch.countDown();
        executor.shutdown();
    }

    @Test
    void testMetrics4Prometheus() {
        final AutoRestlightServerOptions ops = new AutoRestlightServerOptions();
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        final RestlightBizThreadPoolBinder endpoint = new RestlightBizThreadPoolBinder(registry, ops);
        endpoint.setRestlightBizExecutor(command -> {
        });

        assertEquals("", registry.scrape());

        final ThreadPoolExecutor executor =
                new ThreadPoolExecutor(1,
                        2,
                        60L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(ops.getBlockingQueueLength()));
        endpoint.setRestlightBizExecutor(executor);

        String[] metrics = registry.scrape().split("\\n");
        Map<String, String> metricsMap = new HashMap<>(16);
        getMetric(metricsMap, metrics);
        assertEquals(executor.getCorePoolSize(), (int) Double.parseDouble(metricsMap.get("core.pool.size")));
        assertEquals(executor.getMaximumPoolSize(), (int) Double.parseDouble(metricsMap.get("max.pool.size")));
        assertEquals(ops.getBlockingQueueLength(), (int) Double.parseDouble(metricsMap.get("queue.length")));
        assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS),
                (long) Double.parseDouble(metricsMap.get("keep.alive.time.seconds")));
        assertEquals(executor.getActiveCount(), (int) Double.parseDouble(metricsMap.get("active.count")));
        assertEquals(executor.getLargestPoolSize(), (int) Double.parseDouble(metricsMap.get("largest.pool.size")));
        assertEquals(executor.getQueue().size(), (int) Double.parseDouble(metricsMap.get("queue.count")));
        assertEquals(executor.getCompletedTaskCount(),
                (long) Double.parseDouble(metricsMap.get("completed.task.count")));
    }

    private void getMetric(Map<String, String> map, String[] metrics) {
        Arrays.stream(metrics)
                .filter(metric -> metric.charAt(0) != '#')
                .forEach(metric -> {
                    String[] s = metric.split(" ");
                    map.put(s[0].substring(s[0].indexOf("id=\"") + 4, s[0].lastIndexOf('\"')), s[1]);
                });
    }
}
