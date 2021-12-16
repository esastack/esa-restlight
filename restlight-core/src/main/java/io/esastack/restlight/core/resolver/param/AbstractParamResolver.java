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
package io.esastack.restlight.core.resolver.param;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.StrsNameAndValueResolverFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Implementation of {@link StrsNameAndValueResolverFactory} for resolving argument that annotated by
 * the RequestParam.
 */
public abstract class AbstractParamResolver extends StrsNameAndValueResolverFactory {

    private static final NameAndValueResolver.Converter<Collection<String>> SINGLE_MAP_CONVERTER =
            (name, ctx, valueProvider) -> {
                if (ctx != null) {
                    return ctx.request().paramsMap();
                }
                //handle when convert defaultValue
                return null;
            };

    private static final NameAndValueResolver.Converter<Collection<String>> MAP_CONVERTER =
            (name, ctx, valueProvider) -> {
                if (ctx != null) {
                    Map<String, List<String>> p = ctx.request().paramsMap();
                    if (p.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    Map<String, String> m = new HashMap<>(p.size());
                    p.forEach((k, v) -> {
                        if (!v.isEmpty()) {
                            m.put(k, v.get(0));
                        }
                    });
                    return m;
                }
                //handle when convert defaultValue
                return null;
            };

    @Override
    protected NameAndValueResolver.Converter<Collection<String>> initConverter(
            Param param,
            BiFunction<Class<?>, Type, StringConverter> converterLookup) {
        String name = extractParamName(param);
        if (StringUtils.isEmpty(name)
                && Map.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types.length == 2) {
                Class<?> valueType = types[1];
                if (String.class.equals(valueType)) {
                    // Map<String, String>
                    return SINGLE_MAP_CONVERTER;
                } else if (List.class.equals(valueType)) {
                    // Map<String, List<String>>
                    return MAP_CONVERTER;
                }
            }
        }
        return super.initConverter(param, converterLookup);
    }

    public abstract String extractParamName(Param param);

    @Override
    protected BiFunction<String, RequestContext, Collection<String>> initValueProvider(Param param) {
        return (name, ctx) -> ctx.request().paramsMap().get(name);
    }
}
