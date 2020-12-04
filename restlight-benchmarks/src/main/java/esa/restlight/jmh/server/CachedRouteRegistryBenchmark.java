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
package esa.restlight.jmh.server;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.server.route.impl.CachedRouteRegistry;
import esa.restlight.server.route.impl.MappingImpl;
import esa.restlight.server.route.impl.SimpleRouteRegistry;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Threads(Threads.MAX)
@Fork(1)
@State(Scope.Benchmark)
public class CachedRouteRegistryBenchmark {

    private ReadOnlyRouteRegistry cache;
    private ReadOnlyRouteRegistry noCache;

    @Param({"10", "20", "50", "100"})
    private int routes = 100;

    private AsyncRequest[] requests;
    private double lambda;

    @Setup
    public void setUp() {
        RouteRegistry cache = new CachedRouteRegistry(1);
        RouteRegistry noCache = new SimpleRouteRegistry();
        Mapping[] mappings = new Mapping[routes];
        for (int i = 0; i < routes; i++) {
            HttpMethod method = HttpMethod.values()[ThreadLocalRandom.current().nextInt(HttpMethod.values().length)];
            final MappingImpl mapping = Mapping.mapping("/f?o/b*r/**/??x" + i)
                    .method(method)
                    .hasParam("a" + i)
                    .hasParam("b" + i, "1")
                    .hasHeader("c" + i)
                    .hasHeader("d" + i, "1")
                    .consumes(MediaType.APPLICATION_JSON)
                    .produces(MediaType.TEXT_PLAIN);
            mappings[i] = mapping;
        }

        for (Mapping m : mappings) {
            Route route = Route.route(m);
            cache.registerRoute(route);
            noCache.registerRoute(route);
        }

        requests = new AsyncRequest[routes];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = MockAsyncRequest.aMockRequest()
                    .withMethod(mappings[i].method()[0].name())
                    .withUri("/foo/bar/baz/qux" + i)
                    .withParameter("a" + i, "a")
                    .withParameter("b" + i, "1")
                    .withHeader("c" + i, "c")
                    .withHeader("d" + i, "1")
                    .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON.value())
                    .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.TEXT_PLAIN.value())
                    .build();
        }
        this.cache = cache.toReadOnly();
        this.noCache = noCache.toReadOnly();
        this.lambda = (double) routes / 2;
    }

    @Benchmark
    public Route matchByCachedRouteRegistry() {
        return cache.route(getRequest());
    }

    @Benchmark
    public Route matchByDefaultRouteRegistry() {
        return noCache.route(getRequest());
    }

    private AsyncRequest getRequest() {
        return requests[getPossionVariable(lambda, routes - 1)];
    }

    private static int getPossionVariable(double lambda, int max) {
        int x = 0;
        double y = Math.random(), cdf = getPossionProbability(x, lambda);
        while (cdf < y) {
            x++;
            cdf += getPossionProbability(x, lambda);
        }
        return Math.min(x, max);
    }

    private static double getPossionProbability(int k, double lamda) {
        double c = Math.exp(-lamda), sum = 1;
        for (int i = 1; i <= k; i++) {
            sum *= lamda / i;
        }
        return sum * c;
    }



}
