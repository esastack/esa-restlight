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
package esa.restlight.springmvc.resolver.arg;

import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.arg.AbstractSpecifiedFixedRequestBodyArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.springmvc.annotation.shaded.RequestBody0;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestBody and
 * {@link RequestSerializer}, {@link Serializer}
 */
public class SpecifiedFixedRequestBodyArgumentResolver extends AbstractSpecifiedFixedRequestBodyArgumentResolver {

    @Override
    protected boolean supports0(Param param) {
        return super.supports0(param) || param.hasAnnotation(RequestBody0.shadedClass());
    }

    @Override
    protected NameAndValue createNameAndValue(Param param) {
        return new NameAndValue(param.name(), param.hasAnnotation(RequestBody0.shadedClass())
                && RequestBody0.fromShade(param.getAnnotation(RequestBody0.shadedClass())).required(),
                null);
    }

    @Override
    public int getOrder() {
        return 500;
    }
}
