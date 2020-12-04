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
package esa.restlight.core.handler.locate;

import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.RouteHandler;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * This is used to get an {@link Optional} instance of {@link RouteHandler} from given target {@link
 * Method}.
 */
public interface RouteHandlerLocator extends HandlerLocator {

    /**
     * Gets an an {@link Optional} instance of {@link RouteHandler} if possible.
     *
     * @param userType user type
     * @param method   target method
     * @param bean     target bean
     *
     * @return optional value of route handler.
     */
    Optional<RouteHandler> getRouteHandler(Class<?> userType, Method method, Object bean);


    /**
     * @see #getRouteHandler(Class, Method, Object)
     */
    default Optional<RouteHandler> getRouteHandler(Method method, Object bean) {
        return getRouteHandler(bean.getClass(), method, bean);
    }

    /**
     * Default to use {@link #getRouteHandler(Class, Method, Object)}.
     */
    @Override
    default Optional<Handler> getHandler(Class<?> userType, Method method, Object bean) {
        return Optional.ofNullable(getRouteHandler(userType, method, bean).orElse(null));
    }

}
