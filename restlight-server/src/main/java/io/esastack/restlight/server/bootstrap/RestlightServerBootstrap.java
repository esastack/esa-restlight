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
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.config.ServerOptionsConfigure;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.handler.FilteredHandler;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.internal.FilterContextFactory;
import io.esastack.restlight.server.internal.InternalFilter;
import io.esastack.restlight.server.internal.RequestContextFactory;
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

public class RestlightServerBootstrap<CTX extends RequestContext, FCTX extends FilterContext> {

    private final ServerOptions options;
    private final RestlightHandler<CTX> handler;
    private final RequestContextFactory<CTX> requestContext;
    private final FilterContextFactory<CTX, FCTX> filterContext;
    private final ExceptionHandlerChain<CTX> exceptionHandler;
    private final List<InternalFilter<FCTX>> filters;
    private final Map<ChannelOption<?>, Object> channelOptions = new LinkedHashMap<>();
    private final Map<ChannelOption<?>, Object> childChannelOptions = new LinkedHashMap<>();
    @Beta
    private final List<ChannelHandler> channelHandlers = new LinkedList<>();

    private boolean daemon = true;
    private SocketAddress address;

    private RestlightServerBootstrap(ServerOptions options,
                                     RestlightHandler<CTX> handler,
                                     RequestContextFactory<CTX> requestContext,
                                     FilterContextFactory<CTX, FCTX> filterContext,
                                     List<InternalFilter<FCTX>> filters,
                                     ExceptionHandlerChain<CTX> exceptionHandler) {
        this.options = options;
        this.handler = handler;
        this.requestContext = requestContext;
        this.filterContext = filterContext;
        this.filters = filters;
        this.exceptionHandler = exceptionHandler;
    }

    public static <C extends RequestContext, FC extends FilterContext> RestlightServerBootstrap<C, FC> from(
            RestlightHandler<C> handler, RequestContextFactory<C> requestContext, List<InternalFilter<FC>> filters,
            FilterContextFactory<C, FC> filterContext, ExceptionHandlerChain<C> exceptionHandler) {
        return from(ServerOptionsConfigure.defaultOpts(), handler, requestContext, filterContext,
                filters, exceptionHandler);
    }

    public static <C extends RequestContext, FC extends FilterContext> RestlightServerBootstrap<C, FC> from(
            ServerOptions options, RestlightHandler<C> handler, RequestContextFactory<C> requestContext,
            FilterContextFactory<C, FC> filterContext, List<InternalFilter<FC>> filters,
            ExceptionHandlerChain<C> exceptionHandler) {
        return new RestlightServerBootstrap<>(options, handler, requestContext, filterContext, filters,
                exceptionHandler);
    }

    public RestlightServerBootstrap<CTX, FCTX> withAddress(SocketAddress address) {
        this.address = address;
        return this;
    }

    public RestlightServerBootstrap<CTX, FCTX> withAddress(int port) {
        return withAddress(new InetSocketAddress(port));
    }

    public RestlightServerBootstrap<CTX, FCTX> withAddress(String host, int port) {
        return withAddress(SocketUtils.socketAddress(host, port));
    }

    public RestlightServerBootstrap<CTX, FCTX> withDomainSocketAddress(String path) {
        return withAddress(new DomainSocketAddress(path));
    }

    public RestlightServerBootstrap<CTX, FCTX> daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public RestlightServerBootstrap<CTX, FCTX> withOptions(Map<ChannelOption<?>, Object> options) {
        if (options != null && !options.isEmpty()) {
            this.channelOptions.clear();
            this.channelOptions.putAll(options);
        }
        return this;
    }

    public <T> RestlightServerBootstrap<CTX, FCTX> withOption(ChannelOption<T> option, T value) {
        this.channelOptions.put(option, value);
        return this;
    }

    public RestlightServerBootstrap<CTX, FCTX> withChildOptions(Map<ChannelOption<?>, Object> options) {
        if (options != null && !options.isEmpty()) {
            this.childChannelOptions.clear();
            this.childChannelOptions.putAll(options);
        }
        return this;
    }

    public <T> RestlightServerBootstrap<CTX, FCTX> withChildOption(ChannelOption<T> option, T value) {
        this.childChannelOptions.put(option, value);
        return this;
    }

    @Beta
    public RestlightServerBootstrap<CTX, FCTX> withChannelHandler(ChannelHandler channelHandler) {
        if (channelHandler != null) {
            this.channelHandlers.add(channelHandler);
        }
        return this;
    }

    @Beta
    public RestlightServerBootstrap<CTX, FCTX> withChannelHandlers(ChannelHandler... channelHandlers) {
        if (channelHandlers == null || channelHandlers.length == 0) {
            return this;
        }
        return withChannelHandlers(Arrays.asList(channelHandlers));
    }

    @Beta
    public RestlightServerBootstrap<CTX, FCTX> withChannelHandlers(Collection<? extends ChannelHandler>
                                                                                channelHandlers) {
        if (channelHandlers != null && !channelHandlers.isEmpty()) {
            this.channelHandlers.addAll(channelHandlers);
        }
        return this;
    }

    public RestlightServer forServer() {
        // keep filters in sort
        OrderedComparator.sort(this.filters);
        RestlightHandler<CTX> handler = this.handler;
        if (!this.filters.isEmpty()) {
            handler = new FilteredHandler<>(handler, this.filters, this.filterContext, this.exceptionHandler);
        }
        return new NettyRestlightServer<>(options,
                handler,
                requestContext,
                address,
                daemon,
                channelOptions,
                childChannelOptions,
                channelHandlers);
    }
}
