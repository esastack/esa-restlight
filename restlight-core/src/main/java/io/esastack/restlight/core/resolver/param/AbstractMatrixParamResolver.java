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
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.util.PathVariableUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the MatrixVariable
 */
public abstract class AbstractMatrixParamResolver implements ParamResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        List<? extends HttpRequestSerializer> serializers) {
        return new Resolver(param);
    }

    @Override
    public boolean supports(Param param) {
        return false;
    }

    protected class Resolver extends AbstractNameAndValueParamResolver {

        private final Function<String, Object> converter;
        private String pathVar;
        private boolean matrixVariableMap;
        private boolean singleValueMap;

        protected Resolver(Param param) {
            super(param);
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
        }

        @Override
        protected Object resolveName(String name, HttpRequest request) {
            Map<String, MultiValueMap<String, String>> pathParameters =
                    PathVariableUtils.getMatrixVariables(request);

            if (pathParameters.isEmpty()) {
                return matrixVariableMap ? Collections.emptyMap() : null;
            }

            List<String> paramValues = null;
            // Handle matrixVariableMap
            if (matrixVariableMap) {
                return getMatrixVariableMap(pathParameters);
            }

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

            if (paramValues == null || paramValues.isEmpty()) {
                return null;
            } else if (paramValues.size() == 1) {
                return converter.apply(paramValues.get(0));
            } else {
                return paramValues;
            }
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            NameAndValue nav = AbstractMatrixParamResolver.this.createNameAndValue(param);
            this.matrixVariableMap = isMatrixVariableMap(param, nav.name);
            this.singleValueMap = isSingleValueMap(param);
            this.pathVar = AbstractMatrixParamResolver.this.getPathVar(param);
            return nav;
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

    /**
     * Obtains path variable from the given {@code param}.
     *
     * @param param param
     * @return  value
     */
    protected abstract String getPathVar(Param param);

    /**
     * Constructs an {@link NameAndValue} from the given {@code param}.
     *
     * @param param param
     * @return  name and value
     */
    protected abstract NameAndValue createNameAndValue(Param param);
}
