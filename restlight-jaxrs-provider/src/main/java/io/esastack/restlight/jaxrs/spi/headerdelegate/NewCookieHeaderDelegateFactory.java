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
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.Set;

public class NewCookieHeaderDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new NewCookieHeaderDelegate();
    }

    static class NewCookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<NewCookie> {

        @Override
        public NewCookie fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to NewCookie");
            }
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(value);
            if (cookies.isEmpty()) {
                return null;
            }
            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                return new NewCookie(cookie.name(), cookie.value(), cookie.path(), cookie.domain(),
                        NewCookie.DEFAULT_VERSION, null, (int) cookie.maxAge(), cookie.isSecure());
            }
            return null;
        }

        @Override
        public String toString(NewCookie value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(NewCookie) to String");
            }
            final StringBuilder sb = new StringBuilder("NewCookie{");
            sb.append("name=").append(value.getName());
            sb.append(", value=").append(value.getValue());
            sb.append(", domain=").append(value.getDomain());
            sb.append(", path=").append(value.getPath());
            sb.append(", version=").append(value.getVersion());
            sb.append(", comment=").append(value.getComment());
            sb.append(", maxAge=").append(value.getMaxAge());
            sb.append(", expiry=").append(value.getExpiry());
            sb.append(", secure=").append(value.isSecure());
            sb.append(", httpOnly=").append(value.isHttpOnly());
            sb.append('}');
            return sb.toString();
        }

    }
}

