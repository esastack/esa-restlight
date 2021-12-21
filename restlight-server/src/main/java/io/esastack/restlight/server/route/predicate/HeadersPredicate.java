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

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.util.MappingUtils;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * HeadersPredicate
 * <p>
 * Implementation of RequestPredicate which try to judge whether current request is matching the {@link #expressions}
 */
public class HeadersPredicate implements RequestPredicate {

    private final Expression[] expressions;

    private HeadersPredicate(Collection<? extends Expression> expressions) {
        this.expressions = expressions.toArray(new Expression[0]);
    }

    public static String normaliseExpression(String expr) {
        return new Expression(expr).toString();
    }

    public static HeadersPredicate parseFrom(String... headers) {
        Collection<Expression> parsed = parseExpressions(headers);
        if (parsed.isEmpty()) {
            return null;
        }
        return new HeadersPredicate(parsed);
    }

    @Override
    public boolean test(RequestContext context) {
        for (Expression expression : expressions) {
            if (!expression.match(context.request())) {
                return false;
            }
        }
        return true;
    }

    private static Collection<Expression> parseExpressions(String... headers) {
        Set<Expression> expressions = new LinkedHashSet<>();
        if (headers != null) {
            for (String header : headers) {
                Expression expr = new Expression(header);
                if (HttpHeaderNames.ACCEPT.contentEqualsIgnoreCase(expr.name)
                        || HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(expr.name)) {
                    continue;
                }
                expressions.add(new Expression(header));
            }
        }
        return expressions;
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        return MappingUtils.isIntersect(this.expressions, ((HeadersPredicate) another).expressions);
    }

    @Override
    public String toString() {
        return "{headers=" + Arrays.toString(expressions) + '}';
    }

    public static class Expression extends AbstractNameValueExpression {

        public Expression(String expression) {
            super(expression);
        }

        @Override
        protected boolean isCaseSensitiveName() {
            return false;
        }

        @Override
        protected boolean matchName(HttpRequest request) {
            return request.headers().get(name) != null;
        }

        @Override
        protected boolean matchValue(HttpRequest request) {
            return Objects.equals(this.value, request.headers().get(this.name));
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

}
