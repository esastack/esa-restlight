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
import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndStringsValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.PathVariableUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link NameAndValueResolverFactory} for resolving argument that annotated by
 * the MatrixVariable
 */
public abstract class AbstractMatrixParamResolver extends NameAndValueResolverFactory {

    @Override
    public NameAndValueResolver createResolver(Param param, HandlerResolverFactory resolverFactory) {

        String pathVar = getPathVar(param);
        if (isMatrixVariableMap(param, extractName(param))) {
            return new MapResolver(pathVar, isSingleValueMap(param));
        }

        return new NameAndStringsValueResolver(param,
                resolverFactory,
                (name, ctx) -> extractValues(name, ctx, pathVar, param),
                createNameAndValue(param));
    }

    protected List<String> extractValues(String name, RequestContext ctx, String pathVar, Param param) {

        Map<String, MultiValueMap<String, String>> pathParameters =
                PathVariableUtils.getMatrixVariables(ctx);

        if (pathParameters.isEmpty()) {
            return null;
        }

        List<String> paramValues = null;

        if (pathVar != null) {
            MultiValueMap<String, String> m = pathParameters.get(pathVar);
            if (m != null) {
                paramValues = m.get(name);
            }
        } else {
            boolean found = false;
            paramValues = new LinkedList<>();
            for (MultiValueMap<String, String> params : pathParameters.values()) {
                if (params.containsKey(name)) {
                    if (found) {
                        String paramType = param.type().getName();
                        throw WebServerException.badRequest("Found more than one match for URI path parameter '"
                                + name + "' for parameter type ["
                                + paramType + "]. Use 'pathVar' attribute to disambiguate.");
                    }
                    paramValues.addAll(params.get(name));
                    found = true;
                }
            }
        }

        return paramValues;
    }

    protected abstract String extractName(Param param);

    /**
     * Obtains path variable from the given {@code param}.
     *
     * @param param param
     * @return value
     */
    protected abstract String getPathVar(Param param);

    /**
     * Constructs an {@link NameAndValue} from the given {@code param}.
     *
     * @param param param
     * @return name and value
     */
    protected abstract NameAndValue<String> createNameAndValue(Param param);

    private class MapResolver implements NameAndValueResolver {

        private final String pathVar;
        private final boolean singleValueMap;

        public MapResolver(String pathVar, boolean singleValueMap) {
            this.pathVar = pathVar;
            this.singleValueMap = singleValueMap;
        }

        @Override
        public Object resolve(String name, RequestContext ctx) {
            Map<String, MultiValueMap<String, String>> pathParameters =
                    PathVariableUtils.getMatrixVariables(ctx);

            if (pathParameters.isEmpty()) {
                return Collections.emptyMap();
            }

            return getMatrixVariableMap(pathParameters);
        }

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractMatrixParamResolver.this.createNameAndValue(param);
        }

        private Object getMatrixVariableMap(Map<String, MultiValueMap<String, String>> matrixVariables) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

            if (pathVar != null) {
                MultiValueMap<String, String> mapForPathVariable = matrixVariables.get(pathVar);
                if (mapForPathVariable == null) {
                    return Collections.emptyMap();
                }
                map.putAll(mapForPathVariable);
            } else {
                for (MultiValueMap<String, String> vars : matrixVariables.values()) {
                    vars.forEach((name, values) -> {
                        for (String value : values) {
                            map.add(name, value);
                        }
                    });
                }
            }

            return singleValueMap ? map.toSingleValueMap() : map;
        }
    }

    private static boolean isMatrixVariableMap(Param param, String matrixName) {
        return Map.class.isAssignableFrom(param.type()) &&
                StringUtils.isBlank(matrixName);
    }

    private static boolean isSingleValueMap(Param param) {
        if (!MultiValueMap.class.isAssignableFrom(param.type())) {
            Class<?>[] generics = ClassUtils.retrieveGenericTypes(param.genericType());
            if (generics.length == 2) {
                return !List.class.isAssignableFrom(generics[1]);
            }
        }
        return false;
    }

}
