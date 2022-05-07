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
package io.esastack.restlight.core.util;

import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class LoggerUtils {

    private static final Logger logger
            = LoggerFactory.getLogger("io.esastack.restlight");
    private static final long LOG_PERIOD = TimeUnit.MINUTES.toNanos(1L);
    private static final LoggerUtils INSTANCE = new LoggerUtils();

    private final AtomicLong lastLogTime = new AtomicLong(0L);

    public static Logger logger() {
        return logger;
    }

    public static void logErrorPeriodically(String message, Object... objects) {
        if (canLogNow()) {
            logger.error(message, objects);
        }
    }

    private static boolean canLogNow() {
        long timestamp = System.nanoTime();
        if (timestamp - INSTANCE.lastLogTime.get() > LOG_PERIOD) {
            INSTANCE.lastLogTime.lazySet(timestamp);
            return true;
        }
        return false;
    }

}
