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
import io.esastack.restlight.core.method.HandlerMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class HandlerMethodInfoTest {

    @Test
    void testAll() {
        final HandlerMethod method = mock(HandlerMethod.class);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final HandlerMethodInfo methodInfo = new HandlerMethodInfo(method, true, status);
        assertEquals(method, methodInfo.handlerMethod());
        assertEquals(status, methodInfo.customStatus());
        assertTrue(methodInfo.isLocator());
    }

}
