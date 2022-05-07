/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.resolver.reqentity;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.reqentity.FixedRequestEntityResolverFactory;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import jakarta.ws.rs.core.Context;

public class FixedRequestEntityResolverFactoryImpl extends FixedRequestEntityResolverFactory {

    @Override
    protected boolean supports0(Param param) {
        // always returns true if current Param is a MethodParam
        // All of the parameters which is not annotated by argument annotation like @QueryParam and @HeaderParam will
        // be regarded as a body parameter.
        return param.isMethodParam() && !param.hasAnnotation(Context.class);
    }

    @Override
    protected NameAndValue<String> createNameAndValue(Param param) {
        return new NameAndValue<>(param.name(), false, JaxrsMappingUtils.extractDefaultValue(param));
    }

    @Override
    public int getOrder() {
        return 510;
    }
}

