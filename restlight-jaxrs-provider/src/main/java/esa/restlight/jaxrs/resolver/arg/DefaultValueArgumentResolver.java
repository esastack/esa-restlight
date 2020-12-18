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
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.util.ConverterUtils;

import javax.ws.rs.DefaultValue;
import java.util.List;

public class DefaultValueArgumentResolver implements ArgumentResolverFactory {

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        DefaultValue ann = param.getAnnotation(DefaultValue.class);
        final Object defaultValue =
                ConverterUtils.forceConvertStringValue(ann.value(), param.genericType());
        return (request, response) -> defaultValue;
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(DefaultValue.class);
    }

    @Override
    public int getOrder() {
        return 10000;
    }
}
