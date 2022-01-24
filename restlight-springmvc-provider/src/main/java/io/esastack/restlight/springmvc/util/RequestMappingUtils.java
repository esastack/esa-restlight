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
package io.esastack.restlight.springmvc.util;

import esa.commons.StringUtils;
import esa.commons.UrlUtils;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.util.MappingUtils;
import io.esastack.restlight.springmvc.annotation.shaded.RequestMapping0;
import io.esastack.restlight.springmvc.annotation.shaded.ValueConstants0;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public final class RequestMappingUtils {

    /**
     * Normalise the given origin default value which would be the value of {@link ValueConstants0#defaultNone()} in
     * spring mvc environment.
     *
     * @param value origin default value
     * @return normalised value
     */
    public static String normaliseDefaultValue(String value) {
        if (ValueConstants0.defaultNone().equals(value)) {
            return null;
        }
        return value;
    }

    /**
     * @see #extractMapping(Class, Method, String)
     */
    public static Optional<Mapping> extractMapping(Class<?> userType,
                                                   Method method) {
        return extractMapping(userType, method, null);
    }

    /**
     * Extracts an instance of {@link Mapping} from given {@link Method} which may be annotated by the spring mvc
     * request mapping annotations such as {@code @RequestMapping}, {@code @GetMapping}, {@code PostMapping} and so
     * on...
     *
     * @param userType    type of target method's declaring class.
     * @param method      target method
     * @param contextPath context path
     * @return optional value of {@link Mapping}
     */
    public static Optional<Mapping> extractMapping(Class<?> userType,
                                                   Method method,
                                                   String contextPath) {
        if (method == null || userType == null) {
            return Optional.empty();
        }
        contextPath = ConverterUtils.standardContextPath(contextPath);
        //resolve the parent @RequestMapping
        RequestMapping0 parentRequestMapping = findClassRequestMapping(userType);
        //resolve the method @RequestMapping
        RequestMapping0 requestMapping = findMethodRequestMapping(method);

        return buildRouteMappingInfo(requestMapping, parentRequestMapping, contextPath);
    }

    private static RequestMapping0 findClassRequestMapping(Class<?> element) {
        Annotation ann =
                AnnotationUtils.findAnyAnnotation(element, RequestMapping0.extendedClasses(), true);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(element,
                    RequestMapping0.shadedClass(), true);
        }
        return RequestMapping0.fromShade(ann);
    }

    private static RequestMapping0 findMethodRequestMapping(Method element) {
        Annotation ann =
                AnnotationUtils.findAnyAnnotation(element, RequestMapping0.extendedClasses(), true);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(element,
                    RequestMapping0.shadedClass(), true);
        }
        return RequestMapping0.fromShade(ann);
    }

    private static Optional<Mapping> buildRouteMappingInfo(RequestMapping0 requestMapping,
                                                           RequestMapping0 parentRequestMapping,
                                                           String contextPath) {
        if (requestMapping == null) {
            return Optional.empty();
        }
        if (parentRequestMapping == null) {
            return Optional.of(build(requestMapping, contextPath));
        } else {
            final Mapping parent = build(parentRequestMapping, contextPath);
            final Mapping child = build(requestMapping, StringUtils.empty());
            return Optional.of(MappingUtils.combine(parent, child));
        }
    }

    private static Mapping build(RequestMapping0 requestMapping, String contextPath) {
        String[] pathStartsWithContextPath =
                Arrays.stream(requestMapping.path())
                        .map(path -> {
                            if (contextPath != null) {
                                return contextPath + UrlUtils.prependLeadingSlash(path);
                            }
                            return path;
                        })
                        .toArray(String[]::new);

        return Mapping.mapping()
                .name(requestMapping.name())
                .path(pathStartsWithContextPath)
                .method(Arrays.stream(requestMapping.method())
                        .map(HttpMethod::valueOf)
                        .toArray(HttpMethod[]::new))
                .headers(requestMapping.headers())
                .params(requestMapping.params())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces());
    }

    private RequestMappingUtils() {
    }

}
