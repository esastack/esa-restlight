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
package io.esastack.restlight.jaxrs.impl.ext;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverContext;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * This context is used to transmit custom properties among {@link WriterInterceptor}s.
 * !NOTE: The {@link WriterInterceptor} will be executed after {@link ContainerResponseFilter} and there is no need
 * to apply the setters to {@link Response}.
 */
public class WriterInterceptorContextImpl extends InterceptorContextImpl implements WriterInterceptorContext {

    private final ResolverExecutor<ResponseEntityResolverContext> underlying;
    private final MultivaluedMap<String, Object> headers;
    private final WriterInterceptor[] interceptors;
    private final int interceptorsSize;
    private OutputStream outputStream;
    private int index;

    public WriterInterceptorContextImpl(ResolverExecutor<ResponseEntityResolverContext> underlying,
                                        OutputStream outputStream,
                                        MultivaluedMap<String, Object> headers,
                                        WriterInterceptor[] interceptors) {
        super(underlying.context().requestContext(), underlying.context().responseEntity());
        Checks.checkNotNull(outputStream, "outputStream");
        Checks.checkNotNull(headers, "headers");
        this.underlying = underlying;
        this.outputStream = outputStream;
        this.headers = headers;
        this.interceptors = interceptors;
        this.interceptorsSize = interceptors != null ? interceptors.length : 0;
    }

    @Override
    public void proceed() throws IOException, WebApplicationException {
        if (index >= interceptorsSize) {
            try {
                JaxrsUtils.convertThenAddToHeaders(headers,
                        underlying.context().requestContext().response().headers());
                underlying.proceed();
                return;
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new WebApplicationException(ex);
            }
        }

        interceptors[index++].aroundWriteTo(this);
    }

    @Override
    public Object getEntity() {
        return underlying.context().requestContext().response().entity();
    }

    @Override
    public void setEntity(Object entity) {
        underlying.context().requestContext().response().entity(entity);
        if (entity != null) {
            Class<?> type = ClassUtils.getUserType(entity);
            Type genericType = ClassUtils.getRawType(type);
            setType(type);
            setGenericType(genericType);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        this.outputStream = os;
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }
}

