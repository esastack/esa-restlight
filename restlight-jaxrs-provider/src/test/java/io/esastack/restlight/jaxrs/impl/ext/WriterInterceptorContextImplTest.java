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
package io.esastack.restlight.jaxrs.impl.ext;

import esa.commons.collection.AttributeMap;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WriterInterceptorContextImplTest {

    @Test
    void testBasic() throws Throwable {
        assertThrows(NullPointerException.class, () -> new WriterInterceptorContextImpl(null,
                mock(OutputStream.class), new MultivaluedHashMap<>(), new WriterInterceptor[0]));
        assertThrows(NullPointerException.class, () -> new WriterInterceptorContextImpl(
                mock(ResolverExecutor.class), null,
                new MultivaluedHashMap<>(), new WriterInterceptor[0]));
        assertThrows(NullPointerException.class, () -> new WriterInterceptorContextImpl(
                mock(ResolverExecutor.class), mock(OutputStream.class),
                null, new WriterInterceptor[0]));
        ResolverExecutor<ResponseEntityResolverContext> mockExecutor = mock(ResolverExecutor.class);
        when(mockExecutor.context()).thenReturn(mock(ResponseEntityResolverContext.class));
        assertDoesNotThrow(() -> new WriterInterceptorContextImpl(mockExecutor,
                mock(OutputStream.class), new MultivaluedHashMap<>(), null));

        final HttpRequest request = mock(HttpRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final RequestContext context0 = new RequestContextImpl(new AttributeMap(), request, response);
        final ResponseEntity entity = mock(ResponseEntity.class);
        final OutputStream outputStream = mock(OutputStream.class);
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

        final AtomicInteger count = new AtomicInteger();
        final ResponseEntityResolverContext underlying = new ResponseEntityResolverContext() {
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
                return context0;
            }

            @Override
            public DeployContext deployContext() {
                return null;
            }
        };
        final ResolverExecutor<ResponseEntityResolverContext> executor =
                new ResolverExecutor<ResponseEntityResolverContext>() {
                    @Override
                    public ResponseEntityResolverContext context() {
                        return underlying;
                    }

                    @Override
                    public Object proceed() throws Exception {
                        return count.incrementAndGet();
                    }
        };

        final WriterInterceptor interceptor1 = mock(WriterInterceptor.class);
        final WriterInterceptor interceptor2 = mock(WriterInterceptor.class);
        final WriterInterceptorContext context = new WriterInterceptorContextImpl(executor,
                outputStream, headers, new WriterInterceptor[]{interceptor1, interceptor2});

        // getEntity
        verify(response, never()).entity();
        context.getEntity();
        verify(response).entity();

        // setEntity
        final Object entity0 = new Object();
        verify(response, never()).entity(any());
        context.setEntity(entity0);
        verify(response).entity(entity0);
        verify(entity).type(Object.class);
        verify(entity).genericType(Object.class);

        // getOutputStream
        assertSame(outputStream, context.getOutputStream());

        // setOutputStream
        context.setOutputStream(mock(OutputStream.class));
        assertNotSame(outputStream, context.getOutputStream());

        // getHeaders
        assertSame(headers, context.getHeaders());

        // proceed
        doThrow(new IOException()).when(interceptor1).aroundWriteTo(any());
        assertThrows(IOException.class, context::proceed);

        clearInvocations(interceptor1, interceptor2);
        reset(interceptor1, interceptor2);
        final WriterInterceptorContext context1 = new WriterInterceptorContextImpl(executor,
                outputStream, headers, new WriterInterceptor[] {interceptor1, interceptor2});
        doAnswer(invocationOnMock -> {
            context1.proceed();
            return null;
        }).when(interceptor1).aroundWriteTo(any());
        doThrow(new RuntimeException()).when(interceptor2).aroundWriteTo(any());
        verify(interceptor1, never()).aroundWriteTo(any());
        verify(interceptor2, never()).aroundWriteTo(any());

        assertThrows(RuntimeException.class, context1::proceed);
        verify(interceptor1).aroundWriteTo(any());
        verify(interceptor2).aroundWriteTo(any());

        clearInvocations(interceptor1, interceptor2);
        reset(interceptor1, interceptor2);
        final WriterInterceptorContext context2 = new WriterInterceptorContextImpl(executor,
                outputStream, headers, new WriterInterceptor[] {interceptor1, interceptor2});
        assertEquals(0, count.intValue());

        doAnswer(invocationOnMock -> {
            context2.proceed();
            return null;
        }).when(interceptor1).aroundWriteTo(any());
        doAnswer(invocationOnMock -> {
            context2.proceed();
            return null;
        }).when(interceptor2).aroundWriteTo(any());
        context2.proceed();
        assertEquals(1, count.intValue());
    }

}

