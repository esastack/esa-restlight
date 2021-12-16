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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.nav.StrNameAndValueResolverFactory;
import io.esastack.restlight.server.util.PathVariableUtils;

import java.util.function.BiFunction;

/**
 * Implementation of {@link StrNameAndValueResolverFactory} for resolving argument that annotated by
 * the PathVariable.
 */
public abstract class AbstractPathVariableParamResolver extends StrNameAndValueResolverFactory {

    @Override
    protected BiFunction<String, RequestContext, String> initValueProvider(Param param) {
        return (name, ctx) -> {
            String value = PathVariableUtils.getPathVariable(ctx, name);
            return StringUtils.isEmpty(value) ? value : cleanTemplateValueIfNecessary(value);
        };
    }

    /**
     * Remove matrix variables from template if necessary. eg: the url template looks like: /abc/{def}, and the real
     * request url is: /abc/xyz;a=b;c=d ok the template's real value is "xyz;a=b;c=d", just clan the url and return
     * xyz.
     *
     * @param templateVariable template variable
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
