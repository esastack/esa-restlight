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
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class JaxrsExceptionMapperAdapterTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new JaxrsExceptionMapperAdapter<>(null));

        final Response response = mock(Response.class);
        final ExceptionMapper<RuntimeException> mapper = exception -> response;
        final ProxyComponent<ExceptionMapper<? extends Throwable>> proxied = new ProxyComponent<>(mapper, mapper);
        final JaxrsExceptionMapperAdapter<Throwable> adapter = new JaxrsExceptionMapperAdapter(proxied);
        assertSame(proxied, adapter.underlying());

        final AtomicReference<Response> entity = new AtomicReference<>();
        final HttpResponse rsp = mock(HttpResponse.class);
        final HttpRequest req = mock(HttpRequest.class);
        doAnswer((Answer<Object>) invocationOnMock -> {
            entity.set(invocationOnMock.getArgument(0));
            return null;
        }).when(rsp).entity(any());

        final RequestContext context = new RequestContextImpl(new AttributeMap(), req, rsp);
        final CompletableFuture<Void> future1 = adapter
                .handleException(context, new RuntimeException())
                .toCompletableFuture();
        assertTrue(future1.isDone());
        assertSame(response, entity.get());

        // mapping to null
        final ExceptionMapper<RuntimeException> mapper1 = exception -> null;
        final ProxyComponent<ExceptionMapper<?>> proxied1 = new ProxyComponent<>(mapper1, mapper1);
        final JaxrsExceptionMapperAdapter<Throwable> adapter1 = new JaxrsExceptionMapperAdapter(proxied1);
        adapter1.handleException(context, new RuntimeException());
        assertNotSame(response, entity.get());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), entity.get().getStatus());
    }

}

