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
package io.esastack.restlight.core.route.impl;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.SchedulingOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static io.esastack.restlight.core.route.Mapping.get;
import static io.esastack.restlight.core.route.Route.route;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoutableRegistryTest {

    @Test
    void testAll() {
        final AbstractRouteRegistry underlying = new SimpleRouteRegistry();
        final DeployContext context = mock(DeployContext.class);
        final RestlightOptions options = mock(RestlightOptions.class);
        when(context.options()).thenReturn(options);
        Map<String, Scheduler> schedulers = new HashMap<>();
        final Scheduler bizScheduler = mock(Scheduler.class);
        final Scheduler defaultScheduler = mock(Scheduler.class);
        final String defaultKey = "defaultKey";
        schedulers.put(Schedulers.BIZ, bizScheduler);
        schedulers.put(defaultKey, defaultScheduler);
        when(context.schedulers()).thenReturn(schedulers);
        SchedulingOptions scheduling = mock(SchedulingOptions.class);
        when(options.getScheduling()).thenReturn(scheduling);
        RoutableRegistry registry = new RoutableRegistry(context, underlying);
        when(context.options().getScheduling().getDefaultScheduler()).thenReturn("aaa");
        assertThrows(IllegalStateException.class, () -> registry.register(route(get("/foo"))));
        when(context.options().getScheduling().getDefaultScheduler()).thenReturn("");
        registry.register(route(get("/foo")));
        HttpRequest request = MockHttpRequest.aMockRequest().withUri("/foo").build();
        RequestContext requestContext =
                new RequestContextImpl(request, mock(HttpResponse.class));
        Route route = registry.route(requestContext);
        assertEquals(bizScheduler, route.scheduler());

        when(context.options().getScheduling().getDefaultScheduler()).thenReturn(defaultKey);
        registry.register(route(get("/bar")));
        request = MockHttpRequest.aMockRequest().withUri("/bar").build();
        requestContext =
                new RequestContextImpl(request, mock(HttpResponse.class));
        route = registry.route(requestContext);
        assertEquals(defaultScheduler, route.scheduler());

        registry.register(route(get("/aaa")).scheduler(Schedulers.BIZ, mock(Executor.class)));
        request = MockHttpRequest.aMockRequest().withUri("/aaa").build();
        requestContext =
                new RequestContextImpl(request, mock(HttpResponse.class));
        route = registry.route(requestContext);
        assertEquals(bizScheduler, route.scheduler());
        assertEquals(3, registry.routes().size());

        for (Route routeTem : registry.routes()) {
            registry.deRegister(routeTem);
        }

        assertEquals(0, registry.routes().size());
    }

}
