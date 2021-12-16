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
package io.esastack.restlight.jmh.server;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.impl.RequestContextImpl;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.route.impl.AbstractRouteRegistry;
import io.esastack.restlight.server.route.impl.CachedRouteRegistry;
import io.esastack.restlight.server.route.impl.MappingImpl;
import io.esastack.restlight.server.route.impl.SimpleRouteRegistry;
import io.esastack.restlight.server.schedule.Schedulers;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

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

    private AbstractRouteRegistry cache;
    private AbstractRouteRegistry noCache;

    @Param({"10", "20", "50", "100"})
    private int routes = 100;

    private RequestContext[] contexts;
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
            Route route = Route.route(Schedulers.biz()).mapping(m);
            cache.register(route);
            noCache.register(route);
        }

        contexts = new RequestContext[routes];
        for (int i = 0; i < contexts.length; i++) {
            contexts[i] = new RequestContextImpl(
                    MockHttpRequest.aMockRequest()
                            .withMethod(mappings[i].method()[0].name())
                            .withUri("/foo/bar/baz/qux" + i)
                            .withParameter("a" + i, "a")
                            .withParameter("b" + i, "1")
                            .withHeader("c" + i, "c")
                            .withHeader("d" + i, "1")
                            .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON.value())
                            .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.TEXT_PLAIN.value())
                            .build(), MockHttpResponse.aMockResponse().build());
        }
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

    private RequestContext getRequest() {
        return contexts[getPossionVariable(lambda, routes - 1)];
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
