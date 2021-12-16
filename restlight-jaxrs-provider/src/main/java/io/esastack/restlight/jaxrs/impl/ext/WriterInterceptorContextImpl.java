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
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This context is used to transmit custom properties among {@link WriterInterceptor}s.
 * !NOTE: The {@link WriterInterceptor} will be executed after {@link ContainerResponseFilter} and there is no need
 * to apply the setters to {@link Response}.
 */
public class WriterInterceptorContextImpl extends InterceptorContextImpl implements WriterInterceptorContext {

    private final ResponseEntityResolverContext underlying;
    private final MultivaluedMap<String, Object> headers;
    private final WriterInterceptor[] interceptors;
    private final int interceptorsSize;
    private int index;

    public WriterInterceptorContextImpl(ResponseEntityResolverContext underlying,
                                        MultivaluedMap<String, Object> headers,
                                        WriterInterceptor[] interceptors) {
        super(underlying);
        Checks.checkNotNull(headers, "headers");
        this.underlying = underlying;
        this.headers = headers;
        this.interceptors = interceptors;
        this.interceptorsSize = interceptors != null ? interceptors.length : 0;
    }

    @Override
    public void proceed() throws IOException, WebApplicationException {
        if (index >= interceptorsSize) {
            try {
                underlying.proceed();
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
        return underlying.entity();
    }

    @Override
    public void setEntity(Object entity) {
        underlying.entity(entity);
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO:
        return null;
        //return underlying.context().response().outputStream();
    }

    @Override
    public void setOutputStream(OutputStream os) {
        underlying.outputStream(os);
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }
}

