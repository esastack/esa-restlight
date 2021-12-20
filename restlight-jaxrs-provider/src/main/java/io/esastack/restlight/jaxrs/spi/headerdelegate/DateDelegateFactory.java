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
import io.esastack.restlight.server.util.DateUtils;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.Date;

public class DateDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new DateDelegate();
    }

    private static class DateDelegate implements RuntimeDelegate.HeaderDelegate<Date> {

        @Override
        public Date fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to Date");
            }
            return DateUtils.parseByCache(value);
        }

        @Override
        public String toString(Date value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(Date) to String");
            }
            return esa.commons.DateUtils.toString(value, esa.commons.DateUtils.yyyyMMddHHmmss);
        }
    }

}

