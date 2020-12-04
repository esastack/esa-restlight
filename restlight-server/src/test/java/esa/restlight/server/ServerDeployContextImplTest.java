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
package esa.restlight.server;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.config.ServerOptionsConfigure;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerDeployContextImplTest {

    @Test
    void testAll() {
        final ServerOptions ops = ServerOptionsConfigure.defaultOpts();
        final String name = "foo";
        final ServerDeployContextImpl<ServerOptions> ctx = new ServerDeployContextImpl<>(name, ops);
        assertEquals(name, ctx.name());
        assertEquals(ops, ctx.options());
        assertFalse(ctx.routeRegistry().isPresent());

        final ReadOnlyRouteRegistry registry = new ReadOnlyRouteRegistry() {
            @Override
            public Route route(AsyncRequest request) {
                return null;
            }

            @Override
            public List<Route> routes() {
                return null;
            }
        };

        ctx.setRegistry(registry);
        assertTrue(ctx.routeRegistry().isPresent());
        assertEquals(registry, ctx.routeRegistry().get());

        ctx.attribute("foo", "bar");
        ctx.attribute("baz", "qux");
        assertEquals("bar", ctx.attribute("foo"));
        assertEquals("qux", ctx.uncheckedAttribute("baz"));
        assertEquals("bar", ctx.removeAttribute("foo"));
        assertEquals("qux", ctx.removeUncheckedAttribute("baz"));
        assertNull(ctx.attribute("foo"));
        assertNull(ctx.attribute("bar"));
    }

}
