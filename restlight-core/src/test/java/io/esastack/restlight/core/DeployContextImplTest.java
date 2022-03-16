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
package io.esastack.restlight.core;

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeployContextImplTest {

    @Test
    void testAll() {
        final RestlightOptions ops = RestlightOptionsConfigure.defaultOpts();
        final String name = "foo";
        final DeployContextImpl ctx = new DeployContextImpl(name, ops);
        assertEquals(name, ctx.name());
        assertEquals(ops, ctx.options());
        assertFalse(ctx.routeRegistry().isPresent());

        final RouteRegistry registry = new RouteRegistry() {

            @Override
            public List<Route> routes() {
                return null;
            }

            @Override
            public void register(Route route) {
            }

            @Override
            public void deRegister(Route route) {
            }
        };

        ctx.setRegistry(registry);
        assertTrue(ctx.routeRegistry().isPresent());
        assertEquals(registry, ctx.routeRegistry().get());

        ctx.attrs().attr(AttributeKey.stringKey("foo")).set("bar");
        ctx.attrs().attr(AttributeKey.stringKey("baz")).set("qux");
        assertEquals("bar", ctx.attrs().attr(AttributeKey.stringKey("foo")).get());
        assertEquals("qux", ctx.attrs().attr(AttributeKey.stringKey("baz")).get());
        assertEquals("bar", ctx.attrs().attr(AttributeKey.stringKey("foo")).getAndRemove());
        assertEquals("qux", ctx.attrs().attr(AttributeKey.stringKey("baz")).getAndRemove());
        assertNull(ctx.attrs().attr(AttributeKey.stringKey("foo")).get());
        assertNull(ctx.attrs().attr(AttributeKey.stringKey("baz")).get());
    }

}
