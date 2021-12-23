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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractPathVariableParamResolver;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import jakarta.ws.rs.PathParam;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the PathVariable.
 */
public class PathParamResolver extends AbstractPathVariableParamResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(PathParam.class);
    }

    @Override
    protected NameAndValue<String> createNameAndValue(Param param) {
        PathParam pathParam =
                param.getAnnotation(PathParam.class);
        assert pathParam != null;
        return new NameAndValue<>(pathParam.value(), false, JaxrsMappingUtils.extractDefaultValue(param));
    }

    @Override
    public int getOrder() {
        return 10;
    }

}
