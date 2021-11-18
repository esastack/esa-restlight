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
package io.esastack.restlight.jaxrs.resolver.context;

import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import jakarta.ws.rs.core.Context;

abstract class AbstractContextResolverAdapter implements ContextResolverAdapter {

    @Override
    public boolean supports(Param param) {
        return AnnotationUtils.hasAnnotation(param.current(), Context.class) && supports0(param);
    }

    /**
     * Whether supports given {@code param} or not.
     *
     * @param param param
     * @return      {@code true} if supports given {@code param}, otherwise {@code false}.
     */
    protected abstract boolean supports0(Param param);
}

