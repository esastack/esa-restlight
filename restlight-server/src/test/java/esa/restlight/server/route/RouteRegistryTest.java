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
package esa.restlight.server.route;

import esa.httpserver.core.AsyncRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class RouteRegistryTest {

    @Test
    void testDefault() {
        final RouteRegistry registry = new RouteRegistry() {
            @Override
            public void registerRoute(Route route) {
            }

            @Override
            public Route route(AsyncRequest request) {
                return null;
            }

            @Override
            public List<Route> routes() {
                return null;
            }
        };
        assertSame(registry, registry.toReadOnly());
    }

}
