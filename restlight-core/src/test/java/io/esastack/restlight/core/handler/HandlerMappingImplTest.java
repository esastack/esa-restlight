/*
 * Copyright 2022 OPPO ESA Stack Project
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
import io.esastack.restlight.core.route.Mapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HandlerMappingImplTest {

    @Test
    void testConstruct() {
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

    @Test
    void testEqual() {
        final Subject bean = new Subject();
        final RouteMethodInfo routeMethodInfo = mock(RouteMethodInfo.class);
        final Mapping mapping = mock(Mapping.class);
        final HandlerMapping parent = mock(HandlerMapping.class);

        final HandlerMapping handlerMapping = new HandlerMappingImpl(mapping,
                routeMethodInfo,
                bean,
                parent);

        assertFalse(handlerMapping.equals(null));
        assertNotEquals(handlerMapping, new HandlerMappingImpl(mapping,
                routeMethodInfo,
                null,
                null));
        assertEquals(handlerMapping, new HandlerMappingImpl(mapping,
                routeMethodInfo,
                bean,
                parent));
    }

    @Test
    void testToString() {
        final Subject bean = new Subject();
        final RouteMethodInfo routeMethodInfo = mock(RouteMethodInfo.class);
        final Mapping mapping = mock(Mapping.class);
        final HandlerMapping parent = mock(HandlerMapping.class);

        final HandlerMapping handlerMapping = new HandlerMappingImpl(mapping,
                routeMethodInfo,
                bean,
                parent);

        String sb = "HandlerMappingImpl{" + "mapping=" + mapping +
                ", methodInfo=" + routeMethodInfo +
                ", parent=" + parent +
                ", bean=" + bean +
                '}';
        assertEquals(sb, handlerMapping.toString());
    }

    private static class Subject {

        void method() {
        }

    }
}
