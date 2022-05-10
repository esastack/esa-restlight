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
package io.esastack.restlight.core.handler.method;

import io.esastack.restlight.core.annotation.Intercepted;
import io.esastack.restlight.core.annotation.QueryBean;
import io.esastack.restlight.core.annotation.Scheduled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandlerMethodImplTest {

    protected HandlerMethod buildHandlerMethod(Class<?> beanType, Method method) {
        return new HandlerMethodImpl(beanType, method);
    }

    @Test
    void testTypes() throws NoSuchMethodException {
        final HandlerMethod handlerMethod
                = buildHandlerMethod(Subject.class, Subject.class.getDeclaredMethod("method"));
        assertEquals(Subject.class, handlerMethod.beanType());
        assertEquals(Subject.class.getDeclaredMethod("method"), handlerMethod.method());
        assertEquals(0, handlerMethod.parameters().length);
    }

    @Test
    void testParameters() throws NoSuchMethodException {
        final HandlerMethod handlerMethod
                = buildHandlerMethod(Subject.class,
                Subject.class.getDeclaredMethod("params", String.class, int.class, List.class));
        assertNotNull(handlerMethod.parameters());
        assertEquals(3, handlerMethod.parameters().length);

        // test parameter type
        assertEquals(String.class, handlerMethod.parameters()[0].type());
        assertEquals(int.class, handlerMethod.parameters()[1].type());
        assertEquals(List.class, handlerMethod.parameters()[2].type());

        // test parameter name
        assertEquals("p0", handlerMethod.parameters()[0].name());
        assertEquals("p1", handlerMethod.parameters()[1].name());
        assertEquals("p2", handlerMethod.parameters()[2].name());

        // test generic type
        final Type p2Type = handlerMethod.parameters()[2].genericType();
        assertTrue(p2Type instanceof ParameterizedType);
        assertEquals(String.class, ((ParameterizedType) p2Type).getActualTypeArguments()[0]);
    }

    @Test
    void testAnnotations() throws NoSuchMethodException {
        final HandlerMethod handlerMethod
                = buildHandlerMethod(Subject.class, Subject.class.getDeclaredMethod("ann", String.class));
        assertNotNull(handlerMethod.parameters());
        assertEquals(1, handlerMethod.parameters().length);

        assertTrue(handlerMethod.hasMethodAnnotation(Intercepted.class, true));
        assertFalse(handlerMethod.getMethodAnnotation(Intercepted.class, true).value());
        assertFalse(handlerMethod.hasMethodAnnotation(Scheduled.class, true));
        assertNotNull(handlerMethod.getClassAnnotation(Intercepted.class, true));
        assertFalse(handlerMethod.getClassAnnotation(Intercepted.class, true).value());
        assertNull(handlerMethod.getClassAnnotation(Scheduled.class, true));

        assertTrue(handlerMethod.parameters()[0].hasAnnotation(QueryBean.class));
        assertFalse(handlerMethod.parameters()[0].hasAnnotation(Intercepted.class));
    }

    @Intercepted(false)
    private static class Subject {

        void method() {

        }

        void params(String p0, int p1, List<String> p2) {
        }

        @Intercepted(false)
        void ann(@QueryBean String p0) {
        }

    }

}
