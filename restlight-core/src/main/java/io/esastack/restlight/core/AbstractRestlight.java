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
package io.esastack.restlight.core;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.WritableRestlightHandler;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionResolverFactory;
import io.esastack.restlight.server.BaseRestlightServer;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.bootstrap.LinkedExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.schedule.HandleableRestlightHandler;

/**
 * Abstract implementation for a Restlight server bootstrap. This class allows to set some server-level configurations
 * and the biz-level configurations(in {@link Deployments}) to bootstrap a {@link RestlightServer} which could
 * be {@link #start()} for service.
 * <p>
 *
 * @param <R> type of Restlight
 * @param <D> type of Deployments
 */
public abstract class AbstractRestlight<R extends AbstractRestlight<R, D, O>,
        D extends Deployments<R, D, O>, O extends RestlightOptions> extends BaseRestlightServer<R, D, O> implements
        RestlightServer {

    protected AbstractRestlight(O options) {
        super(options);
    }

    @Override
    protected HandleableRestlightHandler buildHandleable(RestlightHandler handler,
                                                         IExceptionHandler[] exceptionHandlers) {
        ExceptionResolver<Throwable> exceptionResolver = getExceptionResolver();
        final ExceptionHandlerChain handlerChain;
        if (exceptionResolver == null) {
            handlerChain = LinkedExceptionHandlerChain.immutable(exceptionHandlers);
        } else {
            handlerChain = LinkedExceptionHandlerChain.immutable(exceptionHandlers,
                    exceptionResolver::handleException);
        }
        return new WritableRestlightHandler(handler, handlerChain, deployments().deployContext());
    }

    private ExceptionResolver<Throwable> getExceptionResolver() {
        return new DefaultExceptionResolverFactory(deployments().ctx().exceptionMappers().orElse(null))
                .createResolver(null);
    }

}
