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

import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.arg.AbstractMatrixArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.springmvc.annotation.shaded.MatrixVariable0;
import esa.restlight.springmvc.util.RequestMappingUtils;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the MatrixVariable
 */
public class MatrixVariableArgumentResolver extends AbstractMatrixArgumentResolver {

    @Override
    protected NameAndValue createNameAndValue(Param param) {
        MatrixVariable0 matrixVariable =
                MatrixVariable0.fromShade(param.getAnnotation(MatrixVariable0.shadedClass()));
        assert matrixVariable != null;
        return new NameAndValue(matrixVariable.value(),
                matrixVariable.required(),
                RequestMappingUtils.normaliseDefaultValue(matrixVariable.defaultValue()));
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(MatrixVariable0.shadedClass());
    }

    @Override
    protected String getPathVar(Param param) {
        MatrixVariable0 matrixVariable =
                MatrixVariable0.fromShade(param.getAnnotation(MatrixVariable0.shadedClass()));
        assert matrixVariable != null;
        return RequestMappingUtils.normaliseDefaultValue(matrixVariable.pathVar());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
