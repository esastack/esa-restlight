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
package esa.restlight.server.route.impl;

import esa.commons.Checks;
import esa.commons.function.ThrowingBiConsumer;
import esa.commons.function.ThrowingConsumer;
import esa.commons.function.ThrowingConsumer3;
import esa.commons.function.ThrowingRunnable;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.route.CompletionHandler;
import esa.restlight.server.route.ExceptionHandler;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.schedule.Scheduling;
import esa.restlight.server.util.Futures;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple implementation of {@link Route} which holds a {@link Mapping}, {@link Scheduling#scheduler()} and {@link
 * #executionFactory} to provides the method implementation of {@link Route}.
 */
public class RouteImpl implements Route {

    private final Mapping mapping;
    private final Function<AsyncRequest, RouteExecution> executionFactory;
    private final Scheduler scheduler;
    private final Object handler;
    private String str;

    public RouteImpl(Mapping mapping,
                     Function<AsyncRequest, RouteExecution> executionFactory,
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
    public RouteImpl schedule(String name, Executor executor) {
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
    public RouteImpl schedule(Scheduler scheduler) {
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
    public RouteImpl executionFactory(Function<AsyncRequest, RouteExecution> factory) {
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
    public RouteImpl handleAsync(BiFunction<AsyncRequest, AsyncResponse, CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        Function<AsyncRequest, RouteExecution> factory;
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
     * @see #handleAsync(BiFunction)
     */
    public RouteImpl handleAsync(Function<AsyncRequest, CompletableFuture<Void>> handler) {
        return handleAsync(((request, response) -> handler.apply(request)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #handleAsync(BiFunction)
     */
    public RouteImpl handleAsync(Supplier<CompletableFuture<Void>> handler) {
        return handleAsync(((request, response) -> handler.get()));
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
    public RouteImpl handle(ThrowingBiConsumer<AsyncRequest, AsyncResponse> handler) {
        Checks.checkNotNull(handler, "handler");
        return handleAsync((request, response) -> {
            try {
                handler.accept(request, response);
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
     * @see #handle(ThrowingBiConsumer)
     */
    public RouteImpl handle(ThrowingConsumer<AsyncRequest> handler) {
        Checks.checkNotNull(handler, "handler");
        return handle((request, response) -> handler.accept(request));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #handle(ThrowingBiConsumer)
     */
    public RouteImpl handle(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return handle((request, response) -> handler.run());
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
        Function<AsyncRequest, RouteExecution> factory;
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
    public RouteImpl onErrorAsync(BiFunction<AsyncRequest, Throwable, CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync(((request, response, throwable) -> handler.apply(request, throwable)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onErrorAsync(ExceptionHandler)
     */
    public RouteImpl onErrorAsync(Function<Throwable, CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync(((request, response, throwable) -> handler.apply(throwable)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onErrorAsync(ExceptionHandler)
     */
    public RouteImpl onErrorAsync(Supplier<CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync(((request, response, throwable) -> handler.get()));
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
    public RouteImpl onError(ThrowingConsumer3<AsyncRequest, AsyncResponse, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onErrorAsync((request, response, error) -> {
            try {
                handler.accept(request, response, error);
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
     * @see #onError(ThrowingConsumer3)
     */
    public RouteImpl onError(ThrowingBiConsumer<AsyncRequest, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onError(((request, response, throwable) -> handler.accept(request, throwable)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onError(ThrowingConsumer3)
     */
    public RouteImpl onError(ThrowingConsumer<Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onError(((request, response, throwable) -> handler.accept(throwable)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onError(ThrowingConsumer3)
     */
    public RouteImpl onError(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return onError(((request, response, throwable) -> handler.run()));
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
        Function<AsyncRequest, RouteExecution> factory;
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
    public RouteImpl onCompleteAsync(BiFunction<AsyncRequest, Throwable, CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((request, response, t) -> handler.apply(request, t));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onCompleteAsync(CompletionHandler)
     */
    public RouteImpl onCompleteAsync(Function<AsyncResponse, CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((request, response, t) -> handler.apply(response));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onCompleteAsync(CompletionHandler)
     */
    public RouteImpl onCompleteAsync(Supplier<CompletableFuture<Void>> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((request, response, t) -> handler.get());
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
    public RouteImpl onComplete(ThrowingConsumer3<AsyncRequest, AsyncResponse, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onCompleteAsync((request, response, error) -> {
            try {
                handler.accept(request, response, error);
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
     * @see #onComplete(ThrowingConsumer3)
     */
    public RouteImpl onComplete(ThrowingBiConsumer<AsyncResponse, Throwable> handler) {
        Checks.checkNotNull(handler, "handler");
        return onComplete(((request, response, throwable) -> handler.accept(response, throwable)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onComplete(ThrowingConsumer3)
     */
    public RouteImpl onComplete(ThrowingConsumer<AsyncResponse> handler) {
        Checks.checkNotNull(handler, "handler");
        return onComplete(((request, response, throwable) -> handler.accept(response)));
    }

    /**
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     * @see #onComplete(ThrowingConsumer3)
     */
    public RouteImpl onComplete(ThrowingRunnable handler) {
        Checks.checkNotNull(handler, "handler");
        return onComplete(((request, response, throwable) -> handler.run()));
    }

    /**
     * Sets {@link #handler} to given object which will be returned by {@link #handler()}.
     *
     * @param handler handler
     *
     * @return a new instance of {@link RouteImpl}
     */
    public RouteImpl handlerObject(Object handler) {
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
    public RouteExecution toExecution(AsyncRequest request) {
        return executionFactory.apply(request);
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

    private static class StatelessExecutionFactory implements Function<AsyncRequest, RouteExecution> {

        final BiFunction<AsyncRequest, AsyncResponse, CompletableFuture<Void>> requestHandler;
        final ExceptionHandler<Throwable> exceptionHandler;
        final CompletionHandler completionHandler;

        private StatelessExecutionFactory() {
            this(null, null, null);
        }

        private StatelessExecutionFactory(BiFunction<AsyncRequest,
                AsyncResponse, CompletableFuture<Void>> requestHandler,
                                          ExceptionHandler<Throwable> exceptionHandler,
                                          CompletionHandler completionHandler) {
            // default to do nothing in request handler.
            this.requestHandler = requestHandler == null
                    ? (request, response) -> Futures.completedFuture()
                    : requestHandler;
            this.exceptionHandler = exceptionHandler;
            this.completionHandler = completionHandler;
        }

        @Override
        public RouteExecution apply(AsyncRequest request) {
            return new RouteExecution() {
                @Override
                public CompletableFuture<Void> handle(AsyncRequest request, AsyncResponse response) {
                    if (requestHandler == null) {
                        return Futures.completedFuture();
                    }
                    return requestHandler.apply(request, response);
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
