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

import io.esastack.restlight.core.util.DateUtils;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CookieHeaderDelegateFactoryTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final CookieHeaderDelegateFactory factory = new CookieHeaderDelegateFactory();
        RuntimeDelegate.HeaderDelegate<Cookie> delegate = (RuntimeDelegate.HeaderDelegate<Cookie>)
                factory.headerDelegate();
        assertNotNull(delegate);

        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));
        final String stringCookie = "name=value;Path=/path;Max-Age=100;Expires="
                + DateUtils.format(new Date().getTime()) + ";Domain=localhost;Secure;HttpOnly";
        Cookie cookie = delegate.fromString(stringCookie);
        assertNotNull(cookie);
        assertEquals("name", cookie.getName());
        assertEquals("value", cookie.getValue());
        assertEquals("/path", cookie.getPath());
        assertEquals("localhost", cookie.getDomain());
        assertEquals(1, cookie.getVersion());

        assertEquals("name=value;Domain=localhost;Path=/path;Version=1", delegate.toString(cookie));
    }
}

