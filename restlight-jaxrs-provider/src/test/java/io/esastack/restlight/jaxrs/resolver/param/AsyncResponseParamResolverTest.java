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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncResponseParamResolverTest {

    @Test
    void testAll() throws Throwable {
        final AsyncResponseParamResolver resolver = new AsyncResponseParamResolver();

        assertFalse(resolver.supports(new ConstructorParamImpl(A.class.getConstructor(AsyncResponse.class), 0)));
        assertTrue(resolver.supports(new MethodParamImpl(A.class.getDeclaredMethod("sayHell0",
                AsyncResponse.class), 0)));
        assertNotNull(resolver.createResolver(null, null, null, null));
    }

    private static final class A {

        public A(@Suspended AsyncResponse asyncResponse) {

        }

        private void sayHell0(@Suspended AsyncResponse asyncResponse) {

        }

    }
}

