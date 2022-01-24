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
package io.esastack.restlight.server.util;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.impl.MappingImpl;
import io.esastack.restlight.server.route.predicate.ConsumesPredicate;
import io.esastack.restlight.server.route.predicate.HeadersPredicate;
import io.esastack.restlight.server.route.predicate.ProducesPredicate;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class MappingUtils {

    /**
     * Combines given child {@link Mapping} into the parent {@link Mapping}.
     *
     * @param parent parent mapping
     * @param child  child mapping
     * @return combined
     */
    public static Mapping combine(Mapping parent, Mapping child) {
        if (parent == null) {
            return child;
        }
        if (child == null) {
            return parent;
        }
        MappingImpl mapping = Mapping.mapping()
                .name(StringUtils.nonEmptyOrElse(child.name(), parent.name()))
                .path(combinePaths(parent.path(), child.path()))
                .method(removeDuplicate(parent.method(), child.method(), new HttpMethod[0]))
                .params(removeDuplicateString(parent.params(), child.params()));

        String[] parentHeaders = parent.headers();
        if (parseConsumeExpressions(child.consumes(), child.headers()).isEmpty()) {
            // could not parse any consumes from child mapping
            // so we use parent consumes and parent headers
            mapping = mapping.consumes(parent.consumes());
        } else {
            // parsed consumes from child mapping, so we need to ignore the parent consumes and content-type in
            // parent headers.
            mapping = mapping.consumes(child.consumes());
            parentHeaders = removeConsumes(parentHeaders);
        }

        if (parseProduceExpressions(child.produces(), child.headers()).isEmpty()) {
            // could not parse any produces from child mapping
            // so we use parent consumes and parent headers
            mapping = mapping.produces(parent.produces());
        } else {
            mapping = mapping.produces(child.produces());
            parentHeaders = removeAccepts(parentHeaders);
        }
        // merge parent headers and child headers
        return mapping.headers(removeDuplicateString(parentHeaders, child.headers()));
    }


    /**
     * Parses given consumes predicates strings and headers predicates strings to {@link
     * ConsumesPredicate.Expression}.
     *
     * @param consumes consumes
     * @param headers  headers
     * @return parsed expressions
     */
    public static Set<ConsumesPredicate.Expression> parseConsumeExpressions(String[] consumes,
                                                                            String[] headers) {
        Set<ConsumesPredicate.Expression> result = new LinkedHashSet<>();
        if (headers != null) {
            for (String header : headers) {
                HeadersPredicate.Expression expr =
                        new HeadersPredicate.Expression(header);
                if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(expr.getName())) {
                    for (MediaType mediaType : MediaTypeUtil.parseMediaTypes(expr.getValue())) {
                        result.add(new ConsumesPredicate.Expression(mediaType,
                                expr.isNegated()));
                    }
                }
            }
        }
        if (consumes != null) {
            for (String consume : consumes) {
                result.add(new ConsumesPredicate.Expression(consume));
            }
        }

        return result;
    }

    /**
     * Parses given produces predicates strings and headers predicates strings to {@link
     * ProducesPredicate.Expression}.
     *
     * @param produces consumes
     * @param headers  headers
     * @return parsed expressions
     */
    public static Set<ProducesPredicate.Expression> parseProduceExpressions(String[] produces,
                                                                            String[] headers) {
        Set<ProducesPredicate.Expression> result = new LinkedHashSet<>();
        if (headers != null) {
            for (String header : headers) {
                HeadersPredicate.Expression expr =
                        new HeadersPredicate.Expression(header);
                if (HttpHeaderNames.ACCEPT.contentEqualsIgnoreCase(expr.getName()) && expr.getValue() != null) {
                    for (MediaType mediaType : MediaTypeUtil.parseMediaTypes(expr.getValue())) {
                        result.add(new ProducesPredicate.Expression(mediaType,
                                expr.isNegated()));
                    }
                }
            }
        }
        if (produces != null) {
            for (String produce : produces) {
                result.add(new ProducesPredicate.Expression(produce));
            }
        }
        return result;
    }

    /**
     * Determines whether the given components may intersect potentially.
     *
     * @param a   components a
     * @param b   components a
     * @param <T> component type
     * @return {@code true} if given components may intersect potentially.
     */
    public static <T> boolean isIntersect(T[] a, T[] b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length == 0 || b.length == 0) {
            return false;
        }
        Set<T> set1 = new HashSet<>(Arrays.asList(a));
        Set<T> set2 = new HashSet<>(Arrays.asList(b));
        set1.retainAll(set2);
        return !set1.isEmpty();
    }

    /**
     * Combines the paths from parent {@link Mapping}
     * <p>
     * use the {@link PathMatcher#combine(String, String)} to combine the parent paths and current paths if the paths of
     * the parent and current is both present, otherwise use the paths that is present, or just padding a empty string.
     */
    private static String[] combinePaths(String[] parentPaths, String[] childPaths) {
        final Set<String> result = new LinkedHashSet<>();
        if (parentPaths.length != 0 && childPaths.length != 0) {
            for (String pattern1 : parentPaths) {
                for (String pattern2 : childPaths) {
                    result.add(PathMatcher.combine(pattern1, pattern2));
                }
            }
        } else if (parentPaths.length != 0) {
            result.addAll(Arrays.asList(parentPaths));
        } else if (childPaths.length != 0) {
            result.addAll(Arrays.asList(childPaths));
        } else {
            result.add("");
        }
        return result.toArray(new String[0]);
    }

    private static boolean isConsume(String header) {
        HeadersPredicate.Expression expr =
                new HeadersPredicate.Expression(header);
        return isConsume(expr);
    }

    private static boolean isAccept(String header) {
        HeadersPredicate.Expression expr =
                new HeadersPredicate.Expression(header);
        return isAccept(expr);
    }

    private static <T> T[] removeDuplicate(T[] origin, T[] other, T[] target) {
        if (origin == null) {
            return other;
        } else if (other == null) {
            return origin;
        }
        Set<T> set = new LinkedHashSet<>(Arrays.asList(origin));
        set.addAll(Arrays.asList(other));
        return set.toArray(target);
    }

    private static String[] removeDuplicateString(String[] origin, String[] other) {
        return removeDuplicate(origin, other, new String[0]);
    }

    private static String[] removeConsumes(String[] headers) {
        if (headers == null || headers.length == 0) {
            return null;
        }
        Set<String> newHeaders = new HashSet<>(headers.length);
        for (String header : headers) {
            if (!isConsume(header)) {
                newHeaders.add(header);
            }
        }
        return newHeaders.toArray(new String[0]);
    }

    private static String[] removeAccepts(String[] headers) {
        if (headers == null || headers.length == 0) {
            return null;
        }
        Set<String> newHeaders = new HashSet<>(headers.length);
        for (String header : headers) {
            if (!isAccept(header)) {
                newHeaders.add(header);
            }
        }
        return newHeaders.toArray(new String[0]);
    }

    private static boolean isConsume(HeadersPredicate.Expression expr) {
        return HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(expr.getName());
    }

    private static boolean isAccept(HeadersPredicate.Expression expr) {
        return HttpHeaderNames.ACCEPT.contentEqualsIgnoreCase(expr.getName()) && expr.getValue() != null;
    }
}
