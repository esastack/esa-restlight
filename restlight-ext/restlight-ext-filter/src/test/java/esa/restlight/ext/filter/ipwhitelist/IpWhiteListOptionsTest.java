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
package esa.restlight.ext.filter.ipwhitelist;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IpWhiteListOptionsTest {

    @Test
    void testConfigure() {
        final IpWhiteListOptions options = IpWhiteListOptionsConfigure.newOpts()
                .ips(Arrays.asList("a", "b"))
                .cacheSize(1)
                .expire(2)
                .configured();
        assertArrayEquals(new String[]{"a", "b"}, options.getIps().toArray());
        assertEquals(1, options.getCacheSize());
        assertEquals(2, options.getExpire());
    }

    @Test
    void testDefaultOpts() {
        final IpWhiteListOptions options = IpWhiteListOptionsConfigure.defaultOpts();
        final IpWhiteListOptions def = new IpWhiteListOptions();

        assertEquals(def.getIps(), options.getIps());
        assertEquals(def.getCacheSize(), options.getCacheSize());
        assertEquals(def.getExpire(), options.getExpire());
    }

}
