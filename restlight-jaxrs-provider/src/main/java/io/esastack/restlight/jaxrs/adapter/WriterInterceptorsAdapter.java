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
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.jaxrs.impl.ext.WriterInterceptorContextImpl;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;

public class WriterInterceptorsAdapter implements ResponseEntityResolverAdviceAdapter {

    private final WriterInterceptor[] interceptors;
    private final ProvidersPredicate predicate;

    public WriterInterceptorsAdapter(WriterInterceptor[] interceptors, ProvidersPredicate predicate) {
        Checks.checkNotNull(interceptors, "interceptors");
        Checks.checkNotNull(predicate, "predicate");
        this.interceptors = interceptors;
        this.predicate = predicate;
    }

    @Override
    public void aroundWrite(ResponseEntityResolverContext context) throws Exception {
        if (predicate.test(context.context())) {
            MultivaluedMap<String, Object> headers = JaxrsUtils.convertToMap(context.context().response().headers());
            try {
                new WriterInterceptorContextImpl(context,
                        ResponseEntityStreamUtils.getUnClosableOutputStream(context.context()),
                        headers, interceptors).proceed();
            } catch (Throwable th) {
                JaxrsUtils.convertThenAddToHeaders(headers, context.context().response().headers());
                throw th;
            }
        } else {
            context.proceed();
        }
    }

}

