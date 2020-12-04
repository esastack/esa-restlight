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
package esa.restlight.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated use {@link esa.commons.Primitives} please
 */
@Deprecated
public final class Primitives {

    private static final Map<Class<?>, Class<?>> WRAPPER_MAP
            = new HashMap<>(16);

    static {
        WRAPPER_MAP.put(Boolean.class, boolean.class);
        WRAPPER_MAP.put(Byte.class, byte.class);
        WRAPPER_MAP.put(Character.class, char.class);
        WRAPPER_MAP.put(Short.class, short.class);
        WRAPPER_MAP.put(Integer.class, int.class);
        WRAPPER_MAP.put(Long.class, long.class);
        WRAPPER_MAP.put(Double.class, double.class);
        WRAPPER_MAP.put(Float.class, float.class);
        WRAPPER_MAP.put(Void.class, void.class);
    }

    public static boolean isWrapperType(Class<?> type) {
        return type != null && WRAPPER_MAP.containsKey(type);
    }

    private Primitives() {
    }

}
