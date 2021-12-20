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
package io.esastack.restlight.jaxrs.spi;

import esa.commons.ClassUtils;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

public class JaxrsResponseAdapterFactory implements ResponseEntityResolverAdviceFactory {

    @Override
    public boolean supports(HandlerMethod handlerMethod) {
        Class<?> returnType = ClassUtils.getRawType(handlerMethod.method().getReturnType());
        return Response.class.isAssignableFrom(returnType)
                || Response.ResponseBuilder.class.isAssignableFrom(returnType)
                || GenericEntity.class.isAssignableFrom(returnType);
    }

    @Override
    public ResponseEntityResolverAdvice createResolverAdvice(ResponseEntity entity) {
        return JaxrsResponseAdapter.SINGLETON;
    }

    private static class JaxrsResponseAdapter implements ResponseEntityResolverAdvice {

        private static final ResponseEntityResolverAdvice SINGLETON = new JaxrsResponseAdapter();

        private JaxrsResponseAdapter() {
        }

        @Override
        public void aroundWrite(ResponseEntityResolverContext context) {
            Object entity = context.context().response().entity();
            if (entity == null) {
                return;
            }
            if (entity instanceof GenericEntity) {
                GenericEntity<?> gEntity = (GenericEntity<?>) entity;
                entity = gEntity.getEntity();
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
                context.context().response().entity(response);
            }
        }
    }

}

