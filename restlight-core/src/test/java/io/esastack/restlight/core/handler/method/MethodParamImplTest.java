/*
 * Copyright 2022 OPPO ESA Stack Project
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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MethodParamImplTest {

    @Test
    void testAll() throws NoSuchMethodException {
        Method method = Bean.class.getMethod("test", String.class, String.class);
        MethodParam param = new MethodParamImpl(method, 1);
        assertEquals("bbb", param.name());
        assertEquals(method, param.method());
        assertTrue(param.hasMethodAnnotation(Intercepted.class));
        assertEquals(1, param.annotations().length);
        assertNotNull(param.getAnnotation(QueryBean.class));
        assertEquals(String.class, param.type());
        assertEquals(1, param.index());
        assertEquals(String.class, param.genericType());
        assertEquals(String.class, param.parameter().getParameterizedType());
    }


    private static final class Bean {

        @Intercepted
        public Integer test(String aaa, @QueryBean String bbb) {
            return 0;
        }
    }

}
