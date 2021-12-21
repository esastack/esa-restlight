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
package io.esastack.restlight.server.bootstrap;

import esa.commons.annotation.Beta;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.config.ServerOptionsConfigure;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.FilteredHandler;
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

public class RestlightServerBootstrap {

    private final ServerOptions options;
    private final RestlightHandler handler;
    private final ExceptionHandlerChain exceptionHandler;
    private final List<Filter> filters;
    private final Map<ChannelOption<?>, Object> channelOptions = new LinkedHashMap<>();
    private final Map<ChannelOption<?>, Object> childChannelOptions = new LinkedHashMap<>();
    @Beta
    private final List<ChannelHandler> channelHandlers = new LinkedList<>();

    private boolean daemon = true;
    private SocketAddress address;

    private RestlightServerBootstrap(ServerOptions options,
                                     RestlightHandler handler,
                                     List<Filter> filters,
                                     ExceptionHandlerChain exceptionHandler) {
        this.options = options;
        this.handler = handler;
        this.filters = filters;
        this.exceptionHandler = exceptionHandler;
    }

    public static RestlightServerBootstrap from(RestlightHandler handler, List<Filter> filters,
                                                ExceptionHandlerChain exceptionHandler) {
        return from(ServerOptionsConfigure.defaultOpts(), handler, filters, exceptionHandler);
    }

    public static RestlightServerBootstrap from(
            ServerOptions options, RestlightHandler handler, List<Filter> filters,
            ExceptionHandlerChain exceptionHandler) {
        return new RestlightServerBootstrap(options, handler, filters,
                exceptionHandler);
    }

    public RestlightServerBootstrap withAddress(SocketAddress address) {
        this.address = address;
        return this;
    }

    public RestlightServerBootstrap withAddress(int port) {
        return withAddress(new InetSocketAddress(port));
    }

    public RestlightServerBootstrap withAddress(String host, int port) {
        return withAddress(SocketUtils.socketAddress(host, port));
    }

    public RestlightServerBootstrap withDomainSocketAddress(String path) {
        return withAddress(new DomainSocketAddress(path));
    }

    public RestlightServerBootstrap daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public RestlightServerBootstrap withOptions(Map<ChannelOption<?>, Object> options) {
        if (options != null && !options.isEmpty()) {
            this.channelOptions.clear();
            this.channelOptions.putAll(options);
        }
        return this;
    }

    public <T> RestlightServerBootstrap withOption(ChannelOption<T> option, T value) {
        this.channelOptions.put(option, value);
        return this;
    }

    public RestlightServerBootstrap withChildOptions(Map<ChannelOption<?>, Object> options) {
        if (options != null && !options.isEmpty()) {
            this.childChannelOptions.clear();
            this.childChannelOptions.putAll(options);
        }
        return this;
    }

    public <T> RestlightServerBootstrap withChildOption(ChannelOption<T> option, T value) {
        this.childChannelOptions.put(option, value);
        return this;
    }

    @Beta
    public RestlightServerBootstrap withChannelHandler(ChannelHandler channelHandler) {
        if (channelHandler != null) {
            this.channelHandlers.add(channelHandler);
        }
        return this;
    }

    @Beta
    public RestlightServerBootstrap withChannelHandlers(ChannelHandler... channelHandlers) {
        if (channelHandlers == null || channelHandlers.length == 0) {
            return this;
        }
        return withChannelHandlers(Arrays.asList(channelHandlers));
    }

    @Beta
    public RestlightServerBootstrap withChannelHandlers(Collection<? extends ChannelHandler> channelHandlers) {
        if (channelHandlers != null && !channelHandlers.isEmpty()) {
            this.channelHandlers.addAll(channelHandlers);
        }
        return this;
    }

    public RestlightServer forServer() {
        // keep filters in sort
        OrderedComparator.sort(this.filters);
        RestlightHandler handler = this.handler;
        if (!this.filters.isEmpty()) {
            handler = new FilteredHandler(handler, this.filters, this.exceptionHandler);
        }
        return new NettyRestlightServer(options,
                handler,
                address,
                daemon,
                channelOptions,
                childChannelOptions,
                channelHandlers);
    }
}
