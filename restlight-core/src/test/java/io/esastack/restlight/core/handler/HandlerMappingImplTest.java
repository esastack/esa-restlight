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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.handler.impl.HandlerMappingImpl;
import io.esastack.restlight.server.route.Mapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class HandlerMappingImplTest {

    @Test
    void testProps() {
        final Subject bean = new Subject();
        final RouteMethodInfo routeMethodInfo = mock(RouteMethodInfo.class);
        final Mapping mapping = mock(Mapping.class);
        final HandlerMapping parent = mock(HandlerMapping.class);

        final HandlerMapping handlerMapping = new HandlerMappingImpl(mapping,
                routeMethodInfo,
                bean,
                parent);
        assertEquals(mapping, handlerMapping.mapping());
        assertEquals(bean, handlerMapping.bean().get());
        assertEquals(routeMethodInfo, handlerMapping.methodInfo());
        assertEquals(parent, handlerMapping.parent().get());
    }

    private static class Subject {

        void method() {
        }

    }
}
