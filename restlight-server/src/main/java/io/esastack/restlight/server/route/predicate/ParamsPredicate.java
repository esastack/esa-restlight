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

import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.MappingUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ParamsPredicate
 * <p>
 * Implementation of RequestPredicate which try to judge whether current request is matching the {@link #expressions}
 */
public class ParamsPredicate implements RequestPredicate {

    /**
     * sub predicates
     */
    private final Expression[] expressions;

    ParamsPredicate(String... params) {
        this.expressions = parseExpressions(params).toArray(new Expression[0]);
    }

    public static String normaliseExpression(String expr) {
        return new Expression(expr).toString();
    }

    private static Collection<Expression> parseExpressions(String... params) {
        Set<Expression> expressions = new LinkedHashSet<>();
        if (params != null) {
            for (String param : params) {
                expressions.add(new Expression(param));
            }
        }
        return expressions;
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

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        return MappingUtils.isIntersect(this.expressions, ((ParamsPredicate) another).expressions);
    }

    private static class Expression extends AbstractNameValueExpression {

        private Expression(String expression) {
            super(expression);
        }

        @Override
        protected boolean isCaseSensitiveName() {
            return true;
        }

        @Override
        protected boolean matchName(HttpRequest request) {
            return request.getParam(name) != null;
        }

        @Override
        protected boolean matchValue(HttpRequest request) {
            return Objects.equals(this.value, request.getParam(this.name));
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

    @Override
    public String toString() {
        return "{params=" + expressions + '}';
    }
}
