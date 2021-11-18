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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.AbstractParamResolver;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import jakarta.ws.rs.QueryParam;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the
 * {@link QueryParam}.
 */
public class QueryParamResolver extends AbstractParamResolver {

    @Override
    public boolean supports(Param parameter) {
        return parameter.hasAnnotation(QueryParam.class);
    }

    @Override
    protected NameAndValue createNameAndValue(Param parameter) {
        QueryParam queryParam
                = parameter.getAnnotation(QueryParam.class);
        assert queryParam != null;
        return new NameAndValue(queryParam.value(),
                false,
                JaxrsMappingUtils.extractDefaultValue(parameter));
    }

    @Override
    public int getOrder() {
        return 10;
    }

}
