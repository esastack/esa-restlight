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
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewCookieHeaderDelegateTest {

    @SuppressWarnings("unchecked")
    @Test
    void testAll() {
        final NewCookieHeaderDelegateFactory factory = new NewCookieHeaderDelegateFactory();
        RuntimeDelegate.HeaderDelegate<NewCookie> delegate = (RuntimeDelegate.HeaderDelegate<NewCookie>)
                factory.headerDelegate();
        assertNotNull(delegate);

        assertThrows(IllegalArgumentException.class, () -> delegate.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> delegate.toString(null));

        final Date date = DateUtils.parseByCache("2021-12-31 12:12:12");
        final String stringCookie = "name=value;Path=/path;Max-Age=100;Expires="
                + DateUtils.format(date.getTime()) + ";Domain=localhost;Secure;HttpOnly;Comment=abc";
        NewCookie cookie = delegate.fromString(stringCookie);
        assertNotNull(cookie);
        assertEquals("name", cookie.getName());
        assertEquals("value", cookie.getValue());
        assertEquals("/path", cookie.getPath());
        assertEquals(100, cookie.getMaxAge());
        assertNotNull(cookie.getExpiry());
        assertEquals("localhost", cookie.getDomain());
        assertEquals("abc", cookie.getComment());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals(Cookie.DEFAULT_VERSION, cookie.getVersion());

        assertEquals("name=value;Domain=localhost;Path=/path;Max-Age=100;" +
                "Expires=2021-12-31 12:12:12;Comment=abc;Secure;HttpOnly;Version=1",
                delegate.toString(cookie));
    }

}

