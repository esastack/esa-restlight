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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.Param;
import jakarta.ws.rs.ext.Providers;

public class ProvidersResolverAdapter extends AbstractContextResolverAdapter {

    private final Providers providers;

    public ProvidersResolverAdapter(Providers providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @Override
    public boolean supports0(Param param) {
        return Providers.class.isAssignableFrom(param.type());
    }

    @Override
    public Object resolve(Param param, DeployContext<? extends RestlightOptions> context) throws Exception {
        return providers;
    }

}

