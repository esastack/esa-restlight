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
import esa.restlight.core.util.Ordered;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * This is used to get an {@link Optional} instance of {@link Handler} from given target {@link Method}.
 */
public interface HandlerLocator extends Ordered {

    /**
     * Gets an an {@link Optional} instance of {@link Handler} if possible.
     *
     * @param userType user type
     * @param method   target method
     * @param bean     target bean
     *
     * @return optional value of handler.
     */
    Optional<Handler> getHandler(Class<?> userType, Method method, Object bean);

    /**
     * @see #getHandler(Class, Method, Object)
     */
    default Optional<Handler> getHandler(Method method, Object bean) {
        return getHandler(bean.getClass(), method, bean);
    }

}
