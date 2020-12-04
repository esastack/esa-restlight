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
package esa.restlight.jaxrs.resolver.arg;

import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.arg.AbstractSpecifiedFixedRequestBodyArgumentResolver;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestBody and
 * {@link RequestSerializer}, {@link Serializer}
 */
public class SpecifiedFixedRequestBodyArgumentResolver extends AbstractSpecifiedFixedRequestBodyArgumentResolver {

    @Override
    protected boolean required(Param param) {
        return false;
    }

    @Override
    protected boolean supports0(Param param) {
        // always returns true if current Param is a MethodParam
        // All of the parameters which is not annotated by argument annotation like @QueryParam and @HeaderParam will
        // be regarded as a body parameter.
        return param.isMethodParam();
    }

    @Override
    protected String defaultValue(Param param) {
        return JaxrsMappingUtils.extractDefaultValue(param);
    }

    @Override
    public int getOrder() {
        return 510;
    }
}
