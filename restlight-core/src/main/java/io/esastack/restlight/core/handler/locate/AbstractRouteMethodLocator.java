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
package io.esastack.restlight.core.handler.locate;

import io.esastack.restlight.core.handler.RouteMethodInfo;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.core.method.RouteHandlerMethodImpl;
import io.esastack.restlight.core.util.InterceptorUtils;
import io.esastack.restlight.core.util.RouteUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;
import java.util.Optional;

public abstract class AbstractRouteMethodLocator implements RouteMethodLocator {

    private final String defaultScheduling;

    public AbstractRouteMethodLocator(String defaultScheduling) {
        this.defaultScheduling = defaultScheduling;
    }

    @Override
    public Optional<RouteMethodInfo> getRouteHandlerInfo(Class<?> userType, Method method) {
        HandlerMethod handlerMethod = HandlerMethodImpl.of(userType, method);
        return Optional.of(new RouteMethodInfo(RouteHandlerMethodImpl.of(handlerMethod,
                InterceptorUtils.isIntercepted(handlerMethod),
                RouteUtils.scheduling(handlerMethod, defaultScheduling)),
                isLocator(handlerMethod), getCustomResponse(handlerMethod)));
    }

    /**
     * Whether given {@link HandlerMethod} is a locator or not.
     *
     * @param handlerMethod handler method
     * @return  {@code true} if the {@code handlerMethod} is a locator, otherwise {@code false}.
     */
    protected boolean isLocator(HandlerMethod handlerMethod) {
        return false;
    }

    /**
     * Get custom response
     *
     * @param handlerMethod handlerMethod
     * @return HttpResponseStatus
     */
    protected abstract HttpResponseStatus getCustomResponse(HandlerMethod handlerMethod);
}
