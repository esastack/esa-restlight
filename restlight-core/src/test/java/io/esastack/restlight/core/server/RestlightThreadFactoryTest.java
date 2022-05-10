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
package io.esastack.restlight.core.server;

import io.esastack.restlight.core.server.processor.schedule.RestlightThreadFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestlightThreadFactoryTest {

    @Test
    void testCreateThread() {
        final RestlightThreadFactory factory = new RestlightThreadFactory("foo", false);
        final Thread t = factory.newThread(() -> {
        });
        assertNotNull(t);
        assertTrue(t.getName().startsWith("foo"));
        assertFalse(t.isDaemon());
        assertNotNull(t.getUncaughtExceptionHandler());

        final RestlightThreadFactory factory1 = new RestlightThreadFactory("foo", true);
        final Thread t1 = factory1.newThread(() -> {
        });

        assertTrue(t1.isDaemon());
    }

}
