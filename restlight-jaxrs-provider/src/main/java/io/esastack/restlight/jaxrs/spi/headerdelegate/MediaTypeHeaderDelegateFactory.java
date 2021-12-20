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

import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class MediaTypeHeaderDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new MediaTypeHeaderDelegate();
    }

    private static class MediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate<MediaType> {

        @Override
        public MediaType fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to MediaType");
            }
            return MediaTypeUtils.convert(MediaTypeUtil.parseMediaType(value));
        }

        @Override
        public String toString(MediaType value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(MediaType) to String");
            }
            return MediaTypeUtils.convert(value).value();
        }

    }
}

