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

import esa.commons.ClassUtils;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverContext;
import io.esastack.restlight.core.util.Ordered;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

public class JaxrsResponseAdapter implements ResponseEntityResolverAdviceAdapter {

    @Override
    public void aroundResolve0(ResolverExecutor<ResponseEntityResolverContext> executor) throws Exception {
        ResponseEntityResolverContext context = executor.context();
        Object entity = context.requestContext().response().entity();
        if (entity == null) {
            executor.proceed();
            return;
        }
        if (entity instanceof GenericEntity) {
            GenericEntity<?> gEntity = (GenericEntity<?>) entity;
            entity = gEntity.getEntity();
            context.requestContext().response().entity(entity);
            context.httpEntity().type(gEntity.getRawType());
            context.httpEntity().genericType(gEntity.getType());
        }
        Response response = null;
        if (entity instanceof Response) {
            response = (Response) entity;
        } else if (entity instanceof Response.ResponseBuilder) {
            response = ((Response.ResponseBuilder) entity).build();
        }
        if (response != null) {
            adaptResponse(response, context);
        }
        executor.proceed();
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    public boolean alsoApplyWhenMissingHandler() {
        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void adaptResponse(Response from, ResponseEntityResolverContext target) {
        Object entity = from.getEntity();
        if (entity != null) {
            target.requestContext().response().entity(entity);
            Class<?> type = ClassUtils.getUserType(entity);
            Class<?> genericType = ClassUtils.getRawType(type);
            target.httpEntity().type(type);
            target.httpEntity().genericType(genericType);
        }
        target.requestContext().response().status(from.getStatus());
    }
}

