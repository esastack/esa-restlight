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
package io.esastack.restlight.jaxrs.configure;

import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.MethodParamImpl;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

final class ResolverUtils {

    @SuppressWarnings("unchecked")
    static ResolvableParam<ConstructorParam, ContextResolver>[] contextResolversOfCons(Constructor<?> constructor,
                                                                                       ResolvableParamPredicate
                                                                                               predicate,
                                                                                       HandlerResolverFactory
                                                                                               factory) {
        List<ResolvableParam<ConstructorParam, ContextResolver>> params = new LinkedList<>();
        int size = constructor.getParameterCount();
        int index = 0;
        while (index++ <= size) {
            ConstructorParam param = new ConstructorParamImpl(constructor, index);
            if (predicate.isResolvable(param)) {
                params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
            }
        }
        return params.toArray(new ResolvableParam[0]);
    }

    @SuppressWarnings("unchecked")
    static ResolvableParam<MethodParam, ContextResolver>[] contextResolversOfSetter(Class<?> clazz,
                                                                                    ResolvableParamPredicate
                                                                                            predicate,
                                                                                    HandlerResolverFactory factory) {

        List<ResolvableParam<MethodParam, ContextResolver>> params = new LinkedList<>();
        ReflectionUtils.getAllDeclaredMethods(clazz).stream()
                .filter(ReflectionUtils::isSetter)
                .forEach(m -> {
                    MethodParam param = new MethodParamImpl(m, 0);
                    if (predicate.isResolvable(param)) {
                        params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
                    }
                });
        return params.toArray(new ResolvableParam[0]);
    }

    @SuppressWarnings("unchecked")
    static ResolvableParam<FieldParam, ContextResolver>[] contextResolversOfField(Class<?> clazz,
                                                                                  ResolvableParamPredicate predicate,
                                                                                  HandlerResolverFactory factory) {
        List<ResolvableParam<FieldParam, ContextResolver>> params = new LinkedList<>();
        ReflectionUtils.getAllDeclaredFields(clazz)
                .forEach(f -> {
                    FieldParam param = new FieldParamImpl(f);
                    if (predicate.isResolvable(param)) {
                        params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
                    }
                });
        return params.toArray(new ResolvableParam[0]);
    }

    private ResolverUtils() {
    }
}

