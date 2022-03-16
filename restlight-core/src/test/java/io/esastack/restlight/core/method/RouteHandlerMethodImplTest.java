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
package io.esastack.restlight.core.method;

import io.esastack.restlight.server.schedule.Schedulers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RouteHandlerMethodImplTest extends HandlerMethodImplTest {

    @Override
    protected HandlerMethod buildHandlerMethod(Class<?> beanType, Method method) {
        return RouteHandlerMethodImpl.of(beanType, method);
    }

    @Test
    void testInterceptedAndScheduler() throws NoSuchMethodException {
        RouteHandlerMethod handlerMethod = RouteHandlerMethodImpl.of(Subject.class,
                Subject.class.getDeclaredMethod("method"), false, "");

        assertFalse(handlerMethod.intercepted());
        assertEquals(Schedulers.BIZ, handlerMethod.scheduler());

        handlerMethod = RouteHandlerMethodImpl.of(Subject.class,
                Subject.class.getDeclaredMethod("method"), true, "aaa");

        assertTrue(handlerMethod.intercepted());
        assertEquals("aaa", handlerMethod.scheduler());
    }

    private static class Subject {

        void method() {
        }

    }
}
