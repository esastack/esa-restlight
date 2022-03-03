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
package io.esastack.restlight.jaxrs.spi.headerdelegate;

import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class URIDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final URIDelegateFactory factory = new URIDelegateFactory();
        RuntimeDelegate.HeaderDelegate<URI> delegate = (RuntimeDelegate.HeaderDelegate<URI>)
                factory.headerDelegate();
        assertNotNull(delegate);

        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));

        final String str = "http://localhost:8080/abc?x=y&m=n#xyz";
        final URI uri = delegate.fromString(str);
        assertEquals("http", uri.getScheme());
        assertEquals("localhost", uri.getHost());
        assertEquals(8080, uri.getPort());
        assertEquals("/abc", uri.getPath());
        assertEquals("x=y&m=n", uri.getQuery());
        assertEquals("xyz", uri.getFragment());

        assertEquals(str, delegate.toString(uri));
    }

}

