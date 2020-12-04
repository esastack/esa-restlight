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
package esa.restlight.server.route.predicate;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.server.route.Mapping;
import io.netty.handler.codec.http.HttpResponseStatus;

public class RoutePredicate implements RequestPredicate {

    public static final String MATCH_STATUS = "$mth.sts";

    private final PatternsPredicate patterns;
    private final MethodPredicate method;
    private final ParamsPredicate params;
    private final HeadersPredicate headers;
    private final ConsumesPredicate consumes;
    private final ProducesPredicate produces;

    private RoutePredicate(PatternsPredicate patterns,
                           MethodPredicate method,
                           ParamsPredicate params,
                           HeadersPredicate headers,
                           ConsumesPredicate consumes,
                           ProducesPredicate produces) {
        Checks.checkNotNull(patterns);
        this.patterns = patterns;
        this.method = method;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    @Override
    public boolean test(AsyncRequest request) {
        if (!patterns.test(request)) {
            return false;
        }

        if (method != null && !method.test(request)) {
            request.setAttribute(MATCH_STATUS, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return false;
        }

        if (params != null && !params.test(request)) {
            return false;
        }

        if (headers != null && !headers.test(request)) {
            return false;
        }

        if (consumes != null && !consumes.test(request)) {
            request.setAttribute(MATCH_STATUS, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
            return false;
        }

        if (produces != null && !produces.test(request)) {
            request.setAttribute(MATCH_STATUS, HttpResponseStatus.NOT_ACCEPTABLE);
            return false;
        }
        return true;
    }

    public static RoutePredicate parseFrom(Mapping mapping) {
        Checks.checkNotNull(mapping, "mapping");
        PatternsPredicate patterns = new PatternsPredicate(mapping.path());
        MethodPredicate method = createMethodCondition(mapping.method());
        ParamsPredicate params = null;
        if (mapping.params() != null && mapping.params().length > 0) {
            params = new ParamsPredicate(mapping.params());
        }
        HeadersPredicate headers = HeadersPredicate.parseFrom(mapping.headers());
        ConsumesPredicate consumes = ConsumesPredicate.parseFrom(mapping.consumes(), mapping.headers());
        ProducesPredicate produces = ProducesPredicate.parseFrom(mapping.produces(), mapping.headers());

        return new RoutePredicate(patterns,
                method, params,
                headers,
                consumes,
                produces);
    }

    private static MethodPredicate createMethodCondition(HttpMethod[] methods) {
        return methods.length == 0 ? null : new MethodPredicate(methods);
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        RoutePredicate that = (RoutePredicate) another;
        return mayAmbiguousWith(patterns, that.patterns) &&
                mayAmbiguousWith(method, that.method) &&
                mayAmbiguousWith(params, that.params) &&
                mayAmbiguousWith(headers, that.headers) &&
                mayAmbiguousWith(consumes, that.consumes) &&
                mayAmbiguousWith(produces, that.produces);
    }

    private boolean mayAmbiguousWith(RequestPredicate p1, RequestPredicate p2) {
        if (p1 == p2) {
            return true;
        }
        if (p1 == null) {
            return p2.mayAmbiguousWith(p1);
        } else {
            return p1.mayAmbiguousWith(p2);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoutePredicate{");
        sb.append("patterns=").append(patterns);
        if (method != null) {
            sb.append(", method=").append(method);
        }
        if (params != null) {
            sb.append(", params=").append(params);
        }
        if (headers != null) {
            sb.append(", headers=").append(headers);
        }
        if (consumes != null) {
            sb.append(", consumes=").append(consumes);
        }
        if (produces != null) {
            sb.append(", produces=").append(produces);
        }
        sb.append('}');
        return sb.toString();
    }
}
