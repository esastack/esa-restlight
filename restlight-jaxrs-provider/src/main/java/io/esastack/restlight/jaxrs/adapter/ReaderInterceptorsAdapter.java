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
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverContext;
import io.esastack.restlight.jaxrs.configure.RouteTracking;
import io.esastack.restlight.jaxrs.impl.ext.ReaderInterceptorContextImpl;
import jakarta.ws.rs.ext.ReaderInterceptor;

public class ReaderInterceptorsAdapter implements RequestEntityResolverAdviceAdapter {

    private final ReaderInterceptor[] interceptors;
    private final boolean onlyActiveWhenMatched;

    public ReaderInterceptorsAdapter(ReaderInterceptor[] interceptors, boolean onlyActiveWhenMatched) {
        Checks.checkNotNull(interceptors, "interceptors");
        this.interceptors = interceptors;
        this.onlyActiveWhenMatched = onlyActiveWhenMatched;
    }

    @Override
    public Object aroundRead(RequestEntityResolverContext context) throws Exception {
        boolean hasMatched = RouteTracking.isMethodMatched(context.context());
        if ((onlyActiveWhenMatched && hasMatched) || (!onlyActiveWhenMatched && !hasMatched)) {
            return new ReaderInterceptorContextImpl(context, interceptors).proceed();
        } else {
            return context.proceed();
        }
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }
}

