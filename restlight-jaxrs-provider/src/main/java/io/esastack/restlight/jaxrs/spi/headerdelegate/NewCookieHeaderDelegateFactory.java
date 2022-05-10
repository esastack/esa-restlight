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

import esa.commons.StringUtils;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import io.esastack.restlight.core.util.DateUtils;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.Date;

public class NewCookieHeaderDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new NewCookieHeaderDelegate();
    }

    static class NewCookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<NewCookie> {

        private static final String VERSION = "Version";
        private static final String PATH = "Path";
        private static final String DOMAIN = "Domain";
        private static final String MAX_AGE = "Max-Age";
        private static final String COMMENT = "Comment";
        private static final String SECURE = "Secure";
        private static final String EXPIRES = "Expires";
        private static final String HTTPONLY = "HttpOnly";

        @Override
        public NewCookie fromString(String str) {
            if (str == null) {
                throw new IllegalArgumentException("Failed to parse a null to NewCookie");
            }

            String name = null;
            String value = null;
            String path = null;
            String domain = null;
            String comment = null;
            int maxAge = NewCookie.DEFAULT_MAX_AGE;
            Date expiry = null;
            boolean secure = false;
            boolean httpOnly = false;

            String[] tokens = str.split(";");
            for (String token : tokens) {
                String trimmed = StringUtils.trim(token);
                if (token.startsWith(MAX_AGE)) {
                    maxAge = Integer.parseInt(trimmed.substring(MAX_AGE.length() + 1));
                } else if (trimmed.startsWith(PATH)) {
                    path = trimmed.substring(PATH.length() + 1);
                } else if (trimmed.startsWith(DOMAIN)) {
                    domain = trimmed.substring(DOMAIN.length() + 1);
                } else if (trimmed.startsWith(COMMENT)) {
                    comment = trimmed.substring(COMMENT.length() + 1);
                } else if (trimmed.startsWith(EXPIRES)) {
                    expiry = DateUtils.parse(trimmed.substring(EXPIRES.length() + 1));
                } else if (trimmed.startsWith(SECURE)) {
                    secure = true;
                } else if (trimmed.startsWith(HTTPONLY)) {
                    httpOnly = true;
                } else {
                    int index = trimmed.indexOf('=');
                    if (index != -1) {
                        name = trimmed.substring(0, index);
                        value = index == trimmed.length() + 1 ? "" : trimmed.substring(index + 1);
                    }
                }
            }

            if (name == null || value == null) {
                throw new IllegalArgumentException("Failed to parse '" + str + "' to NewCookie");
            }
            return new NewCookie(name, value, path, domain, jakarta.ws.rs.core.Cookie.DEFAULT_VERSION, comment,
                    maxAge, expiry, secure, httpOnly);
        }

        @Override
        public String toString(NewCookie value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(NewCookie) to String");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(value.getName()).append('=').append(value.getValue());
            if (value.getDomain() != null) {
                sb.append(';').append(DOMAIN).append('=').append(value.getDomain());
            }
            if (value.getPath() != null) {
                sb.append(';').append(PATH).append('=').append(value.getPath());
            }
            if (value.getMaxAge() != -1) {
                sb.append(';').append(MAX_AGE).append('=').append(value.getMaxAge());
            }
            if (value.getExpiry() != null) {
                sb.append(';').append(EXPIRES).append('=').append(DateUtils.format(value.getExpiry().getTime()));
            }
            if (value.getComment() != null) {
                sb.append(';').append(COMMENT).append('=').append(value.getComment());
            }
            if (value.isSecure()) {
                sb.append(';').append(SECURE);
            }
            if (value.isHttpOnly()) {
                sb.append(';').append(HTTPONLY);
            }
            sb.append(';').append(VERSION).append('=').append(value.getVersion());
            return sb.toString();
        }

    }
}

