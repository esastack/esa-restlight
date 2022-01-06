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
package io.esastack.restlight.server.route.impl;

import esa.commons.Checks;
import esa.commons.function.ThrowingBiConsumer;
import esa.commons.function.ThrowingConsumer;
import esa.commons.function.ThrowingRunnable;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.ExecutionFactory;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.schedule.Schedulers;
import io.esastack.restlight.server.util.Futures;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RouteImpl implements Route {

    private final Mapping mapping;
    private final ExecutionFactory executionFactory;
    private final Scheduler scheduler;
    private final Object handler;
    private String str;

    public RouteImpl(Mapping mapping,
                     ExecutionFactory executionFactory,
                     Scheduler scheduler,
                     Object handler) {
        // default to empty mapping
        this.mapping = mapping == null ? Mapping.mapping() : mapping;
        // default to empty StatelessExecutionFactory
        this.executionFactory = executionFactory == null
                ? new StatelessExecutionFactory()
                : executionFactory;
        this.scheduler = scheduler;
        this.handler = handler;
    }

    /**
     * Sets {@link #mapping} to given value.
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl mapping(Mapping mapping) {
        Checks.checkNotNull(mapping, "mapping");
        return new RouteImpl(mapping,
                this.executionFactory,
                this.scheduler,
                this.handler);
    }

    /**
     * Sets the {@link #scheduler} to given value.
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl scheduler(String name, Executor executor) {
        return new RouteImpl(mapping,
                this.executionFactory,
                Schedulers.fromExecutor(name, executor),
                this.handler);
    }

    /**
     * Sets the {@link #scheduler} to given value.
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl scheduler(Scheduler scheduler) {
        return new RouteImpl(mapping,
                this.executionFactory,
                scheduler,
                this.handler);
    }

    /**
     * Sets the {@link #executionFactory} to given value.
     * <p>
     * If there's a {@link StatelessExecutionFactory} value has been set, it will be overridden by given value.
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl executionFactory(ExecutionFactory factory) {
        Checks.checkNotNull(factory, "factory");
        return new RouteImpl(mapping,
                factory,
                this.scheduler,
                this.handler);
    }


    /**
     * Sets {@link StatelessExecutionFactory#requestHandler} to given value. This handler will be used to handle the
     * coming request asynchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl handleAsync(Function<RequestContext, CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        ExecutionFactory factory;
        if (this.executionFactory instanceof StatelessExecutionFactory) {
            final StatelessExecutionFactory old = (StatelessExecutionFactory) this.executionFactory;
            factory = new StatelessExecutionFactory(
                    handler,
                    old.exceptionHandler,
                    old.completionHandler);
        } else {
            factory = new StatelessExecutionFactory(handler, null, null);
        }
        return new RouteImpl(this.mapping,
                factory,
                this.scheduler,
                this.handler);
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #handleAsync(Function)
     */
    public RouteImpl handleAsync(Supplier<CompletionStage<Void>> handler) {
        return handleAsync(((context) -> handler.get()));
    }

    /**
     * Sets {@link StatelessExecutionFactory#requestHandler} to given value. This handler will be used to handle the
     * coming request synchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl handle(ThrowingConsumer<RequestContext> handler) {
        Checks.checkNotNull(handler, "handler");
        return handleAsync((ctx) -> {
            try {
                handler.accept(ctx);
                return Futures.completedFuture();
            } catch (Throwable t) {
                return Futures.completedExceptionally(t);
            }
        });
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #handle(ThrowingConsumer)
     */
    public RouteImpl handle(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return handle((ctx) -> handler.run());
    }

    /**
     * Sets {@link StatelessExecutionFactory#exceptionHandler} to given value. This handler will be used to handle the
     * coming request asynchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl onErrorAsync(ExceptionHandler<Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        ExecutionFactory factory;
        if (this.executionFactory instanceof StatelessExecutionFactory) {
            final StatelessExecutionFactory old = (StatelessExecutionFactory) this.executionFactory;
            factory = new StatelessExecutionFactory(
                    old.requestHandler,
                    handler,
                    old.completionHandler);
        } else {
            factory = new StatelessExecutionFactory(null, handler, null);
        }
        return new RouteImpl(this.mapping,
                factory,
                this.scheduler,
                this.handler);
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onErrorAsync(ExceptionHandler)
     */
    public RouteImpl onErrorAsync(BiFunction<RequestContext, Throwable, CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync((ExceptionHandler<Throwable>) handler::apply);
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onErrorAsync(ExceptionHandler)
     */
    public RouteImpl onErrorAsync(Function<Throwable, CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync((ExceptionHandler<Throwable>) (context, throwable) -> handler.apply(throwable));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onErrorAsync(ExceptionHandler)
     */
    public RouteImpl onErrorAsync(Supplier<CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync((ExceptionHandler<Throwable>) (context, throwable) -> handler.get());
    }

    /**
     * Sets {@link StatelessExecutionFactory#exceptionHandler} to given value. This handler will be used to handle the
     * coming request synchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl onError(ThrowingBiConsumer<RequestContext, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync((ExceptionHandler<Throwable>) (context, throwable) -> {
            try {
                handler.accept(context, throwable);
                return Futures.completedFuture();
            } catch (Throwable t) {
                return Futures.completedExceptionally(t);
            }
        });
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onError(ThrowingBiConsumer)
     */
    public RouteImpl onError(ThrowingConsumer<Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onError((context, throwable) -> handler.accept(throwable));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onError(ThrowingBiConsumer)
     */
    public RouteImpl onError(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return onError((context, throwable) -> handler.run());
    }

    /**
     * Sets {@link StatelessExecutionFactory#completionHandler} to given value. This handler will be used to handle the
     * coming request asynchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl onCompleteAsync(CompletionHandler handler) {
        Checks.checkNotNull(handler, "handler");
        ExecutionFactory factory;
        if (this.executionFactory instanceof StatelessExecutionFactory) {
            final StatelessExecutionFactory old = (StatelessExecutionFactory) this.executionFactory;
            factory = new StatelessExecutionFactory(
                    old.requestHandler,
                    old.exceptionHandler,
                    handler);
        } else {
            factory = new StatelessExecutionFactory(null, null, handler);
        }
        return new RouteImpl(this.mapping,
                factory,
                this.scheduler,
                this.handler);
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onCompleteAsync(CompletionHandler)
     */
    public RouteImpl onCompleteAsync(BiFunction<RequestContext, Throwable, CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((CompletionHandler) handler::apply);
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onCompleteAsync(CompletionHandler)
     */
    public RouteImpl onCompleteAsync(Supplier<CompletionStage<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((CompletionHandler) (context, t) -> handler.get());
    }

    /**
     * Sets {@link StatelessExecutionFactory#completionHandler} to given value. This handler will be used to handle the
     * coming request synchronously.
     * <p>
     * If there's a {@link #executionFactory} value has been set, it will be overridden by the new {@link
     * StatelessExecutionFactory} instantiated by the given request handler.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl onComplete(ThrowingBiConsumer<RequestContext, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((CompletionHandler) (context, t) -> {
            try {
                handler.accept(context, t);
                return Futures.completedFuture();
            } catch (Throwable th) {
                return Futures.completedExceptionally(th);
            }
        });
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onComplete(ThrowingBiConsumer)
     */
    public RouteImpl onComplete(ThrowingConsumer<RequestContext> handler) {
        Checks.checkNotNull(handler, "handler");
        return onComplete(((ctx, throwable) -> handler.accept(ctx)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onComplete(ThrowingBiConsumer)
     */
    public RouteImpl onComplete(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return onComplete(((ctx, throwable) -> handler.run()));
    }

    /**
     * Sets {@link #handler} to given object which will be returned by {@link #handler()}.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl handler(Object handler) {
        return new RouteImpl(this.mapping,
                this.executionFactory,
                this.scheduler,
                handler);
    }

    @Override
    public Mapping mapping() {
        return mapping;
    }

    @Override
    public ExecutionFactory executionFactory() {
        return executionFactory;
    }

    @Override
    public Optional<Object> handler() {
        return Optional.ofNullable(handler);
    }

    @Override
    public String toString() {
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Route(mapping(").append(mapping).append(")");
            if (scheduler != null) {
                sb.append(",scheduler(").append(scheduler.name()).append(')');
            }
            handler().ifPresent(h -> sb.append(",handler(").append(handler).append(")"));
            sb.append(")");
            str = sb.toString();
        }
        return str;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    private static class StatelessExecutionFactory implements ExecutionFactory {

        final Function<RequestContext, CompletionStage<Void>> requestHandler;
        final ExceptionHandler<Throwable> exceptionHandler;
        final CompletionHandler completionHandler;

        private StatelessExecutionFactory() {
            this(null, null, null);
        }

        private StatelessExecutionFactory(Function<RequestContext, CompletionStage<Void>> requestHandler,
                                          ExceptionHandler<Throwable> exceptionHandler,
                                          CompletionHandler completionHandler) {
            // default to do nothing in request handler.
            this.requestHandler = requestHandler == null
                    ? (ctx) -> Futures.completedFuture()
                    : requestHandler;
            this.exceptionHandler = exceptionHandler;
            this.completionHandler = completionHandler;
        }

        @Override
        public RouteExecution create(RequestContext ctx) {
            return new RouteExecution() {
                @Override
                public CompletionStage<Void> handle(RequestContext context) {
                    if (requestHandler == null) {
                        return Futures.completedFuture();
                    }
                    return requestHandler.apply(context);
                }

                @Override
                public ExceptionHandler<Throwable> exceptionHandler() {
                    return exceptionHandler;
                }

                @Override
                public CompletionHandler completionHandler() {
                    return completionHandler;
                }
            };
        }
    }

}

