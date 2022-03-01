/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.util;

import esa.commons.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UriUtilsTest {

    @Test
    void testEncode0() {
        assertEquals(StringUtils.empty(), UriUtils.encode(StringUtils.empty()));
        assertEquals("%2Fabc%252Aa%25mm", UriUtils.encode("/abc%2Aa%mm"));
        assertEquals("%2Fabc%252Aa%25m", UriUtils.encode("/abc%2Aa%m"));
        assertEquals("%2Fabc%252Aa%25", UriUtils.encode("/abc%2Aa%"));
    }

    @Test
    void testEncode1() {
        final boolean flag = ThreadLocalRandom.current().nextBoolean();
        assertEquals(StringUtils.empty(), UriUtils.encode(StringUtils.empty(), flag, flag, flag));

        assertEquals("%2Fabc%252Aa%25mm", UriUtils.encode("/abc%2Aa%mm",
                true, true, false));
        assertEquals("%2Fabc%252Aa%25m", UriUtils.encode("/abc%2Aa%m",
                true, true, false));
        assertEquals("%2Fabc%252Aa%25", UriUtils.encode("/abc%2Aa%",
                true, true, false));
        assertEquals("%2Fabc%252Aa%25mm", UriUtils.encode("/abc%2Aa%mm",
                true, true, true));
        assertEquals("%2Fabc%252Aa%25m", UriUtils.encode("/abc%2Aa%m",
                true, true, true));
        assertEquals("%2Fabc%252Aa%25", UriUtils.encode("/abc%2Aa%",
                true, true, true));

        assertEquals("/abc%252Aa%25mm", UriUtils.encode("/abc%2Aa%mm",
                false, true, false));
        assertEquals("/abc%252Aa%25mm/", UriUtils.encode("/abc%2Aa%mm/",
                false, true, false));

        assertEquals("/abc%2Aa%mm", UriUtils.encode("/abc%2Aa%mm",
                false, false, false));
        assertEquals("/abc%2Aa%m", UriUtils.encode("/abc%2Aa%m",
                false, false, false));
        assertEquals("/abc%2Aa%", UriUtils.encode("/abc%2Aa%",
                false, false, false));

        assertEquals("/abc%2Aa%25mm", UriUtils.encode("/abc%2Aa%mm",
                false, false, true));
        assertEquals("/abc%2Aa%25m", UriUtils.encode("/abc%2Aa%m",
                false, false, true));
        assertEquals("/abc%2Aa%25", UriUtils.encode("/abc%2Aa%",
                false, false, true));
    }

}

