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

import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.ResolvableParamPredicate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ConstructorUtils {

    public static Constructor<?> extractResolvable(Class<?> clazz, ResolvableParamPredicate predicate) {
        Constructor<?> matched = null;
        for (Constructor<?> constructor : extractAllResolvable(clazz, predicate)) {
            if (matched == null
                    || constructor.getParameterCount() > matched.getParameterCount()) {
                matched = constructor;
            }
        }
        return matched;
    }

    public static List<Constructor<?>> extractAllResolvable(Class<?> clazz, ResolvableParamPredicate predicate) {
        List<Constructor<?>> constructors = new LinkedList<>();
        if (clazz.isInterface()) {
            return constructors;
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            if (isResolvable(constructor, predicate)) {
                constructors.add(constructor);
            }
        }

        return constructors;
    }

    private static boolean isResolvable(Constructor<?> constructor, ResolvableParamPredicate predicate) {
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            ConstructorParam param = new ConstructorParamImpl(constructor, i);
            if (!predicate.isResolvable(param)) {
                return false;
            }
        }
        return true;
    }

    private ConstructorUtils() {
    }

}

