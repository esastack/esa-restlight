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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaTypeHeaderDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final MediaTypeHeaderDelegateFactory factory = new MediaTypeHeaderDelegateFactory();
        RuntimeDelegate.HeaderDelegate<MediaType> delegate = (RuntimeDelegate.HeaderDelegate<MediaType>)
                factory.headerDelegate();
        assertNotNull(delegate);

        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));

        final String value1 = "*/*;charset=utf-8;q=1.0";
        final MediaType mediaType1 = delegate.fromString(value1);
        assertEquals("*", mediaType1.getType());
        assertEquals("*", mediaType1.getSubtype());
        assertEquals(2, mediaType1.getParameters().size());
        assertEquals("utf-8", mediaType1.getParameters().get("charset"));
        assertEquals("1.0", mediaType1.getParameters().get("q"));
        assertEquals(value1, delegate.toString(mediaType1));

        final String value2 = "application/json";
        final MediaType mediaType2 = delegate.fromString(value2);
        assertEquals("application", mediaType2.getType());
        assertEquals("json", mediaType2.getSubtype());
        assertEquals(0, mediaType2.getParameters().size());
        assertEquals(value2, delegate.toString(mediaType2));
    }

}

