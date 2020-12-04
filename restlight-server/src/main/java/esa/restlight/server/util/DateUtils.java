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
package esa.restlight.server.util;

import io.netty.util.concurrent.FastThreadLocal;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final FastThreadLocal<CachedFormatter> LOCAL_FORMAT =
            new FastThreadLocal<CachedFormatter>() {
                @Override
                protected CachedFormatter initialValue() {
                    return new CachedFormatter();
                }
            };

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(esa.commons.DateUtils.yyyyMMddHHmmss)
                    .withZone(ZoneId.systemDefault());

    @Deprecated
    public static String format(Date date) {
        return formatByCache(date.getTime());
    }

    public static String format(LocalDateTime time) {
        return FORMATTER.format(time);
    }

    public static String format(long time) {
        return FORMATTER.format(Instant.ofEpochMilli(time));
    }

    public static String formatByCache(long time) {
        return LOCAL_FORMAT.get().format(time);
    }

    public static String now() {
        return formatByCache(System.currentTimeMillis());
    }

    private DateUtils() {
    }

    private static class CachedFormatter {
        private final SimpleDateFormat sdf =
                new SimpleDateFormat(esa.commons.DateUtils.yyyyMMddHHmmss);
        private long lastTimestamp = -1L;
        private String cacheTime;

        private String format(long time) {
            long t = time / 1000L * 1000L;
            if (lastTimestamp == t) {
                return cacheTime;
            }
            lastTimestamp = t;
            return cacheTime = sdf.format(new Date(time));
        }
    }
}
