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
package io.esastack.restlight.server.route.predicate;

import esa.commons.Checks;
import esa.commons.MathUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.util.MappingUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodPredicate implements RequestPredicate {

    private final HttpMethod[] methods
            = new HttpMethod[MathUtils.nextPowerOfTwo(HttpMethod.values().length)];
    private final int mask = methods.length - 1;
    private final boolean containsGet;
    private String str;

    MethodPredicate(HttpMethod... requestMethods) {
        Checks.checkNotEmptyArg(requestMethods, "HttpMethods must not be null or empty");
        boolean containsGet = false;
        for (HttpMethod requestMethod : requestMethods) {
            fill(requestMethod);
            if (requestMethod == HttpMethod.GET) {
                containsGet = true;
            }
        }
        this.containsGet = containsGet;
    }

    private void fill(HttpMethod httpMethod) {
        int index = hash(httpMethod.name()) & mask;
        if (index < 0 || index >= methods.length) {
            throw new IllegalArgumentException("Unexpected index of HttpMethod." + httpMethod);
        }
        if (methods[index] != null) {
            throw new IllegalArgumentException("Unexpected duplicated index of HttpMethod." +
                    httpMethod + " and HttpMethod." + methods[index]);
        }
        methods[index] = httpMethod;
    }

    @Override
    public boolean test(RequestContext context) {
        final HttpRequest request = context.request();
        final String method = request.rawMethod();
        return contains(method)
                || HttpMethod.HEAD.equals(request.method())
                && containsGet;
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        MethodPredicate that = (MethodPredicate) another;
        HttpMethod[] thisMethods = Arrays.stream(methods)
                .filter(Objects::nonNull)
                .toArray(HttpMethod[]::new);
        HttpMethod[] thatMethods = Arrays.stream(that.methods)
                .filter(Objects::nonNull)
                .toArray(HttpMethod[]::new);
        return MappingUtils.isIntersect(thisMethods, thatMethods);
    }

    @Override
    public String toString() {
        if (str == null) {
            str = "{methods=" + Arrays.stream(methods)
                    .filter(Objects::nonNull)
                    .map(HttpMethod::toString)
                    .collect(Collectors.joining(",")) + '}';
        }
        return str;
    }

    private static int hash(String name) {
        // This hash code needs to produce a unique index in the "values" array for each HttpMethod. If new
        // HttpMethods are added this algorithm will need to be adjusted.
        // For example with the current set of HttpMethods it just so happens that the String hash code value
        // shifted right by 6 bits modulo 16 is unique relative to all other HttpMethod values.
        return name.hashCode() >>> 6;
    }

    private boolean contains(String method) {
        HttpMethod value = methods[hash(method) & mask];
        return value != null && value.name().equals(method);
    }
}
