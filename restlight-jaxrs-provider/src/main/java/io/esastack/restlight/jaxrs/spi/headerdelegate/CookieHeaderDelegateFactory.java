/*
 * Copyright 2021 OPPO ESA Stack Project
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

import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class CookieHeaderDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new CookieHeaderDelegate(new NewCookieHeaderDelegateFactory.NewCookieHeaderDelegate());
    }

    private static class CookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Cookie> {

        private final NewCookieHeaderDelegateFactory.NewCookieHeaderDelegate underlying;

        private CookieHeaderDelegate(NewCookieHeaderDelegateFactory.NewCookieHeaderDelegate underlying) {
            this.underlying = underlying;
        }

        @Override
        public Cookie fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to Cookie");
            }
            return underlying.fromString(value);
        }

        @Override
        public String toString(Cookie value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(Cookie) to String");
            }
            return underlying.toString(new NewCookie(value));
        }

    }
}

