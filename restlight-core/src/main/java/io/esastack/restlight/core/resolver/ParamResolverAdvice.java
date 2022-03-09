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
package io.esastack.restlight.core.resolver;

import io.esastack.restlight.server.context.RequestContext;

/**
 * Allows customising the {@link RequestContext} before resolving the parameter of controller from
 * {@link RequestContext} and customizing the argument resoled from {@link RequestContext}.
 */
public interface ParamResolverAdvice {

    /**
     * This method is called around {@link ParamResolver#resolve(RequestContext)}.
     *
     * @param context context
     * @return resolved arg value
     * @throws Exception exception
     */
    Object aroundResolve(ParamResolverContext context) throws Exception;

}
