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
package io.esastack.restlight.core.handler;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.handler.impl.HandlerMethodInfoImpl;
import io.esastack.restlight.core.handler.impl.RouteMethodInfoImpl;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.RouteHandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.RouteHandlerMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class HandlerMethodInfoTest {

    @Test
    void testConstruct() {
        final boolean locator = true;
        final HandlerMethod method = mock(HandlerMethod.class);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final HandlerMethodInfo methodInfo = new HandlerMethodInfoImpl(method, locator, status);
        assertEquals(method, methodInfo.handlerMethod());
        assertEquals(status, methodInfo.customStatus());
        assertTrue(methodInfo.isLocator());
    }

    @Test
    void testEquals() {
        final boolean locator = true;
        final HandlerMethod method = mock(HandlerMethod.class);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final HandlerMethodInfo methodInfo = new HandlerMethodInfoImpl(method, locator, status);
        assertFalse(methodInfo.equals(null));
        assertNotEquals(methodInfo, new RouteMethodInfoImpl(mock(RouteHandlerMethod.class), locator, status));
        assertEquals(methodInfo, new HandlerMethodInfoImpl(method, locator, status));
    }

    @Test
    void testToString() {
        final boolean locator = true;
        final HandlerMethod method = mock(HandlerMethod.class);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final HandlerMethodInfo methodInfo = new HandlerMethodInfoImpl(method, locator, status);
        String sb = "HandlerMethodInfoImpl{" + "locator=" + locator +
                ", handlerMethod=" + method +
                ", customStatus=" + status +
                '}';
        assertEquals(methodInfo.toString(), sb);
    }

}
