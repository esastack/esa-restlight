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

import esa.restlight.server.util.PathMatcher;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Threads(Threads.MAX)
@Fork(1)
@State(Scope.Benchmark)
public class PathMatcherMultiThreadBenchmark {

    private PathMatcher restlightMatcher;
    private org.springframework.util.PathMatcher springMatcher;

    @Param({"/abcd/efgh/ijkl",
            "/ab??/??gh/i?jkl",
            "/abc*/e*h/*jkl",
            "/ab?d/e?g*/*k*",
            "/**/ijkl",
            "/*b?d/?fgh/**"})
    private String pattern;
    @Param({"/abcd/efgh/ijkl"})
    private String path;

    @Setup
    public void setUp() {
        this.restlightMatcher = new PathMatcher("pattern");
        this.springMatcher = new org.springframework.util.AntPathMatcher();
    }

    @Benchmark
    public boolean matchByRestlight() {
        return restlightMatcher.match(path);
    }

    @Benchmark
    public boolean matchBySpring() {
        return springMatcher.match(pattern, path);
    }


}
