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

import esa.restlight.core.handler.*;
import esa.restlight.core.handler.impl.HandlerImpl;
import esa.restlight.core.handler.impl.HandlerInvokerImpl;
import esa.restlight.core.handler.impl.RouteHandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.util.InterceptorUtils;
import esa.restlight.core.util.RouteUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;
import java.util.Optional;

public abstract class AbstractRouteHandlerLocator implements RouteHandlerLocator {

    private final String globalScheduling;
    private final HandlerAdvicesFactory handlerAdvicesFactory;

    public AbstractRouteHandlerLocator(String globalScheduling,
                                       HandlerAdvicesFactory handlerAdvicesFactory) {
        this.globalScheduling = globalScheduling;
        this.handlerAdvicesFactory = handlerAdvicesFactory;
    }

    @Override
    public Optional<RouteHandler> getRouteHandler(Class<?> userType, Method method, Object bean) {
        final InvocableMethod handlerMethod = getHandlerMethod(userType, method, bean);
        HandlerInvoker invoker = new HandlerInvokerImpl(handlerMethod);
        if (handlerAdvicesFactory != null) {
            HandlerAdvice[] handlerAdvices = handlerAdvicesFactory.getHandlerAdvices(new HandlerImpl(handlerMethod,
                    this.getCustomResponse(handlerMethod),
                    invoker));
            if (handlerAdvices != null && handlerAdvices.length > 0) {
                invoker = LinkedHandlerInvoker.immutable(handlerAdvices, invoker);
            }
        }
        return Optional.of(new RouteHandlerImpl(handlerMethod,
                this.getCustomResponse(handlerMethod),
                invoker,
                InterceptorUtils.isIntercepted(handlerMethod),
                RouteUtils.scheduling(handlerMethod, globalScheduling)));
    }

    protected HandlerMethod getHandlerMethod(Class<?> userType, Method method, Object bean) {
        return HandlerMethod.of(userType, method, bean);
    }

    /**
     * Get custom response
     *
     * @param handlerMethod InvocableMethod
     * @return HttpResponseStatus
     */
    protected abstract HttpResponseStatus getCustomResponse(InvocableMethod handlerMethod);
}
