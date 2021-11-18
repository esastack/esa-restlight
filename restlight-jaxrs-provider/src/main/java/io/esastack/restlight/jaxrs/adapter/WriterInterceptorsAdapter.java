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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.jaxrs.impl.ext.WriterInterceptorContextImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;

public class WriterInterceptorsAdapter implements ResponseEntityResolverAdviceAdapter {

    private final WriterInterceptor[] interceptors;
    private final boolean alsoApplyWhenNotRouted;

    public WriterInterceptorsAdapter(WriterInterceptor[] interceptors, boolean alsoApplyWhenNotRouted) {
        Checks.checkNotNull(interceptors, "interceptors");
        this.interceptors = interceptors;
        this.alsoApplyWhenNotRouted = alsoApplyWhenNotRouted;
    }

    @Override
    public void aroundWrite(ResponseEntityResolverContext context) throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        RuntimeDelegateUtils.addHeadersToMap(context.context().response().headers(), headers);
        try {
            new WriterInterceptorContextImpl(context, headers, interceptors).proceed();
        } finally {
            RuntimeDelegateUtils.addHeadersFromMap(context.context().response().headers(), headers, true);
        }
    }

    @Override
    public boolean supports(HandlerMethod handlerMethod) {
        return alsoApplyWhenNotRouted && handlerMethod != null;
    }
}

