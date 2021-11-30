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
package io.esastack.restlight.springmvc.resolver.rspentity;

import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.FlexibleResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseBody0;

import java.util.List;

public class FlexibleResponseEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
        return new FlexibleResponseEntityResolver0(serializers);
    }

    private static class FlexibleResponseEntityResolver0 extends FlexibleResponseEntityResolver {

        private FlexibleResponseEntityResolver0(List<? extends HttpResponseSerializer> serializers) {
            super(serializers);
        }

        @Override
        protected boolean supports(ResponseEntity entity) {
            HandlerMethod handlerMethod = entity.handler().orElse(null);
            if (handlerMethod == null) {
                return false;
            }
            return AnnotationUtils.hasAnnotation(handlerMethod.beanType(), ResponseBody0.shadedClass())
                    || handlerMethod.hasMethodAnnotation(ResponseBody0.shadedClass());
        }

        @Override
        public int getOrder() {
            return 300;
        }
    }
}

