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

import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.MethodParamImpl;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ApplicationResolverAdapterTest {

    @Test
    void testAll() throws Throwable {
        assertThrows(NullPointerException.class, () -> new ApplicationResolverAdapter(null));

        final Application application = mock(Application.class);
        final ApplicationResolverAdapter adapter = new ApplicationResolverAdapter(application);
        assertEquals(1000, adapter.getOrder());

        assertSame(application, adapter.resolve(null));
        assertTrue(adapter.supports(new ConstructorParamImpl(A.class.getConstructor(Application.class), 0)));
        assertTrue(adapter.supports(new FieldParamImpl(A.class.getDeclaredField("application"))));
        assertTrue(adapter.supports(new MethodParamImpl(A.class.getDeclaredMethod("setApplication",
                ApplicationImpl.class), 0)));
        assertTrue(adapter.supports(new MethodParamImpl(A.class.getDeclaredMethod("sayHell0",
                Application.class), 0)));
    }

    private static final class A {

        @Context
        private Application application;

        public A(@Context Application application) {

        }

        private void sayHell0(@Context Application application) {

        }

        @Context
        private void setApplication(ApplicationImpl application) {

        }
    }

    private static final class ApplicationImpl extends Application {

    }
}

