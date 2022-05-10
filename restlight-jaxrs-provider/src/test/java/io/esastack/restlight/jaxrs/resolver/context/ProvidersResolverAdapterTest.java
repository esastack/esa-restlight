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
package io.esastack.restlight.jaxrs.resolver.context;

import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProvidersResolverAdapterTest {

    @Test
    void testAll() throws Throwable {
        assertThrows(NullPointerException.class, () -> new ProvidersResolverAdapter(null));

        final Providers providers = mock(Providers.class);
        final ProvidersResolverAdapter adapter = new ProvidersResolverAdapter(providers);
        assertEquals(1000, adapter.getOrder());

        assertSame(providers, adapter.resolve(null));
        assertTrue(adapter.supports(new ConstructorParamImpl(A.class.getConstructor(Providers.class), 0)));
        assertTrue(adapter.supports(new FieldParamImpl(A.class.getDeclaredField("providers"))));
        assertTrue(adapter.supports(new MethodParamImpl(A.class.getDeclaredMethod("setProviders",
                Providers.class), 0)));
        assertTrue(adapter.supports(new MethodParamImpl(A.class.getDeclaredMethod("sayHell0",
                Providers.class), 0)));
    }

    private static final class A {

        @Context
        private Providers providers;

        public A(@Context Providers providers) {

        }

        private void sayHell0(@Context Providers providers) {

        }

        @Context
        private void setProviders(Providers providers) {

        }
    }

}

