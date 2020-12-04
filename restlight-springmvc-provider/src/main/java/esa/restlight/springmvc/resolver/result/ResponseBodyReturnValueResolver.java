/*
 * Copyright 2020 OPPO ESA Stack Project
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
package esa.restlight.springmvc.resolver.result;

import esa.commons.reflect.AnnotationUtils;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.result.AbstractResponseBodyReturnValueResolver;
import esa.restlight.springmvc.annotation.shaded.ResponseBody0;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the ResponseBody.
 */
public class ResponseBodyReturnValueResolver extends AbstractResponseBodyReturnValueResolver {

    public ResponseBodyReturnValueResolver(boolean negotiation, String parameterName) {
        super(negotiation, parameterName);
    }

    @Override
    public boolean supports(InvocableMethod invocableMethod) {
        return AnnotationUtils.hasAnnotation(invocableMethod.beanType(), ResponseBody0.shadedClass())
                || invocableMethod.hasMethodAnnotation(ResponseBody0.shadedClass());
    }

    @Override
    public int getOrder() {
        return 1000;
    }

}
