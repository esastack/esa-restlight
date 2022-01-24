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

import java.util.Objects;

abstract class AbstractNameValueExpression {

    protected final String name;
    protected final String value;
    private final boolean isNegated;

    AbstractNameValueExpression(String expression) {
        int separator = expression.indexOf('=');
        if (separator == -1) {
            this.isNegated = expression.startsWith("!");
            this.name = isNegated ? expression.substring(1) : expression;
            this.value = null;
        } else {
            this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == '!');
            this.name = isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator);
            this.value = parseValue(expression.substring(separator + 1));
        }
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isNegated() {
        return this.isNegated;
    }

    /**
     * is case sensitive to name
     *
     * @return is sensitive
     */
    protected abstract boolean isCaseSensitiveName();

    /**
     * parse the value from the expression
     *
     * @param valueExpression expression
     * @return passed value
     */
    protected String parseValue(String valueExpression) {
        return valueExpression;
    }

    public final boolean match(HttpRequest request) {
        boolean isMatch;
        if (this.value != null) {
            isMatch = matchValue(request);
        } else {
            isMatch = matchName(request);
        }
        return isNegated != isMatch;
    }

    /**
     * is name matched
     *
     * @param request request
     * @return is matched
     */
    protected abstract boolean matchName(HttpRequest request);

    /**
     * is value matched
     *
     * @param request request
     * @return is matched
     */
    protected abstract boolean matchValue(HttpRequest request);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractNameValueExpression) {
            AbstractNameValueExpression other = (AbstractNameValueExpression) obj;
            String thisName = isCaseSensitiveName() ? this.name : this.name.toLowerCase();
            String otherName = isCaseSensitiveName() ? other.name : other.name.toLowerCase();
            return ((thisName.equalsIgnoreCase(otherName)) &&
                    (Objects.equals(this.value, other.value)) &&
                    this.isNegated == other.isNegated);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = isCaseSensitiveName() ? name.hashCode() : name.toLowerCase().hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (isNegated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (value != null) {
            builder.append(name);
            if (isNegated) {
                builder.append('!');
            }
            builder.append('=');
            builder.append(value);
        } else {
            if (isNegated) {
                builder.append('!');
            }
            builder.append(name);
        }
        return builder.toString();
    }
}
