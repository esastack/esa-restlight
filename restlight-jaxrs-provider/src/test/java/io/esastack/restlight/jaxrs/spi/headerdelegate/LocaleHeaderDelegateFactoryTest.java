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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocaleHeaderDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final LocaleHeaderDelegateFactory factory = new LocaleHeaderDelegateFactory();
        RuntimeDelegate.HeaderDelegate<Locale> delegate = (RuntimeDelegate.HeaderDelegate<Locale>)
                factory.headerDelegate();
        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));

        final String value1 = "zh-CN;q=0.8";
        final Locale local1 = delegate.fromString(value1);
        assertEquals(Locale.SIMPLIFIED_CHINESE, local1);
        assertEquals("zh-CN", delegate.toString(local1));

        final String value2 = "zh;q=0.8";
        final Locale locale2 = delegate.fromString(value2);
        assertEquals("zh", locale2.getLanguage());
        assertEquals("", locale2.getCountry());
        assertEquals("zh", delegate.toString(locale2));
    }

}

