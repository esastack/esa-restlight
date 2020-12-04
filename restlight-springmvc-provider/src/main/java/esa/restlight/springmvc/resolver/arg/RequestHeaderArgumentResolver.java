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
import esa.restlight.core.resolver.arg.AbstractHeaderArgumentResolver;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.springmvc.annotation.shaded.RequestHeader0;
import esa.restlight.springmvc.util.RequestMappingUtils;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestHeader.
 */
public class RequestHeaderArgumentResolver extends AbstractHeaderArgumentResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestHeader0.shadedClass());
    }

    @Override
    protected NameAndValue createNameAndValue(Param param) {
        RequestHeader0 requestHeader =
                RequestHeader0.fromShade(param.getAnnotation(RequestHeader0.shadedClass()));
        assert requestHeader != null;
        return new NameAndValue(requestHeader.value(),
                requestHeader.required(),
                RequestMappingUtils.normaliseDefaultValue(requestHeader.defaultValue()));
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
