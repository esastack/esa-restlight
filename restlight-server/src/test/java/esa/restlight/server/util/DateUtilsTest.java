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

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DateUtilsTest {

    @Test
    void testFormat() {
        final String dateStr = "2020-05-10 12:00:00";
        final Date date = esa.commons.DateUtils.toDate(dateStr, esa.commons.DateUtils.yyyyMMddHHmmss);
        assertEquals(dateStr, DateUtils.format(date));

        final LocalDateTime time = LocalDateTime.parse(dateStr,
                DateTimeFormatter.ofPattern(esa.commons.DateUtils.yyyyMMddHHmmss));
        assertEquals(dateStr, DateUtils.format(time));
        final Date parsed = esa.commons.DateUtils.toDate(DateUtils.now(),
                esa.commons.DateUtils.yyyyMMddHHmmss);
        assertNotNull(parsed);

        assertEquals(dateStr, DateUtils.formatByCache(date.getTime()));
        for (int i = 1; i < 1000; i++) {
            assertEquals(dateStr, DateUtils.formatByCache(date.getTime() + i));
        }
        assertEquals("2020-05-10 12:00:01", DateUtils.formatByCache(date.getTime() + 1000L));
    }

}
