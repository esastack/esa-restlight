/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.restlight.starter.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AutoRestlightServerOptionsTest {

    @Test
    void testSetterAndGetter() {
        final AutoRestlightServerOptions options = new AutoRestlightServerOptions();
        options.setHost("127.0.1.1");
        assertEquals("127.0.1.1", options.getHost());

        options.setPort(9999);
        assertEquals(9999, options.getPort());

        options.setUnixDomainSocketFile("/abc");
        assertEquals("/abc", options.getUnixDomainSocketFile());

        options.setPrintBanner(false);
        assertFalse(options.isPrintBanner());

        final WarmUpOptions warmUp = new WarmUpOptions();
        warmUp.setEnable(true);
        warmUp.setDelay(99L);
        options.setWarmUp(warmUp);
        assertEquals(warmUp, options.getWarmUp());
    }

}

