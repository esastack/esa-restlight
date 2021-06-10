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
package esa.restlight.starter.actuator.adapt;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.impl.RouteImpl;
import esa.restlight.server.schedule.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
class RestlightMappingDescriptionProviderTest {

    @SuppressWarnings("unchecked")
    @Test
    void testDescribeMappings() throws Throwable {
        final AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.register(RestlightMappingDescriptionProviderTest.class);
        appContext.refresh();

        final InvocableMethod handler = HandlerMethod.of(A.class.getDeclaredMethod("hello"),
                appContext.getBean(A.class));
        final List<Route> routes = new LinkedList<>();
        routes.add(new RouteImpl(Mapping.mapping("/abc"), null, Schedulers.io(), handler));

        final ReadOnlyRouteRegistry registry = mock(ReadOnlyRouteRegistry.class);
        when(registry.routes()).thenReturn(routes);

        final DeployContext<? extends RestlightOptions> context = mock(DeployContext.class);
        when(context.routeRegistry()).thenReturn(Optional.of(registry));

        final RestlightMappingDescriptionProvider provider = new RestlightMappingDescriptionProvider();
        provider.setDeployContext(context);

        final List<RestlightMappingDescriptionProvider.MappingResult> results =
                (List<RestlightMappingDescriptionProvider.MappingResult>)
                        provider.describeMappings(appContext);

        assertEquals(1, results.size());
        assertEquals("/abc", results.get(0).getPath());
        assertEquals("a", results.get(0).getBean());
        assertEquals(A.class.getDeclaredMethod("hello").toGenericString(), results.get(0).getMethod());

        results.get(0).setPath("/def");
        results.get(0).setBean("xyz");
        results.get(0).setMethod("hello");
        assertEquals("/def", results.get(0).getPath());
        assertEquals("xyz", results.get(0).getBean());
        assertEquals("hello", results.get(0).getMethod());

        appContext.close();
    }

    @Test
    void testGetMappingName() {
        assertEquals("Restlight-Mappings", new RestlightMappingDescriptionProvider().getMappingName());
    }

    @Bean
    public A a() {
        return new A();
    }

    private static class A {
        private void hello() {

        }
    }

}

