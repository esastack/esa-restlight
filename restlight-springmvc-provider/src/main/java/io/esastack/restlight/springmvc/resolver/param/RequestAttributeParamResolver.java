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

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractNameAndValueParamResolver;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.springmvc.annotation.shaded.RequestAttribute0;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestAttribute.
 */
public class RequestAttributeParamResolver implements ParamResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        List<? extends HttpRequestSerializer> serializers) {
        return new AbstractNameAndValueParamResolver(param) {

            final Function<String, Object> converter =
                    ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);

            @Override
            protected Object resolveName(String name, RequestContext context) {
                Object v = context.attr(AttributeKey.valueOf(name)).get();
                if (converter != null && v instanceof String) {
                    return converter.apply((String) v);
                }
                return v;
            }

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                RequestAttribute0 requestAttribute
                        = RequestAttribute0.fromShade(param.getAnnotation(RequestAttribute0.shadedClass()));
                assert requestAttribute != null;
                return new NameAndValue(requestAttribute.value(), requestAttribute.required());
            }
        };
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestAttribute0.shadedClass());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
