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
package esa.restlight.jmh.common;

import esa.restlight.core.util.MediaType;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Threads(Threads.MAX)
@Fork(1)
@State(Scope.Benchmark)
public class MediaTypeBenchmark {

    @Benchmark
    public MediaType parseDirectly() {
        return MediaType.parseMediaType("application/json;charset=utf-8");
    }

    @Benchmark
    public MediaType parseWithCache() {
        return MediaType.valueOf("application/json;charset=utf-8");
    }

}
