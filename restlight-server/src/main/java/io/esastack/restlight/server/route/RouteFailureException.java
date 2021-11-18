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
package io.esastack.restlight.server.route;

public class RouteFailureException extends RuntimeException {

    private final RouteFailure failureType;

    public RouteFailureException(RouteFailure failureType) {
        super();
        this.failureType = failureType;
    }

    public RouteFailure getFailureType() {
        return failureType;
    }

    public enum RouteFailure {

        /**
         * request's pattern mismatch
         */
        PATTERN_MISMATCH,

        /**
         * request' method mismatch
         */
        METHOD_MISMATCH,

        /**
         * request's param mismatch
         */
        PARAM_MISMATCH,

        /**
         * request's header mismatch
         */
        HEADER_MISMATCH,

        /**
         * request's consumes mismatch
         */
        CONSUMES_MISMATCH,

        /**
         * request's produces mismatch
         */
        PRODUCES_MISMATCH

    }
}

