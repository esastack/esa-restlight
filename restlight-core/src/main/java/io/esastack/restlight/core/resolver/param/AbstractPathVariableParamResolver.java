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

import esa.commons.StringUtils;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.util.PathVariableUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the PathVariable.
 */
public abstract class AbstractPathVariableParamResolver implements ParamResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        List<? extends HttpRequestSerializer> serializers) {
        return new AbstractNameAndValueParamResolver(param) {

            protected final Function<String, Object> converter =
                    ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);

            @Override
            protected Object resolveName(String name, HttpRequest request) {
                String value = PathVariableUtils.getPathVariable(request, name);
                return converter.apply(StringUtils.isEmpty(value) ? value : cleanTemplateValueIfNecessary(value));
            }

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return AbstractPathVariableParamResolver.this.createNameAndValue(param);
            }
        };
    }

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param parameter parameter
     *
     * @return name and value
     */
    protected abstract NameAndValue createNameAndValue(Param parameter);

    /**
     * Remove matrix variables from template if necessary. eg: the url template looks like: /abc/{def}, and the real
     * request url is: /abc/xyz;a=b;c=d ok the template's real value is "xyz;a=b;c=d", just clan the url and return
     * xyz.
     *
     * @param templateVariable template variable
     *
     * @return clean template variable
     */
    private static String cleanTemplateValueIfNecessary(String templateVariable) {
        final int pos = templateVariable.indexOf(";");
        if (pos == -1) {
            return templateVariable;
        }
        return templateVariable.substring(0, pos);
    }
}
