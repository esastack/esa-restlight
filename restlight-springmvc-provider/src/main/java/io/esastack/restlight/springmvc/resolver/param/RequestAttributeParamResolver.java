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
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.HttpParamResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.springmvc.annotation.shaded.RequestAttribute0;

/**
 * Implementation of {@link HttpParamResolverFactory} for resolving argument that annotated by the RequestAttribute.
 */
public class RequestAttributeParamResolver extends NameAndValueResolverFactory {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestAttribute0.shadedClass());
    }

    @Override
    protected NameAndValueResolver createResolver(Param param,
                                                  StringConverterProvider converters) {
        final StringConverter converter = converters.get(StringConverterProvider.Key.from(param));
        return new NameAndValueResolver() {
            @Override
            public Object resolve(String name, RequestContext ctx) {
                Object value = ctx.attrs().attr(AttributeKey.valueOf(name)).get();
                if (converter != null && (value instanceof String)) {
                    return converter.fromString((String) value);
                }
                return value;
            }

            @Override
            public NameAndValue<Object> createNameAndValue(Param param) {
                RequestAttribute0 requestAttribute
                        = RequestAttribute0.fromShade(param.getAnnotation(RequestAttribute0.shadedClass()));
                assert requestAttribute != null;
                return new NameAndValue<>(requestAttribute.value(), requestAttribute.required());
            }
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
