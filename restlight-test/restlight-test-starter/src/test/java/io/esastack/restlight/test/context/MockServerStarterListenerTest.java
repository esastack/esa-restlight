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
package io.esastack.restlight.test.context;

import io.esastack.restlight.starter.condition.ServerPortType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class MockServerStarterListenerTest {

    @Test
    void testServerPortWhenMissingWebEnv() {
        ServerPortType.reset();
        final MockServerStarterListener listener = new MockServerStarterListener();
        final TestContext context = Mockito.mock(TestContext.class);

        when(context.getTestClass()).thenReturn((Class) MockServerStarterListenerTest.class);
        listener.beforeTestClass(context);
        assertEquals(ServerPortType.MOCK, ServerPortType.get());
        listener.afterTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());
    }

    @Test
    void testServerPortWithWebEnv() {
        final MockServerStarterListener listener = new MockServerStarterListener();
        final TestContext context = Mockito.mock(TestContext.class);

        when(context.getTestClass()).thenReturn((Class) A.class);
        listener.beforeTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());
        listener.afterTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());

        reset(context);
        when(context.getTestClass()).thenReturn((Class) B.class);

        listener.beforeTestClass(context);
        assertEquals(ServerPortType.RANDOM, ServerPortType.get());
        listener.afterTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());

        reset(context);
        when(context.getTestClass()).thenReturn((Class) C.class);

        listener.beforeTestClass(context);
        assertEquals(ServerPortType.MOCK, ServerPortType.get());
        listener.afterTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());

        reset(context);
        when(context.getTestClass()).thenReturn((Class) D.class);

        listener.beforeTestClass(context);
        assertEquals(ServerPortType.MOCK, ServerPortType.get());
        listener.afterTestClass(context);
        assertEquals(ServerPortType.DEFINED, ServerPortType.get());
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
    private static class A {

    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    private static class B {

    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
    private static class C {

    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    private static class D {

    }

}
