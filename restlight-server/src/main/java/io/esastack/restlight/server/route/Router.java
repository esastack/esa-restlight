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
package io.esastack.restlight.server.route;

import io.esastack.restlight.server.context.RequestContext;

/**
 * This {@link Router} is used to match a {@link Route} to handle the given {@link RequestContext}.
 */
public interface Router {

    /**
     * Gets the matched {@link Route} by the given {@code context}.
     *
     * @param context context
     *
     * @return matched {@link Route} or {@code null} if missing
     */
    Route route(RequestContext context);

}

