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
package esa.restlight.server.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulingOptionsTest {

    @Test
    void testConfigure() {
        final Map<String, FailFastOptions> failFastOptions = new HashMap<>(1);
        failFastOptions.put("A", FailFastOptionsConfigure.defaultOpts());

        final SchedulingOptions options = SchedulingOptionsConfigure.newOpts()
                .defaultScheduler("foo")
                .failFastOptions(failFastOptions)
                .configured();

        assertEquals("foo", options.getDefaultScheduler());
        assertEquals(1, options.getFailFastOptions().size());
        assertThrows(UnsupportedOperationException.class,
                () -> options.getFailFastOptions().put("A", FailFastOptionsConfigure.defaultOpts()));
    }

    @Test
    void testDefaultOpts() {
        assertEquals(new SchedulingOptions().getDefaultScheduler(),
                SchedulingOptionsConfigure.defaultOpts().getDefaultScheduler());
        assertTrue(new SchedulingOptions().getFailFastOptions().isEmpty());
    }

}
