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
package io.esastack.restlight.jaxrs.impl.container;

import jakarta.ws.rs.container.ResourceInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceInfoImplTest {

    @Test
    void testBasic() throws Throwable {
        final Class<?> userType = Object.class;
        final Method method = userType.getDeclaredMethod("toString");
        assertThrows(NullPointerException.class, () -> new ResourceInfoImpl(null, method));
        assertThrows(NullPointerException.class, () -> new ResourceInfoImpl(userType, null));
        final ResourceInfo resourceInfo = new ResourceInfoImpl(userType, method);
        assertSame(userType, resourceInfo.getResourceClass());
        assertSame(method, resourceInfo.getResourceMethod());
        assertEquals("ResourceInfoImpl{userType=class java.lang.Object," +
                " method=public java.lang.String java.lang.Object.toString()}", resourceInfo.toString());
    }

}

