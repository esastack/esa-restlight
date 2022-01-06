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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.DelegatingDeployContext;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.RouterRegistries;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.server.bootstrap.DispatcherHandlerImpl;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.route.Router;
import io.esastack.restlight.server.route.impl.AbstractRouteRegistry;
import io.esastack.restlight.server.route.impl.SimpleRouteRegistry;
import io.esastack.restlight.server.util.Futures;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HandlerLocatorResolver implements HandlerValueResolver {

    private final DeployContext<? extends RestlightOptions> deployContext;
    private final HandlerMapping handlerMapping;
    private final RouterRegistries registries;

    public HandlerLocatorResolver(DeployContext<? extends RestlightOptions> deployContext,
                                  HandlerMapping handlerMapping,
                                  RouterRegistries registries) {
        Checks.checkNotNull(deployContext, "deployContext");
        Checks.checkNotNull(handlerMapping, "handlerMapping");
        Checks.checkNotNull(registries, "registries");
        this.deployContext = deployContext;
        this.handlerMapping = handlerMapping;
        this.registries = registries;
    }

    @Override
    public CompletionStage<Void> handle(Object value, RequestContext context) {
        if (value == null) {
            return Futures.completedExceptionally(new WebServerException("Unexpected 'null' returned by" +
                    " resource locator: [" + handlerMapping.methodInfo().handlerMethod() + "]"));
        }

        final Class<?> userType = ClassUtils.getUserType(value);
        final Router router = registries.getOrCompute(userType, (clazz) -> {
            final AbstractRouteRegistry registry = new SimpleRouteRegistry();
            ClassUtils.doWithUserDeclaredMethods(userType,
                    method -> {
                        HandlerContext<?> ctx = HandlerContext.build(lookupParentRecursively(deployContext),
                                HandlerMethodImpl.of(userType, method));
                        RouteUtils.extractHandlerMapping(ctx, handlerMapping,
                                extractBean(value), userType, method)
                                .flatMap(mapping -> RouteUtils.extractRoute(ctx, mapping))
                                .ifPresent(registry::register);
                    },
                    RouteUtils::isHandlerMethod);
            return registry;
        });

        final Route route = router.route(context);
        if (route == null) {
            return Futures.completedExceptionally(new RouteFailureException(context,
                    DispatcherHandlerImpl.notFound(context)));
        }

        final RouteExecution execution;
        try {
            execution = route.executionFactory().create(context);
        } catch (Throwable th) {
            return Futures.completedExceptionally(th);
        }

        final CompletionStage<Void> promise = new CompletableFuture<>();
        try {
            return execution.handle(context).whenComplete((v, th) -> {
                if (th != null && execution.exceptionHandler() != null) {
                    execution.exceptionHandler().handleException(context, th)
                            .whenComplete((v0, th0) -> {
                                complete(context, execution.completionHandler(), th0, promise);
                            });
                } else {
                    complete(context, execution.completionHandler(), null, promise);
                }
            });
        } catch (Throwable th) {
            complete(context, execution.completionHandler(), th, promise);
        }

        return promise;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void complete(RequestContext context, CompletionHandler completionHandler,
                          Throwable th, CompletionStage<Void> promise) {
        if (completionHandler != null) {
            completionHandler.onComplete(context, th);
        }
        if (th != null) {
            promise.toCompletableFuture().completeExceptionally(th);
        } else {
            promise.toCompletableFuture().complete(null);
        }
    }

    private static Object extractBean(Object value) {
        return value instanceof Type ? null : value;
    }

    private static <O extends RestlightOptions> DeployContext<O> lookupParentRecursively(DeployContext<O> current) {
        if (current instanceof DelegatingDeployContext) {
            return lookupParentRecursively(((DelegatingDeployContext<O>) current).unwrap());
        } else {
            return current;
        }
    }

}

