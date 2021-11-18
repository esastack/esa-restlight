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
package io.esastack.restlight.ext.filter.cpuload;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpuLoadProtectionOptionsTest {

    @Test
    void testConfigure() {
        final CpuLoadProtectionOptions options = CpuLoadProtectionOptionsConfigure.newOpts()
                .threshold(1.0D)
                .initialDiscardRate(2.0D)
                .maxDiscardRate(3.0D)
                .configured();
        assertEquals(1.0D, options.getThreshold());
        assertEquals(2.0D, options.getInitialDiscardRate());
        assertEquals(3.0D, options.getMaxDiscardRate());
    }

    @Test
    void testDefaultOpts() {
        final CpuLoadProtectionOptions options = CpuLoadProtectionOptionsConfigure.defaultOpts();
        final CpuLoadProtectionOptions def = new CpuLoadProtectionOptions();

        assertEquals(def.getThreshold(), options.getThreshold());
        assertEquals(def.getInitialDiscardRate(), options.getInitialDiscardRate());
        assertEquals(def.getMaxDiscardRate(), options.getMaxDiscardRate());
    }

}
