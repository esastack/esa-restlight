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
package esa.restlight.core.interceptor;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.InterceptorUtils;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.util.PathMatcher;

import java.util.Arrays;

import static esa.restlight.core.interceptor.HandlerInterceptor.PATTERN_FOR_ALL;

class HandlerInterceptorWrap extends AbstractInterceptorWrap<HandlerInterceptor> {

    private final InterceptorPredicate predicate;
    private final int affinity;

    HandlerInterceptorWrap(HandlerInterceptor interceptor,
                           DeployContext<? extends RestlightOptions> ctx,
                           Route route) {
        super(interceptor);
        final Mapping mapping = route.mapping();
        final String[] includes =
                InterceptorUtils.parseIncludesOrExcludes(ctx.options().getContextPath(),
                        interceptor.includes());
        final String[] excludes =
                InterceptorUtils.parseIncludesOrExcludes(ctx.options().getContextPath(),
                        interceptor.excludes());

        this.affinity = parseAffinity(mapping, includes, excludes);

        if (this.affinity < 0) {
            this.predicate = InterceptorPredicate.NEVER;
        } else if (this.affinity == 0) {
            this.predicate = InterceptorPredicate.ALWAYS;
        } else {
            this.predicate = new InterceptorPathPredicate(includes, excludes);
        }
    }

    private int parseAffinity(Mapping mapping, String[] includes, String[] excludes) {
        String[] patterns;
        //    public static Affinity matchToPattern(InterceptorPathPredicate predicate, String[] patterns) {
        if (mapping.path() == null || (patterns = mapping.path()).length == 0) {
            return DETACHED;
        } else if (isMatchAll(includes, excludes)) {
            // ignore always matching predicate
            return ATTACHED;
        } else if (isMatchEmpty(includes, excludes)) {
            // ignore empty matching
            return DETACHED;
        } else if (certainlyMatchAll(patterns, excludes)) {
            // rule out if excludes will match to these patterns certainly
            return DETACHED;
        } else if ((excludes == null || excludes.length == 0 || neverIntersect(excludes, patterns))
                && (includes == null || certainlyMatchAll(patterns, includes))) {
            // excludes must be empty, because excludes may contains a pattern that would be matched to the route
            // pattern. eg. route pattern: fo?, includes: fo?, excludes: foo
            // this includes will certainly match to these patterns.
            return ATTACHED;
        } else if (includes != null && (includes.length == 0 || neverIntersect(includes, patterns))) {
            return DETACHED;
        } else {
            // we think it is expensive to match a request to a interceptor while this interceptor
            // is not always matched to this handler and there's pattern paths in includes or excludes
            int affinity = 1;
            if (includes != null) {
                for (String include : includes) {
                    affinity += computeAffinity(include);
                }
            }
            if (excludes != null) {
                for (String exclude : excludes) {
                    if (PathMatcher.isPattern(exclude)) {
                        affinity += computeAffinity(exclude);
                    }
                }
            }
            return affinity;
        }
    }

    private static boolean isMatchAll(String[] includes, String[] excludes) {
        return (excludes == null || excludes.length == 0)
                && (includes == null || containsAll(includes));
    }

    private static boolean isMatchEmpty(String[] includes, String[] excludes) {
        return (includes != null && includes.length == 0) || (excludes != null && containsAll(excludes));
    }

    private static boolean containsAll(String[] arr) {
        for (String include : arr) {
            if (PATTERN_FOR_ALL.equals(include)) {
                return true;
            }
        }
        return false;
    }

    private boolean neverIntersect(String[] includes, String[] patterns) {
        // rule out if includes will never match to these patterns certainly
        if (includes != null && includes.length > 0) {
            for (String include : includes) {
                if (Arrays.stream(patterns)
                        .anyMatch(pattern -> PathMatcher.isPotentialIntersect(include, pattern))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean certainlyMatchAll(String[] patterns, String[] target) {
        boolean result = false;
        if (target != null && target.length > 0) {
            for (String value : target) {
                if (Arrays.stream(patterns)
                        .allMatch(pattern -> PathMatcher.certainlyIncludes(value, pattern))) {
                    result = true;
                }
            }
        }

        return result;
    }

    private int computeAffinity(String include) {
        int affinity = 0;
        if (PathMatcher.isPattern(include)) {
            affinity += 4;
        }
        // every wildcard char plus 2 affinity(affinity gonna to be lower)
        affinity += include.chars().map(c -> {
            if (PathMatcher.isWildcardChar((char) c)) {
                return 2;
            }
            return 0;
        }).sum();
        return affinity;
    }

    @Override
    public InterceptorPredicate predicate() {
        return predicate;
    }

    @Override
    public int affinity() {
        return affinity;
    }
}
