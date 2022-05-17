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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.AttributeMap;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.context.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpResponse;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class JaxrsResponseAdapterTest {

    @Test
    void testAroundWrite() throws Throwable {
        final JaxrsResponseAdapter adapter = new JaxrsResponseAdapter();

        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext rCtx = new RequestContextImpl(new AttributeMap(), mock(HttpRequest.class), response);

        final ResponseEntity entity = new ResponseEntityImpl(null, response, null);
        ResponseEntityResolverContext context = new ResponseEntityResolverContext() {
            @Override
            public ResponseEntity httpEntity() {
                return entity;
            }

            @Override
            public ResponseEntityChannel channel() {
                return null;
            }

            @Override
            public RequestContext requestContext() {
                return rCtx;
            }
        };
        ResolverExecutor<ResponseEntityResolverContext> executor = new ResolverExecutor() {
            @Override
            public ResponseEntityResolverContext context() {
                return context;
            }

            @Override
            public Object proceed() throws Exception {
                return null;
            }
        };
        adapter.aroundResolve0(executor);
        assertNull(entity.type());
        assertNull(entity.genericType());

        // plain GenericEntity
        response.entity(new GenericEntity<>("ABC", String.class));
        adapter.aroundResolve0(executor);
        assertEquals("ABC", response.entity());
        assertEquals(String.class, entity.type());
        assertEquals(String.class, entity.genericType());

        // Response
        response.entity(null);
        entity.type(null);
        entity.genericType(null);
        final Response rsp = Response.ok("DEF").build();
        response.entity(new GenericEntity<>(rsp, Response.class));
        adapter.aroundResolve0(executor);
        assertEquals("DEF", response.entity());
        assertEquals(String.class, entity.type());
        assertEquals(String.class, entity.genericType());
        assertEquals(200, response.status());

        // Response.ResponseBuilder
        response.entity(null);
        entity.type(null);
        entity.genericType(null);
        final Response.ResponseBuilder builder = Response.notModified().entity(100);
        response.entity(new GenericEntity<>(builder, Response.ResponseBuilder.class));
        adapter.aroundResolve0(executor);
        assertEquals(100, response.entity());
        assertEquals(Integer.class, entity.type());
        assertEquals(Integer.class, entity.genericType());
        assertEquals(304, response.status());
    }

}

