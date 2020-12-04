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
package esa.restlight.core.serialize;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpJsonBodySerializerAdapterTest {

    @Test
    void serializer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final JacksonSerializer serializer = new JacksonSerializer();
        final HttpJsonBodySerializerAdapter adapter = new HttpJsonBodySerializerAdapter(serializer) {
            @Override
            protected Serializer serializer() {
                return serializer;
            }
        };
        final Class<? extends HttpJsonBodySerializerAdapter> adapterClass = adapter.getClass();
        final Method serializerMethod = adapterClass.getDeclaredMethod("serializer");
        final Object invoke = serializerMethod.invoke(adapter);
        assertEquals(serializer, invoke);
    }
}
