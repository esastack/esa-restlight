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
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.arg.AbstractPathVariableArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;

import javax.ws.rs.PathParam;


/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the PathVariable.
 */
public class PathParamArgumentResolver extends AbstractPathVariableArgumentResolver {

    @Override
    protected NameAndValue createNameAndValue(Param parameter) {
        PathParam pathParam =
                parameter.getAnnotation(PathParam.class);
        assert pathParam != null;
        return new NameAndValue(pathParam.value(), false,
                JaxrsMappingUtils.extractDefaultValue(parameter));
    }

    @Override
    public boolean supports(Param parameter) {
        return parameter.hasAnnotation(PathParam.class);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
