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
package io.esastack.restlight.jaxrs.impl.core;

import io.esastack.restlight.core.handler.method.RouteMethodInfo;
import io.esastack.restlight.core.handler.method.RouteMethodInfoImpl;
import io.esastack.restlight.core.handler.method.RouteHandlerMethodImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MatchedResourceTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new MatchedResource(null, null));
        assertDoesNotThrow(() -> new MatchedResource(mock(RouteMethodInfo.class), null));
    }

    @Test
    void testBasic() throws Throwable {
        final RouteMethodInfo methodInfo = new RouteMethodInfoImpl(RouteHandlerMethodImpl.of(Object.class,
                Object.class.getDeclaredMethod("toString"), false, null),
                false, null);
        final MatchedResource resource = new MatchedResource(methodInfo, null);
        assertSame(methodInfo, resource.method());
        assertFalse(resource.bean().isPresent());
        assertEquals("MatchedResource{method=HandlerMethodInfoImpl{locator=false," +
                " handlerMethod=RouteHandlerMethod: {java.lang.Object => toString," +
                " intercepted: false, scheduler: BIZ}, customStatus=null}}",
                resource.toString());
    }

}

