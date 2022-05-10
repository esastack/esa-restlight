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
package io.esastack.restlight.springmvc.resolver.param;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.HttpParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractHttpParamResolver;
import io.esastack.restlight.springmvc.annotation.shaded.RequestParam0;
import io.esastack.restlight.springmvc.util.RequestMappingUtils;

/**
 * Implementation of {@link HttpParamResolverFactory} for resolving argument that annotated by the RequestParam.
 */
public class RequestParamResolver extends AbstractHttpParamResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestParam0.shadedClass());
    }

    @Override
    protected NameAndValue<String> createNameAndValue(Param param) {
        RequestParam0 requestParam
                = RequestParam0.fromShade(param.getAnnotation(RequestParam0.shadedClass()));
        assert requestParam != null;
        return new NameAndValue<>(requestParam.value(),
                requestParam.required(),
                RequestMappingUtils.normaliseDefaultValue(requestParam.defaultValue()));
    }

    @Override
    protected String extractName(Param param) {
        RequestParam0 requestParam
                = RequestParam0.fromShade(param.getAnnotation(RequestParam0.shadedClass()));
        assert requestParam != null;
        return requestParam.value();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
