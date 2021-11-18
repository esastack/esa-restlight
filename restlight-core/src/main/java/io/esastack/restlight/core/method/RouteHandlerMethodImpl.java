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
package io.esastack.restlight.core.method;

import esa.commons.StringUtils;
import io.esastack.restlight.server.schedule.Schedulers;

import java.lang.reflect.Method;

public class RouteHandlerMethodImpl extends HandlerMethodImpl implements RouteHandlerMethod {

    private final boolean intercepted;
    private final String scheduler;

    private RouteHandlerMethodImpl(Class<?> userType, Method method,
                                   boolean intercepted, String scheduler) {
        super(userType, method);
        this.intercepted = intercepted;
        this.scheduler = StringUtils.nonEmptyOrElse(scheduler, Schedulers.BIZ);
    }

    public static RouteHandlerMethodImpl of(HandlerMethod handlerMethod, boolean intercepted, String scheduler) {
        return new RouteHandlerMethodImpl(handlerMethod.beanType(), handlerMethod.method(), intercepted, scheduler);
    }

    public static RouteHandlerMethodImpl of(Class<?> userType, Method method, boolean intercepted, String scheduler) {
        return new RouteHandlerMethodImpl(userType, method, intercepted, scheduler);
    }

    @Override
    public boolean intercepted() {
        return intercepted;
    }

    @Override
    public String scheduler() {
        return scheduler;
    }

    @Override
    public String toString() {
        return "RouteHandlerMethod: {" + beanType().getName() + " => " + method().toGenericString()
                + ", intercepted: " + intercepted + ", scheduler: " + scheduler + "}";
    }
}

