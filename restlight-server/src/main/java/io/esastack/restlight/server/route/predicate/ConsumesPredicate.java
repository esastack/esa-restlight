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

import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.server.util.MappingUtils;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConsumesPredicate implements RequestPredicate {

    private final Expression[] expressions;

    private ConsumesPredicate(Set<Expression> expressions) {
        List<Expression> temp = new ArrayList<>(expressions);
        Collections.sort(temp);
        this.expressions = temp.toArray(new Expression[0]);
    }

    public static ConsumesPredicate parseFrom(String[] consumes) {
        return parseFrom(consumes, null);
    }

    public static ConsumesPredicate parseFrom(String[] consumes, String[] headers) {

        Set<ConsumesPredicate.Expression> consumeExpressions =
                MappingUtils.parseConsumeExpressions(consumes, headers);

        if (!consumeExpressions.isEmpty()) {
            return new ConsumesPredicate(consumeExpressions);
        }
        return null;
    }

    @Override
    public boolean test(HttpRequest request) {
        for (Expression expression : expressions) {
            if (expression.match(request)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        return MappingUtils.isIntersect(this.expressions, ((ConsumesPredicate) another).expressions);
    }

    @Override
    public String toString() {
        return "{Consumes=" + Arrays.toString(expressions) + '}';
    }

    public static final class Expression extends AbstractMediaTypeExpression {
        public Expression(MediaType mediaType, boolean isNegated) {
            super(mediaType, isNegated);
        }

        public Expression(String expression) {
            super(expression);
        }

        @Override
        protected boolean matchMediaType(HttpRequest request) {
            String contentTypeString = request.getHeader(HttpHeaderNames.CONTENT_TYPE);
            MediaType contentType = StringUtils.isEmpty(contentTypeString) ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaTypeUtil.parseMediaType(contentTypeString);
            return getMediaType().includes(contentType);
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
