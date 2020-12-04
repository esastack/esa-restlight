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
package esa.restlight.core.handler.impl;

import esa.restlight.core.method.HandlerMethod;
import esa.restlight.server.schedule.Schedulers;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteHandlerImplTest {

    private static final Subject SUBJECT = new Subject();

    @Test
    void testProps() throws NoSuchMethodException {
        final HandlerMethod handlerMethod =
                HandlerMethod.of(Subject.class.getDeclaredMethod("method"), SUBJECT);
        final HttpResponseStatus customResponse = HttpResponseStatus.ACCEPTED;
        final String strategy = Schedulers.IO;
        final RouteHandlerImpl handler =
                new RouteHandlerImpl(handlerMethod, customResponse,
                        null, true, strategy);
        assertEquals(handlerMethod, handler.handler());
        assertEquals(customResponse, handler.customResponse());
        assertTrue(handler.intercepted());
        assertEquals(strategy, handler.scheduler());
    }

    private static class Subject {

        public String method() {
            return "foo";
        }
    }

}
