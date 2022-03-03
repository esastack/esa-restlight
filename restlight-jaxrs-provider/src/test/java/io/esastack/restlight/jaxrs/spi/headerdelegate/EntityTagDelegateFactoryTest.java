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

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTagDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final EntityTagDelegateFactory factory = new EntityTagDelegateFactory();
        RuntimeDelegate.HeaderDelegate<EntityTag> delegate = (RuntimeDelegate.HeaderDelegate<EntityTag>)
                factory.headerDelegate();
        assertNull(delegate.toString(null));
        assertNull(delegate.fromString(null));

        final String value1 = "W/\"abc\"";
        final EntityTag tag1 = delegate.fromString(value1);
        assertNotNull(tag1);
        assertEquals("abc", tag1.getValue());
        assertTrue(tag1.isWeak());
        assertEquals(value1, delegate.toString(tag1));

        final String value2 = "\"xyz\"";
        final EntityTag tag2 = delegate.fromString(value2);
        assertNotNull(tag2);
        assertEquals("xyz", tag2.getValue());
        assertFalse(tag2.isWeak());
        assertEquals(value2, delegate.toString(tag2));
    }

}

