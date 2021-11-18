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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import io.esastack.httpserver.impl.AttributesProxy;
import io.esastack.restlight.core.context.RequestContext;

import java.io.OutputStream;
import java.util.List;

public class ResponseEntityResolverContextImpl extends AttributesProxy implements ResponseEntityResolverContext {

    private final RequestContext context;
    private final ResponseEntity entity;
    private final List<ResponseEntityResolver> resolvers;
    private final ResponseEntityResolverAdvice[] advices;
    private int index;

    public ResponseEntityResolverContextImpl(RequestContext context,
                                             ResponseEntity entity,
                                             List<ResponseEntityResolver> resolvers,
                                             ResponseEntityResolverAdvice[] advices) {
        super(context);
        Checks.checkNotNull(entity, "value");
        Checks.checkNotNull(resolvers, "resolvers");
        this.context = context;
        this.entity = entity;
        this.resolvers = resolvers;
        this.advices = advices;
    }

    @Override
    public RequestContext context() {
        return context;
    }

    @Override
    public ResponseEntity entityInfo() {
        return entity;
    }

    @Override
    public void outputStream(OutputStream os) {
        context.response().outputStream(os);
    }

    @Override
    public Object entity() {
        return entity.entity();
    }

    @Override
    public void entity(Object entity) {
        this.entity.entity(entity);
    }

    @Override
    public void proceed() throws Exception {
        if (advices == null || index >= advices.length) {
            HandledValue<Void> handled;
            for (ResponseEntityResolver resolver : resolvers) {
                handled = resolver.writeTo(entity, context);
                if (handled.isSuccess()) {
                    return;
                }
            }
            return;
        }

        advices[index++].aroundWrite(this);
    }
}

