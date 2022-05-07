/*
 * Copyright 2022 OPPO ESA Stack Project
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

import esa.commons.Checks;
import esa.commons.UrlUtils;
import esa.commons.collection.LinkedMultiArrayValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.annotation.Intercepted;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.Route;

import java.util.List;
import java.util.Optional;

import static io.esastack.restlight.core.interceptor.HandlerInterceptor.PATTERN_FOR_ALL;

public final class InterceptorUtils {

    public static boolean isIntercepted(HandlerMethod handler) {
        Intercepted intercepted = handler.getMethodAnnotation(Intercepted.class, false);
        if (intercepted == null) {
            intercepted = handler.getClassAnnotation(Intercepted.class, false);
        }
        if (intercepted == null) {
            return true;
        }
        return intercepted.value();
    }

    static MultiValueMap<InterceptorPredicate, Interceptor> filter(DeployContext
                                                                           context,
                                                                   Mapping mapping,
                                                                   Object handler,
                                                                   List<InterceptorFactory> interceptors) {
        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                new LinkedMultiArrayValueMap<>(interceptors.size());
        // use a fake route to represent
        final Route fake = Route.route().mapping(mapping).handler(handler);

        for (InterceptorFactory factory : interceptors) {
            Optional<Interceptor> interceptor = factory.create(context, fake);
            int affinity;
            if (!interceptor.isPresent() || (affinity = interceptor.get().affinity()) < 0) {
                continue;
            }
            if (affinity == 0) {
                // certainly match
                filtered.add(InterceptorPredicate.ALWAYS, interceptor.get());
            } else {
                filtered.add(Checks.checkNotNull(interceptor.get().predicate(),
                        "Unexpected null predicate of interceptor '" + interceptor.get() + "'"),
                        interceptor.get());
            }
        }
        return filtered;
    }

    public static String[] parseIncludesOrExcludes(String contextPath, String[] patterns) {
        String[] withContextPath = null;
        if (patterns != null) {
            withContextPath = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                if (!PATTERN_FOR_ALL.equals(patterns[i])) {
                    withContextPath[i] = ConverterUtils.standardContextPath(contextPath)
                            + UrlUtils.prependLeadingSlash(patterns[i]);
                } else {
                    withContextPath[i] = UrlUtils.prependLeadingSlash(patterns[i]);
                }
            }
        }
        return withContextPath;
    }

    private InterceptorUtils() {
    }
}
