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
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverterProvider;
import io.esastack.restlight.core.resolver.nav.NameAndStringsValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.server.context.RequestContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link NameAndValueResolverFactory} for resolving argument that annotated by
 * the RequestParam.
 */
public abstract class AbstractParamResolver extends NameAndValueResolverFactory {

    @Override
    protected NameAndValueResolver createResolver(Param param,
                                                  StringConverterProvider converters) {
        String name = extractName(param);
        if (StringUtils.isEmpty(name)
                && Map.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types.length == 2) {
                Class<?> valueType = types[1];
                if (String.class.equals(valueType)) {
                    // Map<String, String>
                    return new SingleMapResolver();
                } else if (List.class.equals(valueType)) {
                    // Map<String, List<String>>
                    return new ListMapResolver();
                }
            }
        }

        return new NameAndStringsValueResolver(param,
                converters,
                this::extractValue,
                createNameAndValue(param));
    }

    /**
     * Extracts name of given {@code param}.
     *
     * @param param     param
     * @return          name
     */
    protected abstract String extractName(Param param);

    /**
     * Creates {@link NameAndValue} for given {@code param}.
     *
     * @param param     param
     * @return          name and value
     */
    protected abstract NameAndValue<String> createNameAndValue(Param param);

    private Collection<String> extractValue(String name, RequestContext ctx) {
        return ctx.request().paramsMap().get(name);
    }

    private class SingleMapResolver implements NameAndValueResolver {
        @Override
        public Object resolve(String name, RequestContext ctx) {
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

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractParamResolver.this.createNameAndValue(param);
        }
    }

    private class ListMapResolver implements NameAndValueResolver {
        @Override
        public Object resolve(String name, RequestContext ctx) {
            return ctx.request().paramsMap();
        }

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractParamResolver.this.createNameAndValue(param);
        }
    }
}
