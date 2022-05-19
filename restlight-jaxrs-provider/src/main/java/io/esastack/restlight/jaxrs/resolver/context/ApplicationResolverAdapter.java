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

import esa.commons.Checks;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.context.ContextResolverContext;
import jakarta.ws.rs.core.Application;

public class ApplicationResolverAdapter extends AbstractContextResolverAdapter {

    private final Application application;

    public ApplicationResolverAdapter(Application application) {
        Checks.checkNotNull(application, "application");
        this.application = application;
    }

    @Override
    protected boolean supports0(Param param) {
        return Application.class.isAssignableFrom(param.type());
    }

    @Override
    public Object resolve(ContextResolverContext context) {
        return application;
    }

}

