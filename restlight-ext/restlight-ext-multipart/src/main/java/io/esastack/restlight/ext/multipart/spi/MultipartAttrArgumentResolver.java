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
package io.esastack.restlight.ext.multipart.spi;

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.ext.multipart.annotation.FormParam;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MultipartAttrArgumentResolver extends AbstractMultipartParamResolver<String> {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(FormParam.class);
    }

    @Override
    protected Function<Param, NameAndValue> initNameAndValueCreator(BiFunction<String,
            Boolean,
            Object> defaultValueConverter) {
        return (param) -> {
            FormParam formParam = param.getAnnotation(FormParam.class);
            assert formParam != null;
            return new NameAndValue(formParam.value(),
                    formParam.required(),
                    defaultValueConverter.apply(ConverterUtils.normaliseDefaultValue(formParam.defaultValue()),
                            false));
        };
    }

    @Override
    protected BiFunction<String, RequestContext, String> doInitValueProvider(Param param) {
        return (name, ctx) -> ctx.attr(AttributeKey.stringKey(PREFIX + name)).get();
    }

    @Override
    protected NameAndValueResolver.Converter<String> doInitConverter(Param param,
                                                                     BiFunction<Class<?>,
                                                                             Type,
                                                                             StringConverter> converterLookup) {
        StringConverter converter = converterLookup.apply(param.type(), param.genericType());
        return (name, ctx, valueProvider) -> converter.fromString(valueProvider.apply(name, ctx));
    }

    @Override
    protected BiFunction<String, Boolean, Object> initDefaultValueConverter(
            NameAndValueResolver.Converter<String> converter) {
        return (defaultValue, isLazy) -> converter.convert(null, null, (name, ctx) -> defaultValue);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
