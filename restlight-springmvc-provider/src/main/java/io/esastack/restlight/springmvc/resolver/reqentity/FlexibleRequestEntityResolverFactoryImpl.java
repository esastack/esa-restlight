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
package io.esastack.restlight.springmvc.resolver.reqentity;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.param.entity.FlexibleRequestEntityResolverFactory;
import io.esastack.restlight.springmvc.annotation.shaded.RequestBody0;

public class FlexibleRequestEntityResolverFactoryImpl extends FlexibleRequestEntityResolverFactory {

    public FlexibleRequestEntityResolverFactoryImpl(boolean negotiation, String paramName) {
        super(negotiation, paramName);
    }

    @Override
    public boolean supports(Param param) {
        return super.supports(param) || param.hasAnnotation(RequestBody0.shadedClass());
    }

    @Override
    protected NameAndValue<String> createNameAndValue(Param param) {
        return new NameAndValue<>(param.name(), param.hasAnnotation(RequestBody0.shadedClass())
                && RequestBody0.fromShade(param.getAnnotation(RequestBody0.shadedClass())).required());
    }

    @Override
    public int getOrder() {
        return 1000;
    }

}

