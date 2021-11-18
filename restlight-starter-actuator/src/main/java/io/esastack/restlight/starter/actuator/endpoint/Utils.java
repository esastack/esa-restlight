/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.starter.actuator.endpoint;

import io.esastack.restlight.server.bootstrap.AbstractDelegatedRestlightServer;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;

final class Utils {
    private Utils() {
    }

    static RestlightServer findServer(AbstractDelegatedRestlightServer server) {
        if (server == null) {
            return null;
        }
        RestlightServer s = server.unWrap();
        if (s instanceof AbstractDelegatedRestlightServer) {
            return findServer((AbstractDelegatedRestlightServer) s);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<T> findField(Object target, String name, Class<T> type) {
        T value = null;
        try {
            Field field = ReflectionUtils.findField(target.getClass(), name, type);
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                value = (T) ReflectionUtils.getField(field, target);
            }
        } catch (Throwable ignored) {
        }
        return Optional.ofNullable(value);
    }
}
