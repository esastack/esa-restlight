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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.adapter.JaxrsResponseAdapter;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpResponse;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsResponseAdapterFactoryTest {

    @Test
    void testAll() {
        final JaxrsResponseAdapterFactory factory = new JaxrsResponseAdapterFactory();
        assertEquals(Ordered.HIGHEST_PRECEDENCE, factory.getOrder());
        final ResponseEntityResolverAdvice advice1 = factory.createResolverAdvice(null);
        final ResponseEntityResolverAdvice advice2 = factory.createResolverAdvice(null);
        assertSame(advice1, advice2);
        assertEquals(JaxrsResponseAdapter.class, advice1.getClass());

        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final ResponseEntity entity = mock(ResponseEntity.class);
        when(entity.response()).thenReturn(response);
        assertFalse(factory.supports(entity));

        response.entity(new Object());
        assertFalse(factory.supports(entity));

        response.entity(Response.ok());
        assertTrue(factory.supports(entity));

        response.entity(Response.ok().build());
        assertTrue(factory.supports(entity));

        response.entity(new GenericEntity<>(new Object(), Object.class));
        assertTrue(factory.supports(entity));
    }

}

