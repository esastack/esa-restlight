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
package esa.restlight.core.handler.impl;

import esa.commons.StringUtils;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.server.schedule.Schedulers;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Default implementation of {@link RouteHandler}.
 */
public class RouteHandlerImpl extends HandlerImpl implements RouteHandler {

    private final boolean intercepted;
    private final String scheduler;

    public RouteHandlerImpl(InvocableMethod handler,
                            boolean intercepted,
                            String scheduler) {
        super(handler);
        this.intercepted = intercepted;
        this.scheduler = StringUtils.nonEmptyOrElse(scheduler, Schedulers.BIZ);
    }

    public RouteHandlerImpl(InvocableMethod handler,
                            HttpResponseStatus customResponse,
                            HandlerInvoker invoker,
                            boolean intercepted,
                            String scheduler) {
        super(handler, customResponse, invoker);
        this.intercepted = intercepted;
        this.scheduler = StringUtils.nonEmptyOrElse(scheduler, Schedulers.BIZ);
    }

    @Override
    public String scheduler() {
        return scheduler;
    }

    @Override
    public boolean intercepted() {
        return intercepted;
    }

    @Override
    public String toString() {
        return StringUtils.concat("{Controller => ", handler().beanType().getName(),
                ", Method => ", handler().method().toGenericString(), "}");
    }


}
