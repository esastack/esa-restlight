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

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.util.MappingUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProducesPredicate implements RequestPredicate {

    public static final String COMPATIBLE_MEDIA_TYPES = "$cpt.mts";

    private static final FastThreadLocal<List<MediaType>> ACCEPTABLE_MEDIA_TYPES
            = new FastThreadLocal<>();
    private static final List<MediaType> ALL =
            Collections.singletonList(MediaType.ALL);

    private final Expression[] expressions;
    private final List<MediaType> producibleMediaTypes;

    private ProducesPredicate(Set<Expression> expressions) {
        List<Expression> temp = new ArrayList<>(expressions);
        Collections.sort(temp);
        this.expressions = temp.toArray(new Expression[0]);
        this.producibleMediaTypes = Collections.unmodifiableList(getProducibleMediaTypes());
    }

    public static ProducesPredicate parseFrom(String[] produces) {
        return parseFrom(produces, null);
    }

    public static ProducesPredicate parseFrom(String[] produces, String[] headers) {
        Set<ProducesPredicate.Expression> produceExpressions =
                MappingUtils.parseProduceExpressions(produces, headers);
        if (!produceExpressions.isEmpty()) {
            return new ProducesPredicate(produceExpressions);
        }
        return null;
    }

    @Override
    public boolean test(AsyncRequest request) {
        try {
            for (Expression expression : expressions) {
                if (expression.match(request)) {
                    List<MediaType> compatibleMediaTypes = getCompatibleMediaType();
                    if (!compatibleMediaTypes.isEmpty()) {
                        request.setAttribute(COMPATIBLE_MEDIA_TYPES, compatibleMediaTypes);
                    }
                    return true;
                }
            }
        } finally {
            ACCEPTABLE_MEDIA_TYPES.remove();
        }
        return false;
    }

    private List<MediaType> getCompatibleMediaType() {
        List<MediaType> compatibleMediaTypes = new LinkedList<>();
        MediaType mostSpecificMediaType;
        final List<MediaType> accepts = ACCEPTABLE_MEDIA_TYPES.get();
        for (MediaType acceptable : accepts) {
            for (MediaType producible : this.producibleMediaTypes) {
                if (acceptable.isCompatibleWith(producible)) {
                    mostSpecificMediaType = this.getMostSpecificMediaType(acceptable, producible);
                    //MediaType needs to be ignored
                    if (!MediaType.ALL.equals(mostSpecificMediaType)) {
                        compatibleMediaTypes.add(mostSpecificMediaType);
                    }
                }
            }
        }
        MediaType.sortBySpecificityAndQuality(compatibleMediaTypes);
        return compatibleMediaTypes;
    }

    private static List<MediaType> getAcceptedMediaTypes(AsyncRequest request) {
        List<MediaType> mediaTypes = MediaType.valuesOf(request.getHeader(HttpHeaderNames.ACCEPT));
        return (mediaTypes.isEmpty())
                ? ALL
                : mediaTypes;
    }

    /**
     * Return the more specific of the acceptable and the producible media types with the q-value of the former.
     */
    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        produceType = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceType) < 0 ? acceptType : produceType);
    }

    /**
     * Return the contained producible media types excluding negated expressions.
     */
    private List<MediaType> getProducibleMediaTypes() {
        Set<MediaType> result = new LinkedHashSet<>();
        for (Expression expression : this.expressions) {
            if (!expression.isNegated()) {
                result.add(expression.getMediaType());
            }
        }
        return result.isEmpty() ? ALL : new ArrayList<>(result);
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        return MappingUtils.isIntersect(this.expressions, ((ProducesPredicate) another).expressions);
    }

    @Override
    public String toString() {
        return "{Produces=" + Arrays.toString(expressions) + '}';
    }

    /**
     * Parses and matches a single media type expression to a request's 'Accept' header.
     */
    public static class Expression extends AbstractMediaTypeExpression {

        public Expression(MediaType mediaType, boolean negated) {
            super(mediaType, negated);
        }

        public Expression(String expression) {
            super(expression);
        }

        @Override
        protected boolean matchMediaType(AsyncRequest request) {
            List<MediaType> mediaTypes = ACCEPTABLE_MEDIA_TYPES.getIfExists();
            if (mediaTypes == null) {
                ACCEPTABLE_MEDIA_TYPES.set(mediaTypes = getAcceptedMediaTypes(request));
            }
            if (mediaTypes.isEmpty()) {
                return false;
            }
            for (MediaType acceptedMediaType : mediaTypes) {
                if (getMediaType().isCompatibleWith(acceptedMediaType)) {
                    return true;
                }
            }
            return false;
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
