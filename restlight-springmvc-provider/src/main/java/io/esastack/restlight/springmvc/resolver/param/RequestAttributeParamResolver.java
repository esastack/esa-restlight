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

import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.springmvc.annotation.shaded.RequestAttribute0;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestAttribute.
 */
public class RequestAttributeParamResolver extends NameAndValueResolverFactory<Object> {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestAttribute0.shadedClass());
    }

    @Override
    protected Function<Param, NameAndValue> initNameAndValueCreator(BiFunction<String,
            Boolean,
            Object> defaultValueConverter) {
        return (param) -> {
            RequestAttribute0 requestAttribute
                    = RequestAttribute0.fromShade(param.getAnnotation(RequestAttribute0.shadedClass()));
            assert requestAttribute != null;
            return new NameAndValue(requestAttribute.value(), requestAttribute.required());
        };
    }

    @Override
    protected BiFunction<String, RequestContext, Object> initValueProvider(Param param) {
        return (name, ctx) -> ctx.request().getAttribute(name);
    }

    @Override
    protected BiFunction<String, Boolean, Object> initDefaultValueConverter(
            NameAndValueResolver.Converter<Object> converter) {
        return (defaultString, isLazy) -> null;
    }

    @Override
    protected NameAndValueResolver.Converter<Object> initConverter(Param param,
                                                                   BiFunction<Class<?>,
                                                                           Type,
                                                                           StringConverter> converterLookup) {
        final StringConverter converter =
                converterLookup.apply(param.type(), param.genericType());

        return (name, ctx, valueProvider) -> {
            Object v = valueProvider.apply(name, ctx);
            if (converter != null && v instanceof String) {
                return converter.fromString((String) v);
            }
            return v;
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
