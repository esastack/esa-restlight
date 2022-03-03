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

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final LinkDelegateFactory factory = new LinkDelegateFactory();
        RuntimeDelegate.HeaderDelegate<Link> delegate = (RuntimeDelegate.HeaderDelegate<Link>)
                factory.headerDelegate();
        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));

        final String value = "</abc/def>;rel=\"xyz\";" +
                "title=\"pq\";type=\"application/json\";name=\"value\"";
        final Link link = delegate.fromString(value);
        assertEquals("/abc/def", link.getUri().toString());
        assertEquals("value", link.getParams().get("name"));
        assertEquals("xyz", link.getRel());
        assertEquals("pq", link.getTitle());
        assertEquals("application/json", link.getType());

        assertEquals(value, delegate.toString(link));
    }

}

