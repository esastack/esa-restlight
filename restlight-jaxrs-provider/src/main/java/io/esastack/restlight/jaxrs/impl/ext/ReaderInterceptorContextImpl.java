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

import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverContext;
import io.esastack.restlight.jaxrs.impl.core.ModifiableMultivaluedMap;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

import java.io.IOException;
import java.io.InputStream;

public class ReaderInterceptorContextImpl extends InterceptorContextImpl implements ReaderInterceptorContext {

    private final ResolverExecutor<RequestEntityResolverContext> underlying;

    private final ReaderInterceptor[] interceptors;
    private final int interceptorsSize;
    private int index;

    public ReaderInterceptorContextImpl(ResolverExecutor<RequestEntityResolverContext> underlying,
                                        ReaderInterceptor[] interceptors) {
        super(underlying.context().requestContext(), underlying.context().requestEntity());
        this.underlying = underlying;
        this.interceptors = interceptors;
        this.interceptorsSize = interceptors != null ? interceptors.length : 0;
    }

    @Override
    public Object proceed() throws IOException, WebApplicationException {
        if (index >= interceptorsSize) {
            try {
                return underlying.proceed();
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new WebApplicationException(ex);
            }
        }

        return interceptors[index++].aroundReadFrom(this);
    }

    @Override
    public InputStream getInputStream() {
        return underlying.context().requestContext().request().inputStream();
    }

    @Override
    public void setInputStream(InputStream is) {
        underlying.context().requestEntity().inputStream(is);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return new ModifiableMultivaluedMap(underlying.context().requestContext().request().headers());
    }
}

