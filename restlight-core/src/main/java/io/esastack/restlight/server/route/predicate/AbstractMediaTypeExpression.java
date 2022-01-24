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

import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.server.core.HttpRequest;

public abstract class AbstractMediaTypeExpression implements Comparable<AbstractMediaTypeExpression> {

    private static final String NEGATE_SYMBOL = "!";

    private final MediaType mediaType;
    private final boolean isNegated;

    AbstractMediaTypeExpression(MediaType mediaType, boolean isNegated) {
        this.mediaType = mediaType;
        this.isNegated = isNegated;
    }

    AbstractMediaTypeExpression(String expression) {
        if (expression.startsWith(NEGATE_SYMBOL)) {
            this.isNegated = true;
            expression = expression.substring(1);
        } else {
            this.isNegated = false;
        }
        this.mediaType = MediaTypeUtil.parseMediaType(expression);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public final boolean match(HttpRequest request) {
        try {
            boolean match = matchMediaType(request);
            return (!this.isNegated == match);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int compareTo(AbstractMediaTypeExpression other) {
        return MediaTypeUtil.SPECIFICITY_COMPARATOR.compare(this.getMediaType(),
                other.getMediaType());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractMediaTypeExpression otherExpr = (AbstractMediaTypeExpression) other;
        return (this.mediaType.equals(otherExpr.mediaType) && this.isNegated == otherExpr.isNegated);
    }

    @Override
    public int hashCode() {
        return this.mediaType.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(mediaType.value().length() + 1);
        if (this.isNegated) {
            builder.append('!');
        }
        builder.append(this.mediaType.value());
        return builder.toString();
    }

    /**
     * Match mediaType
     *
     * @param request request
     * @return boolean
     */
    protected abstract boolean matchMediaType(HttpRequest request);
}
