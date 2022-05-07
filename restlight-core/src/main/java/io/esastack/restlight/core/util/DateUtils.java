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

import esa.commons.StringUtils;
import io.netty.util.concurrent.FastThreadLocal;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class DateUtils {

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    private static final FastThreadLocal<CachedFormatter> LOCAL_FORMAT =
            new FastThreadLocal<CachedFormatter>() {
                @Override
                protected CachedFormatter initialValue() {
                    return new CachedFormatter();
                }
            };

    private static final FastThreadLocal<CachedParser> LOCAL_PARSE = new FastThreadLocal<CachedParser>() {
        @Override
        protected CachedParser initialValue() {
            return new CachedParser();
        }
    };

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(esa.commons.DateUtils.yyyyMMddHHmmss)
                    .withZone(ZoneId.systemDefault());

    private static final Collection<SimpleDateFormat> DEFAULT_SDFS;

    static {
        DEFAULT_SDFS = new ArrayList<>(5);
        DEFAULT_SDFS.add(new SimpleDateFormat(esa.commons.DateUtils.yyyyMMddHHmmss));
        DEFAULT_SDFS.add(new SimpleDateFormat(esa.commons.DateUtils.yyyyMMdd));
        DEFAULT_SDFS.add(new SimpleDateFormat(PATTERN_ASCTIME));
        DEFAULT_SDFS.add(new SimpleDateFormat(PATTERN_RFC1036));
        DEFAULT_SDFS.add(new SimpleDateFormat(PATTERN_RFC1123));
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

    public static Date parseByCache(String date) {
        return LOCAL_PARSE.get().parse(date);
    }

    public static Date parse(String date) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        for (SimpleDateFormat format : DEFAULT_SDFS) {
            try {
                return format.parse(date);
            } catch (Throwable ignore) {

            }
        }
        return null;
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

    private static class CachedParser {

        private final ArrayList<SimpleDateFormat> sdfs = new ArrayList<>(5);

        private CachedParser() {
            sdfs.add(new SimpleDateFormat(esa.commons.DateUtils.yyyyMMddHHmmss));
            sdfs.add(new SimpleDateFormat(esa.commons.DateUtils.yyyyMMdd));
            sdfs.add(new SimpleDateFormat(PATTERN_ASCTIME));
            sdfs.add(new SimpleDateFormat(PATTERN_RFC1036));
            sdfs.add(new SimpleDateFormat(PATTERN_RFC1123));
        }

        private Date parse(String date) {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            for (SimpleDateFormat format : sdfs) {
                try {
                    return format.parse(date);
                } catch (Throwable ignore) {

                }
            }
            return null;
        }
    }
}
