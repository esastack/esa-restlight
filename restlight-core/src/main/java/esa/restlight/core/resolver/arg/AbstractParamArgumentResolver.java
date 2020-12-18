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
package esa.restlight.core.resolver.arg;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.util.ConverterUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestParam.
 */
public abstract class AbstractParamArgumentResolver implements ArgumentResolverFactory {

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {

        NameAndValue nameAndValue = createNameAndValue(param);
        if (StringUtils.isEmpty(nameAndValue.name)
                && Map.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types.length == 2) {
                Class<?> valueType = types[1];
                if (String.class.equals(valueType)) {
                    // Map<String, String>
                    return new SingleValueMapResolver(param, nameAndValue);
                } else if (List.class.equals(valueType)) {
                    // Map<String, List<String>>
                    return new MapResolver(param, nameAndValue);
                }
            }
        }
        return new StringOrListResolver(param, nameAndValue);
    }

    protected abstract NameAndValue createNameAndValue(Param param);

    private abstract static class BaseResolver extends AbstractNameAndValueArgumentResolver {

        private final NameAndValue nav0;

        BaseResolver(Param param, NameAndValue nav0) {
            super(param, nav0);
            this.nav0 = nav0;
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return nav0;
        }
    }

    /**
     * Implementation for resolving argument type of {@link String}
     */
    private static class StringOrListResolver extends BaseResolver {

        private final Function<String, Object> converter;
        private final Function<Collection<String>, Object> strsConverter;

        StringOrListResolver(Param param, NameAndValue nav) {
            super(param, nav);
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
            this.strsConverter = ConverterUtils.strs2ObjectConverter(param.genericType());
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            final List<String> values = request.getParameters(name);
            if (values == null || values.isEmpty()) {
                return null;
            }
            if (values.size() > 1 && strsConverter != null) {
                return strsConverter.apply(values);
            }
            return converter.apply(values.get(0));
        }
    }

    /**
     * Implementation for resolving argument type of {@link Map}
     */
    private static class MapResolver extends BaseResolver {

        MapResolver(Param param, NameAndValue nav) {
            super(param, nav);
        }

        @Override
        protected Map<String, List<String>> resolveName(String name, AsyncRequest request) {
            return request.parameterMap();
        }
    }

    private static class SingleValueMapResolver extends BaseResolver {

        SingleValueMapResolver(Param param, NameAndValue nav0) {
            super(param, nav0);
        }

        @Override
        protected Map<String, String> resolveName(String name, AsyncRequest request) {
            Map<String, List<String>> p = request.parameterMap();
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
    }

}
