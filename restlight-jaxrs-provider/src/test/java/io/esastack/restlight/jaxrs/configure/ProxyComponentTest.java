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
package io.esastack.restlight.jaxrs.configure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyComponentTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new ProxyComponent<>(null, new Object()));
        assertThrows(NullPointerException.class, () -> new ProxyComponent<>(new Object(), null));

        final Object underlying = new Object();
        final Object proxied = new Object();
        ProxyComponent<Object> component = new ProxyComponent<>(underlying, proxied);
        assertSame(underlying, component.underlying());
        assertSame(proxied, component.proxied());
    }

}

