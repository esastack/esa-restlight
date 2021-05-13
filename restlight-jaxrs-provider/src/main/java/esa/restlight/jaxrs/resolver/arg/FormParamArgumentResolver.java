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
import esa.restlight.core.resolver.arg.AbstractParamArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;

import javax.ws.rs.FormParam;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the {@link
 * FormParam}.
 */
public class FormParamArgumentResolver extends AbstractParamArgumentResolver {

    @Override
    public boolean supports(Param parameter) {
        return parameter.hasAnnotation(FormParam.class);
    }

    @Override
    protected NameAndValue createNameAndValue(Param parameter) {
        FormParam formParam
                = parameter.getAnnotation(FormParam.class);
        assert formParam != null;
        return new NameAndValue(formParam.value(),
                false,
                JaxrsMappingUtils.extractDefaultValue(parameter),
                JaxrsMappingUtils.hasDefaultValue(parameter));
    }

    @Override
    public int getOrder() {
        return 20;
    }

}
