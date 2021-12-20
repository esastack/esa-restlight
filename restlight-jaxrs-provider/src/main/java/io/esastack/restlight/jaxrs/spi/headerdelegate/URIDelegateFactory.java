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
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;

public class URIDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new URIHeaderDelegate();
    }

    private static class URIHeaderDelegate implements RuntimeDelegate.HeaderDelegate<URI> {

        @Override
        public URI fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to URI");
            }
            return URI.create(value);
        }

        @Override
        public String toString(URI value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(URI) to String");
            }
            return value.toString();
        }

    }
}

