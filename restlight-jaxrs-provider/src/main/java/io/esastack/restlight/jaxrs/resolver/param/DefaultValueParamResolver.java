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

import esa.commons.function.Function3;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.DefaultValue;

import java.lang.reflect.Type;
import java.util.List;

public class DefaultValueParamResolver implements ParamResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        Function3<Class<?>, Type, Param, StringConverter> converterFunc,
                                        List<? extends HttpRequestSerializer> serializers) {
        DefaultValue ann = JaxrsUtils.getAnnotation(param, DefaultValue.class);
        final Object defaultValue =
                ConverterUtils.forceConvertStringValue(ann.value(), param.genericType());
        return (ctx) -> defaultValue;
    }

    @Override
    public boolean supports(Param param) {
        return JaxrsUtils.hasAnnotation(param, DefaultValue.class);
    }

    @Override
    public int getOrder() {
        return 10000;
    }
}
