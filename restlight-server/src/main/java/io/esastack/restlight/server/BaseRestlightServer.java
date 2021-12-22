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
package io.esastack.restlight.server;

import esa.commons.Checks;
import esa.commons.annotation.Beta;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.bootstrap.AbstractDelegatedRestlightServer;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.bootstrap.RestlightServerBootstrap;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseRestlightServer<R extends BaseRestlightServer<R, D, O>, D extends
        BaseDeployments<R, D, O>, O extends ServerOptions> extends AbstractDelegatedRestlightServer
        implements RestlightServer {
    /**
     * Server options
     */
    protected final O options;
    boolean daemon = true;
    private final Map<ChannelOption<?>, Object> channelOptions = new LinkedHashMap<>();
    private final Map<ChannelOption<?>, Object> childChannelOptions = new LinkedHashMap<>();
    @Beta
    private final List<ChannelHandler> channelHandlers = new LinkedList<>();
    private D deployments;
    private String name = Constants.SERVER;
    /**
     * local server address to bind.
     */
    private SocketAddress address;

    private final AtomicBoolean immutable = new AtomicBoolean(false);

    protected BaseRestlightServer(O options) {
        Checks.checkNotNull(options, "options");
        this.options = options;
    }

    public R name(String name) {
        checkImmutable();
        Checks.checkNotEmptyArg(name, "name");
        this.name = name;
        return self();
    }

    public R address(SocketAddress localAddress) {
        checkImmutable();
        this.address = localAddress;
        return self();
    }

    /**
     * @see #address(SocketAddress)
     */
    public R address(int port) {
        return address(new InetSocketAddress(port));
    }

    /**
     * @see #address(SocketAddress)
     */
    public R address(String host, int port) {
        return address(SocketUtils.socketAddress(host, port));
    }

    /**
     * @see #address(SocketAddress)
     */
    public R domainSocketAddress(String path) {
        return address(new DomainSocketAddress(path));
    }

    /**
     * Makes server start on daemon threads.
     *
     * @param daemon daemon
     * @return this
     */
    public R daemon(boolean daemon) {
        checkImmutable();
        this.daemon = daemon;
        return self();
    }

    public R options(Map<ChannelOption<?>, Object> options) {
        checkImmutable();
        if (options != null && !options.isEmpty()) {
            this.channelOptions.clear();
            this.channelOptions.putAll(options);
        }
        return self();
    }

    public <T> R option(ChannelOption<T> option, T value) {
        checkImmutable();
        this.channelOptions.put(option, value);
        return self();
    }

    public R childOptions(Map<ChannelOption<?>, Object> options) {
        checkImmutable();
        if (options != null && !options.isEmpty()) {
            this.childChannelOptions.clear();
            this.childChannelOptions.putAll(options);
        }
        return self();
    }

    public <T> R childOption(ChannelOption<T> option, T value) {
        checkImmutable();
        this.childChannelOptions.put(option, value);
        return self();
    }

    @Beta
    public R channelHandler(ChannelHandler channelHandler) {
        checkImmutable();
        if (channelHandler != null) {
            this.channelHandlers.add(channelHandler);
        }
        return self();
    }

    @Beta
    public R channelHandlers(ChannelHandler... channelHandlers) {
        checkImmutable();
        if (channelHandlers == null || channelHandlers.length == 0) {
            return self();
        }
        return channelHandlers(Arrays.asList(channelHandlers));
    }

    @Beta
    public R channelHandlers(Collection<? extends ChannelHandler> channelHandlers) {
        checkImmutable();
        if (channelHandlers != null && !channelHandlers.isEmpty()) {
            this.channelHandlers.addAll(channelHandlers);
        }
        return self();
    }

    /**
     * Gets {@link D} for deployments configuration.
     */
    public synchronized D deployments() {
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

    @SuppressWarnings("unchecked")
    protected R self() {
        return (R) this;
    }

    private RestlightServer buildServer() {
        RestlightHandler handler = deployments().applyDeployments();
        List<Filter> fs = new LinkedList<>(deployments().filters());
        return doBuildServer(handler, fs);
    }

    protected RestlightServer doBuildServer(RestlightHandler handler, List<Filter> fs) {
        return RestlightServerBootstrap.from(options, handler, fs)
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

    protected abstract D createDeployments();

}
