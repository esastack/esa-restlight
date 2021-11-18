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
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;

import java.io.InputStream;
import java.util.List;

public class RequestEntityResolverContextImpl implements RequestEntityResolverContext {

    private final Param param;
    private final RequestContext context;
    private final RequestEntity entity;
    private final List<RequestEntityResolver> resolvers;
    private final List<RequestEntityResolverAdvice> advices;
    private final int advicesSize;
    private int index;

    public RequestEntityResolverContextImpl(Param param,
                                            RequestContext context,
                                            RequestEntity entity,
                                            List<RequestEntityResolver> resolvers,
                                            List<RequestEntityResolverAdvice> advices) {
        Checks.checkNotNull(param, "param");
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(resolvers, "resolvers");
        this.param = param;
        this.context = context;
        this.entity = entity;
        this.resolvers = resolvers;
        this.advices = advices;
        this.advicesSize = (advices == null || advices.isEmpty()) ? 0 : advices.size();
    }

    @Override
    public RequestContext context() {
        return context;
    }

    @Override
    public RequestEntity entityInfo() {
        return entity;
    }

    @Override
    public MediaType mediaType() {
        return entity.mediaType();
    }

    @Override
    public void mediaType(MediaType mediaType) {
        entity.mediaType(mediaType);
    }

    @Override
    public void inputStream(InputStream ins) {
        entity.inputStream(ins);
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advicesSize) {
            HandledValue<Object> handled;
            for (RequestEntityResolver resolver : resolvers) {
                handled = resolver.readFrom(param, entity, context);
                if (handled.isSuccess()) {
                    return handled.value();
                }
            }
            return null;
        }

        return advices.get(index++).aroundRead(this);
    }

    @Override
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }

    @Override
    public Object removeAttribute(String name) {
        return context.removeAttribute(name);
    }

    @Override
    public String[] attributeNames() {
        return context.attributeNames();
    }
}

