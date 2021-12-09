/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.method.HandlerMethod;

import java.util.Objects;

public class HandlerMethodInfo {

    private final boolean locator;
    private final HandlerMethod handlerMethod;
    private final HttpStatus customResponse;

    private String strVal;

    public HandlerMethodInfo(HandlerMethod handlerMethod,
                             boolean locator,
                             HttpStatus customResponse) {
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        this.handlerMethod = handlerMethod;
        this.locator = locator;
        this.customResponse = customResponse;
    }

    public boolean isLocator() {
        return locator;
    }

    public HandlerMethod handlerMethod() {
        return handlerMethod;
    }

    public HttpStatus customResponse() {
        return customResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerMethodInfo that = (HandlerMethodInfo) o;
        return locator == that.locator && Objects.equals(handlerMethod, that.handlerMethod)
                && Objects.equals(customResponse, that.customResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locator, handlerMethod, customResponse);
    }

    @Override
    public String toString() {
        if (strVal == null) {
            final StringBuilder sb = new StringBuilder("HandlerMethodInfo{");
            sb.append("locator=").append(locator);
            sb.append(", handlerMethod=").append(handlerMethod);
            sb.append(", customResponse=").append(customResponse);
            sb.append('}');
            strVal = sb.toString();
        }
        return strVal;
    }
}

