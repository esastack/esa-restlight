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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.jaxrs.configure.RouteTracking;

import java.util.Optional;

public class RouteTrackingFilterFactory implements RouteFilterFactory {

    @Override
    public Optional<RouteFilter> create(HandlerMethod method) {
        return Optional.of(RouteTracking.singleton());
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }
}

