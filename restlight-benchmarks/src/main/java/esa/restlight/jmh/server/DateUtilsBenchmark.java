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

import esa.restlight.server.util.DateUtils;
import org.openjdk.jmh.annotations.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Threads(Threads.MAX)
@Fork(1)
@State(Scope.Benchmark)
public class DateUtilsBenchmark {

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat(esa.commons.DateUtils.yyyyMMddHHmmss);

    @Benchmark
    public String formatBySdf() {
        return SDF.format(new Date(System.currentTimeMillis()));
    }

    @Benchmark
    public String formatBySdfCache() {
        return DateUtils.formatByCache(System.currentTimeMillis());
    }

    @Benchmark
    public String formatByDateTime() {
        return DateUtils.format(System.currentTimeMillis());
    }
}
