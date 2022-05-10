/*
 * Copyright 2022 OPPO ESA Stack Project
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

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.annotation.Beta;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.dispatcher.ExceptionHandlerChain;
import io.esastack.restlight.core.dispatcher.IExceptionHandler;
import io.esastack.restlight.core.dispatcher.LinkedExceptionHandlerChain;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.server.processor.RestlightHandlerImpl;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionResolverFactory;
import io.esastack.restlight.core.spi.ResponseEntityChannelFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.core.server.*;
import io.esastack.restlight.core.filter.Filter;
import io.esastack.restlight.core.server.processor.FilteredHandler;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.server.processor.AbstractRestlightHandler;
import io.esastack.restlight.core.spi.FilterFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract implementation for a Restlight server bootstrap. This class allows to set some server-level configurations
 * and the biz-level configurations(in {@link Deployments}) to bootstrap a {@link RestlightServer} which could
 * be {@link #start()} for service.
 */
public abstract class AbstractRestlight extends AbstractDelegatedRestlightServer
        implements RestlightServer {

    /**
     * Server options
     */
    protected final RestlightOptions options;
    boolean daemon = true;
    private final Map<ChannelOption<?>, Object> channelOptions = new LinkedHashMap<>();
    private final Map<ChannelOption<?>, Object> childChannelOptions = new LinkedHashMap<>();
    @Beta
    private final List<ChannelHandler> channelHandlers = new LinkedList<>();
    private Deployments deployments;
    private String name = Constants.SERVER;
    /**
     * local server address to bind.
     */
    private SocketAddress address;

    private final AtomicBoolean immutable = new AtomicBoolean(false);

    protected AbstractRestlight(RestlightOptions options) {
        Checks.checkNotNull(options, "options");
        this.options = options;
    }

    public AbstractRestlight name(String name) {
        checkImmutable();
        Checks.checkNotEmptyArg(name, "name");
        this.name = name;
        return self();
    }

    public AbstractRestlight address(SocketAddress localAddress) {
        checkImmutable();
        this.address = localAddress;
        return self();
    }

    /**
     * @see #address(SocketAddress)
     */
    public AbstractRestlight address(int port) {
        return address(new InetSocketAddress(port));
    }

    /**
     * @see #address(SocketAddress)
     */
    public AbstractRestlight address(String host, int port) {
        return address(SocketUtils.socketAddress(host, port));
    }

    /**
     * @see #address(SocketAddress)
     */
    public AbstractRestlight domainSocketAddress(String path) {
        return address(new DomainSocketAddress(path));
    }

    /**
     * Makes server start on daemon threads.
     *
     * @param daemon daemon
     * @return this
     */
    public AbstractRestlight daemon(boolean daemon) {
        checkImmutable();
        this.daemon = daemon;
        return self();
    }

    public AbstractRestlight options(Map<ChannelOption<?>, Object> options) {
        checkImmutable();
        if (options != null && !options.isEmpty()) {
            this.channelOptions.clear();
            this.channelOptions.putAll(options);
        }
        return self();
    }

    public <T> AbstractRestlight option(ChannelOption<T> option, T value) {
        checkImmutable();
        this.channelOptions.put(option, value);
        return self();
    }

    public AbstractRestlight childOptions(Map<ChannelOption<?>, Object> options) {
        checkImmutable();
        if (options != null && !options.isEmpty()) {
            this.childChannelOptions.clear();
            this.childChannelOptions.putAll(options);
        }
        return self();
    }

    public <T> AbstractRestlight childOption(ChannelOption<T> option, T value) {
        checkImmutable();
        this.childChannelOptions.put(option, value);
        return self();
    }

    @Beta
    public AbstractRestlight channelHandler(ChannelHandler channelHandler) {
        checkImmutable();
        if (channelHandler != null) {
            this.channelHandlers.add(channelHandler);
        }
        return self();
    }

    @Beta
    public AbstractRestlight channelHandlers(ChannelHandler... channelHandlers) {
        checkImmutable();
        if (channelHandlers == null || channelHandlers.length == 0) {
            return self();
        }
        return channelHandlers(Arrays.asList(channelHandlers));
    }

    @Beta
    public AbstractRestlight channelHandlers(Collection<? extends ChannelHandler> channelHandlers) {
        checkImmutable();
        if (channelHandlers != null && !channelHandlers.isEmpty()) {
            this.channelHandlers.addAll(channelHandlers);
        }
        return self();
    }

    /**
     * Gets {@link Deployments} for deployments configuration.
     */
    public synchronized Deployments deployments() {
        if (deployments == null) {
            deployments = createDeployments();
        }
        return deployments;
    }

    public String name() {
        return name;
    }

    @Override
    public synchronized void start() {
        checkImmutable();
        this.preStart();
        if (getServer() == null) {
            setServer(buildServer());
        }
        super.start();
        this.postStart(getServer());
        this.immutable.set(true);
    }

    protected void preStart() {
    }

    protected void postStart(RestlightServer server) {
    }

    protected AbstractRestlight self() {
        return this;
    }

    private RestlightServer buildServer() {
        RestlightHandler handler = deployments().applyDeployments();
        List<Filter> filters = new LinkedList<>(deployments().filters());
        filters.addAll(loadFiltersBySpi());
        if (!filters.isEmpty()) {
            // keep filters in sort
            OrderedComparator.sort(filters);
            handler = new FilteredHandler(handler, filters);
        }
        return doBuildServer(buildHandler(handler, deployments().exceptionHandlers));
    }

    protected RestlightServer doBuildServer(RestlightHandler handler) {
        return RestlightServerBootstrap.from(options, handler)
                .withAddress(address)
                .withOptions(channelOptions)
                .withChildOptions(childChannelOptions)
                .withChannelHandlers(channelHandlers)
                .daemon(daemon)
                .forServer();
    }

    protected void checkImmutable() {
        if (immutable.get()) {
            throw new IllegalStateException("Illegal operation, server has been immutable.");
        }
    }

    private List<Filter> loadFiltersBySpi() {
        List<Filter> filters = new LinkedList<>(SpiLoader.cached(Filter.class)
                .getByGroup(deployments().restlight.name(), true));
        SpiLoader.cached(FilterFactory.class)
                .getByGroup(deployments().restlight.name(), true)
                .forEach(factory -> factory.filter(deployments.ctx()).ifPresent(filters::add));
        return filters;
    }

    /**
     * Builds an {@link AbstractRestlightHandler} by given {@link RestlightHandler} and
     * {@link IExceptionHandler}s.
     *
     * @param handler           handler
     * @param exceptionHandlers exception handlers
     * @return handler which can handle exception.
     */
    protected AbstractRestlightHandler buildHandler(RestlightHandler handler,
                                                    IExceptionHandler[] exceptionHandlers) {
        ExceptionResolver<Throwable> exceptionResolver = getExceptionResolver();
        final ExceptionHandlerChain handlerChain;
        if (exceptionResolver == null) {
            handlerChain = LinkedExceptionHandlerChain.immutable(exceptionHandlers);
        } else {
            handlerChain = LinkedExceptionHandlerChain.immutable(exceptionHandlers,
                    exceptionResolver::handleException);
        }
        return new RestlightHandlerImpl(handler, handlerChain, extractChannelFactory(),
                deployments().deployContext());
    }

    private ResponseEntityChannelFactory extractChannelFactory() {
        List<ResponseEntityChannelFactory> factories = SpiLoader.cached(ResponseEntityChannelFactory.class)
                .getByFeature(deployments().server().name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        Checks.checkNotEmptyArg(factories, "factories must not be empty");
        OrderedComparator.sort(factories);
        return factories.get(0);
    }

    private ExceptionResolver<Throwable> getExceptionResolver() {
        return new DefaultExceptionResolverFactory(deployments().ctx().exceptionMappers().orElse(null))
                .createResolver(null);
    }

    protected abstract Deployments createDeployments();

}
