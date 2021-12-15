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

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.nav.AbstractNameAndValueResolver;
import io.esastack.restlight.server.bootstrap.WebServerException;

public abstract class AbstractNameAndValueParamResolver extends AbstractNameAndValueResolver implements ParamResolver {

    public AbstractNameAndValueParamResolver(Param param) {
        super(param);
    }

    @Override
    public Object resolve(Param param, RequestContext context) throws Exception {
        Object arg = this.resolveName(nav.name, context.request());
        if (arg == null) {
            if (nav.hasDefaultValue) {
                arg = nav.defaultValue.get();
            }
            if (nav.required && arg == null) {
                throw WebServerException.badRequest("Missing required value: " + nav.name);
            }
        }
        return arg;
    }

    /**
     * Try to resolve the value by the given name from the {@link HttpRequest}.
     *
     * @param name    name
     * @param request request
     * @return resolved value
     * @throws Exception occurred
     */
    protected abstract Object resolveName(String name, HttpRequest request) throws Exception;
}
