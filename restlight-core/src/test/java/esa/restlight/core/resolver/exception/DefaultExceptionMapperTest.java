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
package esa.restlight.core.resolver.exception;

import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.server.bootstrap.WebServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultExceptionMapperTest extends BaseExceptionMapperTest {

    private final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings = new HashMap<>();

    @BeforeEach
    void setUp() {
        mappings.putIfAbsent(WebServerException.class, new WebServerExceptionResolver());
        mappings.putIfAbsent(IllegalArgumentException.class, new IllegalArgumentExceptionResolver());
        mappings.putIfAbsent(SubWebServerException.class, new SubWebServerExceptionResolver());
        mappings.putIfAbsent(RuntimeException.class, new RuntimeExceptionResolver());
    }

    @Test
    void testMapTo() {
        final ExceptionMapper mapper = new DefaultExceptionMapper(mappings);
        assertNull(mapper.mapTo(null));

        assertEquals(WebServerExceptionResolver.class, mapper.mapTo(WebServerException.class).getClass());
        assertEquals(IllegalArgumentExceptionResolver.class, mapper.mapTo(IllegalArgumentException.class).getClass());
        assertEquals(SubWebServerExceptionResolver.class, mapper.mapTo(SubWebServerException.class).getClass());
        assertEquals(RuntimeExceptionResolver.class, mapper.mapTo(RuntimeException.class).getClass());

        assertEquals(RuntimeExceptionResolver.class, mapper.mapTo(IllegalStateException.class).getClass());
    }

    @Test
    void testEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultExceptionMapper(Collections.emptyMap()));
    }
}
