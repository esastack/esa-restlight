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
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class EntityTagDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new EntityTagDelegate();
    }

    private static class EntityTagDelegate implements RuntimeDelegate.HeaderDelegate<EntityTag> {

        @Override
        public EntityTag fromString(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            boolean weak = false;
            if (value.startsWith("W/")) {
                weak = true;
                value = value.substring(2);
            }
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }
            return new EntityTag(value, weak);
        }

        @Override
        public String toString(EntityTag value) {
            if (value == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (value.isWeak()) {
                sb.append("W/");
            }
            sb.append('"');
            sb.append(value.getValue());
            sb.append('"');
            return sb.toString();
        }
    }
}

