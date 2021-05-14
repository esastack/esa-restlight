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

import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.arg.AbstractRequestBodyArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;

public class RequestBodyArgumentResolver extends AbstractRequestBodyArgumentResolver {

    public RequestBodyArgumentResolver() {
    }

    public RequestBodyArgumentResolver(boolean negotiation, String paramName) {
        super(negotiation, paramName);
    }

    @Override
    public boolean supports(Param param) {
        // always returns true if current Param is a MethodParam
        // All of the parameters which is not annotated by argument annotation like @QueryParam and @HeaderParam will
        // be regarded as a body parameter.
        return param.isMethodParam();
    }

    @Override
    protected NameAndValue createNameAndValue(Param param) {
        return new NameAndValue(param.name(), false, JaxrsMappingUtils.extractDefaultValue(param));
    }

    @Override
    public int getOrder() {
        return 1100;
    }
}
