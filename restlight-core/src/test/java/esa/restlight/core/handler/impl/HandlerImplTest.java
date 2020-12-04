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

import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.core.handler.LinkedHandlerInvoker;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HandlerImplTest {

    private static final Subject SUBJECT = new Subject();
    private static final HandlerAdvice[] handlerAdvices =
            {(request, response, args, invoker) -> invoker.invoke(request, response, args)};

    @Test
    void testProps() throws NoSuchMethodException {
        HandlerInvoker invoker = getInvoker("method");
        final HandlerMethod handlerMethod =
                HandlerMethod.of(Subject.class.getDeclaredMethod("method"), SUBJECT);
        final HttpResponseStatus customResponse = HttpResponseStatus.ACCEPTED;
        final HandlerImpl handler =
                new HandlerImpl(handlerMethod, customResponse, invoker);
        assertEquals(handlerMethod, handler.handler());
        assertEquals(customResponse, handler.customResponse());

    }

    @Test
    void testInvoke() throws Throwable {
        HandlerInvoker invoker = getInvoker("method");
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method"), SUBJECT),
                        HttpResponseStatus.ACCEPTED,
                        invoker);
        final Object ret = handler.invoke(MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build(), null);
        assertEquals("foo", ret);
    }

    @Test
    void testHandlerAdvice() throws Throwable {
        HandlerInvoker invoker = getInvoker("method");
        final HandlerAdvice advice = (request, response, args, invoker1) -> {
            throw new WebServerException("");
        };
        invoker = LinkedHandlerInvoker.immutable(Collections.singleton(advice).toArray(new HandlerAdvice[0]), invoker);
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method"), SUBJECT),
                        null,
                        invoker);

        assertThrows(WebServerException.class, () -> handler.invoke(MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build(), null));
    }

    @Test
    void testInvokeError() throws Throwable {
        HandlerInvoker invoker = getInvoker("err");
        final HandlerImpl handler =
                new HandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("err"), SUBJECT),
                        HttpResponseStatus.ACCEPTED,
                        invoker);
        assertThrows(UnsupportedOperationException.class, () -> handler.invoke(MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build(), null));
    }

    private static class Subject {

        public String method() {
            return "foo";
        }

        void err() {
            throw new UnsupportedOperationException();
        }
    }

    private HandlerInvoker getInvoker(String methodName) throws NoSuchMethodException {
        final InvocableMethod handlerMethod = HandlerMethod.of(Subject.class,
                Subject.class.getDeclaredMethod(methodName),
                SUBJECT);
        HandlerInvoker invoker = new HandlerInvokerImpl(handlerMethod);
        return LinkedHandlerInvoker.immutable(handlerAdvices, invoker);
    }

}
