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
package io.esastack.restlight.core.util;

import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.ResolvableParamPredicate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConstructorUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConstructorUtils.class);

    public static Constructor<?> extractResolvable(Class<?> clazz, ResolvableParamPredicate resolvable) {
        Constructor<?> matched = null;
        for (Constructor<?> constructor : extractAllResolvable(clazz, resolvable)) {
            if (matched == null || constructor.getParameterCount() > matched.getParameterCount()) {
                matched = constructor;
            }
        }
        return matched;
    }

    private static List<Constructor<?>> extractAllResolvable(Class<?> clazz, ResolvableParamPredicate resolvable) {
        List<Constructor<?>> constructors = new LinkedList<>();
        if (clazz.isInterface()) {
            return constructors;
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            if (isResolvable(constructor, resolvable)) {
                constructors.add(constructor);
            }
        }

        return constructors;
    }

    private static boolean isResolvable(Constructor<?> constructor, ResolvableParamPredicate resolvable) {
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            ConstructorParam param = new ConstructorParamImpl(constructor, i);
            if (!resolvable.test(param)) {
                logger.warn("Constructor: {} can't be used to instantiate class: {}, due to unresolvable" +
                        " param: {}", constructor.getName() + " => " +
                        Arrays.toString(constructor.getParameterTypes()),
                        constructor.getDeclaringClass().getName(),
                        param);
                return false;
            }
        }
        return true;
    }

    private ConstructorUtils() {
    }

}

