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

import esa.restlight.core.handler.impl.HandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.server.bootstrap.WebServerException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeExceptionMapperTest extends BaseExceptionMapperTest {

    @Test
    void testEmptyWrap() {
        assertThrows(IllegalArgumentException.class,
                () -> CompositeExceptionMapper.wrapIfNecessary(Collections.emptyList()));
    }

    @Test
    void testWrapIfNecessary() {
        final ExceptionMapper mapper = new DefaultExceptionMapper(Collections.singletonMap(Exception.class,
                (request, response, throwable) -> null));
        assertSame(mapper, CompositeExceptionMapper.wrapIfNecessary(Collections.singletonList(mapper)));
    }

    @Test
    void testIsApplicable() throws NoSuchMethodException {
        // mapper which can apply to all handlers.
        final CompositeExceptionMapper mapper = build();
        assertTrue(mapper.isApplicable(new HandlerImpl(HandlerMethod.of(Subject.class
                        .getDeclaredMethod("method0"),
                new Subject()))));

        assertTrue(mapper.isApplicable(new HandlerImpl(HandlerMethod.of(Subject2.class
                        .getDeclaredMethod("method0"),
                new Subject2()))));
    }

    @Test
    void testMapTo() {
        final CompositeExceptionMapper mapper = build();

        assertEquals(WebServerExceptionResolver.class, mapper.mapTo(WebServerException.class).getClass());
        assertEquals(WebServerExceptionResolver.class, mapper.mapTo(SubWebServerException.class).getClass());
        assertEquals(IllegalArgumentExceptionResolver.class, mapper.mapTo(IllegalArgumentException.class).getClass());

        assertNull(mapper.mapTo(Exception.class));
        assertNull(mapper.mapTo(NullPointerException.class));
    }

    private CompositeExceptionMapper build() {
        final ExceptionMapper mapper0 =
                new HandlerOnlyExceptionMapper(Collections.singletonMap(WebServerException.class,
                new WebServerExceptionResolver()), Subject.class);

        final ExceptionMapper mapper1 = new DefaultExceptionMapper(Collections.singletonMap(
                IllegalArgumentException.class,
                new IllegalArgumentExceptionResolver()));

        List<ExceptionMapper> mappers = new ArrayList<>(2);
        mappers.add(mapper0);
        mappers.add(mapper1);
        return new CompositeExceptionMapper(mappers);
    }

    private static class Subject {
        private void method0() {
        }
    }

    private static class Subject2 {
        private void method0() {
        }
    }
}
