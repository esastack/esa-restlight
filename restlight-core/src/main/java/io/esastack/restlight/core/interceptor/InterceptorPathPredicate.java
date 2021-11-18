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
package io.esastack.restlight.core.interceptor;

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.server.util.PathMatcher;

import java.util.Arrays;

public class InterceptorPathPredicate implements InterceptorPredicate {

    private final String[] includes;
    private final String[] excludes;

    InterceptorPathPredicate(String[] includes, String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public boolean test(HttpRequest request) {
        return match(request.path());
    }

    /**
     * Judge current request if matches interception predicate
     *
     * @param lookupPath lookupPath
     *
     * @return match
     */
    private boolean match(String lookupPath) {
        if (excludes != null) {
            for (String pattern : excludes) {
                if (PathMatcher.match(pattern, lookupPath)) {
                    return false;
                }
            }
        }
        if (includes == null) {
            return true;
        } else {
            for (String pattern : includes) {
                if (PathMatcher.match(pattern, lookupPath)) {
                    return true;
                }
            }
            return false;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterceptorPathPredicate that = (InterceptorPathPredicate) o;
        return Arrays.equals(includes, that.includes) &&
                Arrays.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(includes);
        result = 31 * result + Arrays.hashCode(excludes);
        return result;
    }
}
