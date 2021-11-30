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
package io.esastack.restlight.server.bootstrap;

import esa.commons.Checks;
import io.esastack.restlight.server.util.LoggerUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RestlightThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_ID = new AtomicInteger();

    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;
    private final boolean daemon;

    public RestlightThreadFactory(String prefix) {
        this(prefix, true);
    }

    public RestlightThreadFactory(String prefix,
                                  boolean daemon) {
        Checks.checkNotNull(prefix, "prefix");
        this.prefix = prefix + "-" + POOL_ID.getAndIncrement() + "#";
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + nextId.getAndIncrement();
        Thread t = new RestlightThread(r, name);
        try {
            if (t.isDaemon() != daemon) {
                t.setDaemon(daemon);
            }
        } catch (Exception ignored) {
            // Doesn't matter even if failed to set.
        }
        t.setUncaughtExceptionHandler((thread, error) ->
                LoggerUtils.logger().error("Got uncaught exception from Restlight thread '"
                        + thread.getName() + "'", error)
        );
        return t;
    }
}
