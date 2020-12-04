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
package esa.restlight.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.DeployContext;
import esa.restlight.core.annotation.Scheduled;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.handler.impl.CompletableFutureRouteExecution;
import esa.restlight.core.handler.impl.DefaultRouteExecution;
import esa.restlight.core.handler.impl.ListenableFutureRouteExecution;
import esa.restlight.core.handler.impl.NettyFutureRouteExecution;
import esa.restlight.core.handler.impl.RouteExecutionFactory;
import esa.restlight.core.handler.impl.RouteHandlerAdapter;
import esa.restlight.core.handler.locate.CompositeMappingLocator;
import esa.restlight.core.handler.locate.CompositeRouteHandlerLocator;
import esa.restlight.core.handler.locate.MappingLocator;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.spi.MappingLocatorFactory;
import esa.restlight.core.spi.RouteHandlerLocatorFactory;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.util.LoggerUtils;
import io.netty.util.concurrent.Future;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static esa.restlight.core.util.InterceptorUtils.filter;

public class RouteUtils {

    public static boolean isConcurrent(InvocableMethod handler) {
        Class<?> type = handler.method().getReturnType();
        return (CompletableFuture.class.isAssignableFrom(type))
                || (FutureUtils.hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(type))
                || (Future.class.isAssignableFrom(type));
    }

    public static String scheduling(InvocableMethod handler) {
        return scheduling(handler, null);
    }

    public static String scheduling(InvocableMethod handler,
                                    String global) {
        Scheduled scheduled = handler.getMethodAnnotation(Scheduled.class);
        if (scheduled == null) {
            scheduled = handler.beanType().getAnnotation(Scheduled.class);
        }
        if (scheduled == null) {
            if (global == null) {
                return Schedulers.BIZ;
            } else {
                return global;
            }
        }
        return scheduled.value();
    }

    /**
     * Choose a implementation of {@link RouteExecution} by the return value type of the given handler, and use the
     * non-asynchronous implementation({@link DefaultRouteExecution}) as the default.
     *
     * @param returnType returnType
     *
     * @return RouteExecutionFactory
     */
    public static RouteExecutionFactory routeExecutionFactory(Class<?> returnType) {
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            return CompletableFutureRouteExecution::new;
        } else if (FutureUtils.hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(returnType)) {
            return ListenableFutureRouteExecution::new;
        } else if (Future.class.isAssignableFrom(returnType)) {
            return NettyFutureRouteExecution::new;
        } else {
            return DefaultRouteExecution::new;
        }
    }

    public static Optional<Route> extractRoute(DeployContext<? extends RestlightOptions> ctx,
                                               Class<?> userType,
                                               Method method,
                                               Object bean) {
        if (!ctx.mappingLocator().isPresent()) {
            return Optional.empty();
        }

        // find RouteMapping by locators
        Optional<Mapping> mapping = ctx.mappingLocator().get().getMapping(userType, method);
        // RouteMapping not found
        if (!mapping.isPresent()) {
            return Optional.empty();
        }
        return extractRoute(ctx, userType, method, bean, mapping.get());

    }

    public static Optional<Route> extractRoute(DeployContext<? extends RestlightOptions> ctx,
                                               Class<?> userType,
                                               Method method,
                                               Object bean,
                                               Mapping mapping) {
        if (mapping == null || !ctx.routeHandlerLocator().isPresent()) {
            return Optional.empty();
        }
        // find RouteHandler by locators
        final Optional<RouteHandler> routeHandler = ctx.routeHandlerLocator()
                .get()
                .getRouteHandler(userType, method, bean);
        // We found a RouteMapping on this method but did not find a RouteHandler.
        if (!routeHandler.isPresent()) {
            LoggerUtils.logger().debug("Found RouteMapping but could not generate RouteHandler for it. " +
                            "userType: {}, method: {}",
                    userType.getName(), method.toString());
            return Optional.empty();
        }

        return extractRoute(ctx, mapping, routeHandler.get());
    }

    public static Optional<Route> extractRoute(DeployContext<? extends RestlightOptions> ctx,
                                               Mapping mapping,
                                               RouteHandler routeHandler) {
        if (mapping == null
                || !ctx.resolverFactory().isPresent()
                || !ctx.exceptionResolverFactory().isPresent()) {
            return Optional.empty();
        }
        final RouteHandlerAdapter handlerAdapter = new RouteHandlerAdapter(routeHandler,
                ctx.resolverFactory().get(),
                filter(ctx, mapping, routeHandler, ctx.interceptors().orElse(Collections.emptyList())),
                ctx.exceptionResolverFactory().get().createResolver(routeHandler));
        Scheduler scheduler = ctx.schedulers().get(handlerAdapter.scheduler());
        Checks.checkNotNull(scheduler,
                "Could not find any scheduler named '" + handlerAdapter.scheduler() + "'");
        final Route route = Route.route(mapping)
                .executionFactory(handlerAdapter::toExecution)
                .schedule(scheduler)
                .handlerObject(handlerAdapter.handler());
        return Optional.of(route);
    }

    public static RouteHandlerLocator loadRouteHandlerLocator(DeployContext<? extends RestlightOptions> ctx) {
        List<RouteHandlerLocatorFactory> factories =
                SpiLoader.cached(RouteHandlerLocatorFactory.class)
                        .getByFeature(ctx.name(),
                                true,
                                Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                false);
        List<RouteHandlerLocator> routeHandlerLocators = factories.stream()
                .map(factory -> factory.locator(ctx))
                .collect(Collectors.toList());
        return CompositeRouteHandlerLocator.wrapIfNecessary(routeHandlerLocators);
    }

    public static MappingLocator loadRouteMappingLocator(DeployContext<? extends RestlightOptions> ctx) {
        List<MappingLocatorFactory> factories =
                SpiLoader.cached(MappingLocatorFactory.class)
                        .getByFeature(ctx.name(),
                                true,
                                Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                false);
        List<MappingLocator> mappingLocators = factories.stream()
                .map(factory -> factory.locator(ctx))
                .collect(Collectors.toList());
        return CompositeMappingLocator.wrapIfNecessary(mappingLocators);
    }
}
