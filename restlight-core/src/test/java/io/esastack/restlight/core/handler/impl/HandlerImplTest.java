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
package io.esastack.restlight.core.handler.impl;

import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.LinkedHandlerInvoker;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class HandlerImplTest {

    private static final Subject SUBJECT = new Subject();

    @Test
    void testConstruct() throws NoSuchMethodException {
        final HandlerMethod handlerMethod =
                HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method"));
        final HandlerImpl handler =
                new HandlerImpl(handlerMethod, SUBJECT);
        assertEquals(handlerMethod, handler.handlerMethod());
        assertEquals(SUBJECT, handler.bean());
    }

    @Test
    void testInvoke() throws Throwable {
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethodImpl.of(Subject.class,
                        Subject.class.getDeclaredMethod("method")),
                        SUBJECT);
        final Object ret = handler.invoke(mock(RequestContext.class), null);
        assertEquals("foo", ret);
    }

    @Test
    void testHandlerAdvice() throws Throwable {
        final HandlerMethod handlerMethod = HandlerMethodImpl.of(Subject.class,
                Subject.class.getDeclaredMethod("method"));
        HandlerInvoker invoker = new HandlerInvokerImpl(handlerMethod, SUBJECT);
        final HandlerAdvice advice = (ctx, args, invoker1) -> {
            throw new WebServerException("");
        };
        invoker = LinkedHandlerInvoker.immutable(Collections.singleton(advice).toArray(new HandlerAdvice[0]), invoker);
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethodImpl.of(Subject.class,
                        Subject.class.getDeclaredMethod("method")),
                        SUBJECT,
                        invoker);

        assertThrows(WebServerException.class, () -> handler.invoke(mock(RequestContext.class), null));
    }

    @Test
    void testInvokeError() throws Throwable {
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethodImpl.of(Subject.class,
                        Subject.class.getDeclaredMethod("err")),
                        SUBJECT);
        assertThrows(UnsupportedOperationException.class, () -> handler.invoke(mock(RequestContext.class), null));
    }

    private static class Subject {

        public String method() {
            return "foo";
        }

        void err() {
            throw new UnsupportedOperationException();
        }
    }

}
