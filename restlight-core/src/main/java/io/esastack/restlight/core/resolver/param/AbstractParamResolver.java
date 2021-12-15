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
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestParam.
 */
public abstract class AbstractParamResolver extends StrsNameAndValueResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        HandlerResolverFactory resolverFactory) {
        String name = extractParamName(param);
        if (StringUtils.isEmpty(name)
                && Map.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types.length == 2) {
                Class<?> valueType = types[1];
                if (String.class.equals(valueType)) {
                    // Map<String, String>
                    return new SingleValueMapResolver(param);
                } else if (List.class.equals(valueType)) {
                    // Map<String, List<String>>
                    return new MapResolver(param);
                }
            }
        }
        return super.createResolver(param, resolverFactory);
    }

    public abstract String extractParamName(Param param);

    @Override
    protected BiFunction<String, HttpRequest, Collection<String>> valueExtractor(Param param) {
        return (name, request) -> request.paramsMap().get(name);
    }

    private abstract class BaseResolver extends AbstractNameAndValueParamResolver {

        private BaseResolver(Param param) {
            super(param);
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractParamResolver.this.createNameAndValue(param, (defaultValue, isLazy) -> defaultValue);
        }
    }

    /**
     * Implementation for resolving argument type of {@link Map}
     */
    private class MapResolver extends BaseResolver {

        private MapResolver(Param param) {
            super(param);
        }

        @Override
        protected Map<String, List<String>> resolveName(String name, HttpRequest request) {
            return request.paramsMap();
        }
    }

    private class SingleValueMapResolver extends BaseResolver {

        private SingleValueMapResolver(Param param) {
            super(param);
        }

        @Override
        protected Map<String, String> resolveName(String name, HttpRequest request) {
            Map<String, List<String>> p = request.paramsMap();
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
