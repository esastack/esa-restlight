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
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverContext;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReaderInterceptorContextImplTest {

    @Test
    void testBasic() throws Throwable {
        assertThrows(NullPointerException.class, () -> new ReaderInterceptorContextImpl(null,
                new ReaderInterceptor[0]));
        ResolverExecutor mockExecutor = mock(ResolverExecutor.class);
        RequestEntityResolverContext resolverContext = mock(RequestEntityResolverContext.class);
        when(resolverContext.requestContext()).thenReturn(mock(RequestContext.class));
        when(resolverContext.httpEntity()).thenReturn(mock(RequestEntity.class));
        when(mockExecutor.context()).thenReturn(resolverContext);
        assertDoesNotThrow(() -> new ReaderInterceptorContextImpl(mockExecutor, null));

        final HttpRequest request = mock(HttpRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final RequestContext context0 = new RequestContextImpl(new AttributeMap(), request, response);
        final RequestEntity entity = mock(RequestEntity.class);

        final AtomicInteger count = new AtomicInteger();
        final RequestEntityResolverContext underlying = new RequestEntityResolverContext() {

            @Override
            public RequestContext requestContext() {
                return context0;
            }

            @Override
            public RequestEntity httpEntity() {
                return entity;
            }
        };
        final ResolverExecutor<RequestEntityResolverContext> executor =
                new ResolverExecutor<RequestEntityResolverContext>() {
                    @Override
                    public RequestEntityResolverContext context() {
                        return underlying;
                    }

                    @Override
                    public Object proceed() throws Exception {
                        count.incrementAndGet();
                        return count;
                    }
                };

        final ReaderInterceptor interceptor1 = mock(ReaderInterceptor.class);
        final ReaderInterceptor interceptor2 = mock(ReaderInterceptor.class);
        final ReaderInterceptorContext context = new ReaderInterceptorContextImpl(executor,
                new ReaderInterceptor[]{interceptor1, interceptor2});

        verify(request, never()).inputStream();
        context.getInputStream();
        verify(request).inputStream();

        verify(entity, never()).inputStream(null);
        context.setInputStream(null);
        verify(entity).inputStream(null);

        when(request.headers()).thenReturn(new Http1HeadersImpl());
        verify(request, never()).headers();
        context.getHeaders();
        verify(request).headers();

        // proceed
        when(interceptor1.aroundReadFrom(any())).thenThrow(new IOException());
        assertThrows(IOException.class, context::proceed);

        clearInvocations(interceptor1, interceptor2);
        reset(interceptor1, interceptor2);
        final ReaderInterceptorContext context1 = new ReaderInterceptorContextImpl(executor,
                new ReaderInterceptor[] {interceptor1, interceptor2});
        when(interceptor1.aroundReadFrom(any())).thenAnswer(invocationOnMock -> context1.proceed());
        when(interceptor2.aroundReadFrom(any())).thenThrow(new RuntimeException());
        verify(interceptor1, never()).aroundReadFrom(any());
        verify(interceptor2, never()).aroundReadFrom(any());

        assertThrows(RuntimeException.class, context1::proceed);
        verify(interceptor1).aroundReadFrom(any());
        verify(interceptor2).aroundReadFrom(any());

        clearInvocations(interceptor1, interceptor2);
        reset(interceptor1, interceptor2);
        final ReaderInterceptorContext context2 = new ReaderInterceptorContextImpl(executor,
                new ReaderInterceptor[] {interceptor1, interceptor2});
        assertEquals(0, count.intValue());
        when(interceptor1.aroundReadFrom(any())).thenAnswer(invocationOnMock -> context2.proceed());
        when(interceptor2.aroundReadFrom(any())).thenAnswer(invocationOnMock -> context2.proceed());
        context2.proceed();
        assertEquals(1, count.intValue());
    }

}

