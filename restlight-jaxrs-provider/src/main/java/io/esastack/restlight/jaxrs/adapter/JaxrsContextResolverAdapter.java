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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;

public class JaxrsContextResolverAdapter implements ContextResolverAdapter {

    private final jakarta.ws.rs.ext.ContextResolver<?> delegating;
    private final Class<?> targetClass;

    public JaxrsContextResolverAdapter(jakarta.ws.rs.ext.ContextResolver<?> delegating) {
        Checks.checkNotNull(delegating, "delegating");
        this.delegating = delegating;
        this.targetClass = ClassUtils.getRawType(ClassUtils.getUserType(delegating));
    }

    @Override
    public Object resolve(Param param, DeployContext<? extends RestlightOptions> context) throws Exception {
        return delegating.getContext(param.type());
    }

    @Override
    public boolean supports(Param param) {
        return targetClass.equals(param.type());
    }
}

