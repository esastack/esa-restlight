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

import io.esastack.restlight.server.util.PathMatcher;
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

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Threads(1)
@Fork(1)
@State(Scope.Benchmark)
public class PathMatcherSingleThreadBenchmark {

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
        this.restlightMatcher = new PathMatcher(pattern);
        this.springMatcher = new org.springframework.util.AntPathMatcher();
    }

    @Benchmark
    public boolean match() {
        return restlightMatcher.match(path);
    }

    @Benchmark
    public boolean matchSpring() {
        return springMatcher.match(pattern, path);
    }
}
