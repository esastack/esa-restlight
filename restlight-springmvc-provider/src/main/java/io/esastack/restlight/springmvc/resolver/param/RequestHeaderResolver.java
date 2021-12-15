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

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractHeaderResolver;
import io.esastack.restlight.springmvc.annotation.shaded.RequestHeader0;
import io.esastack.restlight.springmvc.util.RequestMappingUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestHeader.
 */
public class RequestHeaderResolver extends AbstractHeaderResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestHeader0.shadedClass());
    }

    protected Function<Param, NameAndValue> initNameAndValueCreator(BiFunction<String, Boolean, Object> defaultValueConverter) {
        return (param) -> {
            RequestHeader0 requestHeader =
                    RequestHeader0.fromShade(param.getAnnotation(RequestHeader0.shadedClass()));
            assert requestHeader != null;
            return new NameAndValue(requestHeader.value(),
                    requestHeader.required(),
                    defaultValueConverter.apply(RequestMappingUtils.normaliseDefaultValue(requestHeader.defaultValue())
                            , false));
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
