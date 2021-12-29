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
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.jaxrs.configure.RouteTracking;
import io.esastack.restlight.jaxrs.impl.ext.WriterInterceptorContextImpl;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;

public class WriterInterceptorsAdapter implements ResponseEntityResolverAdviceAdapter {

    private final WriterInterceptor[] interceptors;
    private final boolean onlyActiveWhenMatched;

    public WriterInterceptorsAdapter(WriterInterceptor[] interceptors, boolean onlyActiveWhenMatched) {
        Checks.checkNotNull(interceptors, "interceptors");
        this.interceptors = interceptors;
        this.onlyActiveWhenMatched = onlyActiveWhenMatched;
    }

    @Override
    public void aroundWrite(ResponseEntityResolverContext context) throws Exception {
        boolean hasMatched = RouteTracking.isMethodMatched(context.context());
        if (onlyActiveWhenMatched && hasMatched || !onlyActiveWhenMatched && !hasMatched) {
            MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
            RuntimeDelegateUtils.addHeadersToMap(context.context().response().headers(), headers);
            try {
                new WriterInterceptorContextImpl(context,
                        ResponseEntityStreamClose.getNonClosableOutputStream(context.context()),
                        headers, interceptors).proceed();
            } catch (Throwable th) {
                RuntimeDelegateUtils.addHeadersFromMap(context.context().response().headers(), headers, true);
                throw th;
            }
        } else {
            context.proceed();
        }
    }

    @Override
    public boolean supports(ResponseEntity entity) {
        return true;
    }
}

