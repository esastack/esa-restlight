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
import io.esastack.restlight.server.util.HttpHeaderUtils;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.Locale;

public class LocaleHeaderDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new LocaleHeaderDelegate();
    }

    private static class LocaleHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Locale> {

        @Override
        public Locale fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to Locale");
            }
            return HttpHeaderUtils.parseToLanguage(value);
        }

        @Override
        public String toString(Locale value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(Locale) to String");
            }
            if (StringUtils.isEmpty(value.getCountry())) {
                return value.getLanguage();
            } else {
                return value.getLanguage() + '-' + value.getCountry();
            }
        }

    }
}

