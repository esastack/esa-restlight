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
import esa.restlight.core.resolver.arg.AbstractPathVariableArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.springmvc.annotation.shaded.PathVariable0;


/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the PathVariable.
 */
public class PathVariableArgumentResolver extends AbstractPathVariableArgumentResolver {

    @Override
    protected NameAndValue createNameAndValue(Param param) {
        PathVariable0 pathVariable =
                PathVariable0.fromShade(param.getAnnotation(PathVariable0.shadedClass()));
        assert pathVariable != null;
        return new NameAndValue(pathVariable.value(), pathVariable.required());
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(PathVariable0.shadedClass());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
