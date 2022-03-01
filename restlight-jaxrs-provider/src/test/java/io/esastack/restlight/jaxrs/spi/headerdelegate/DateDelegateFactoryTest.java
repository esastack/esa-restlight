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

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final DateDelegateFactory factory = new DateDelegateFactory();
        RuntimeDelegate.HeaderDelegate<Date> delegate = (RuntimeDelegate.HeaderDelegate<Date>)
                factory.headerDelegate();
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));

        final String value = "2021-12-31 12:12:12";
        Date date = delegate.fromString(value);
        assertNotNull(date);
        assertEquals(value, delegate.toString(date));
    }

}

