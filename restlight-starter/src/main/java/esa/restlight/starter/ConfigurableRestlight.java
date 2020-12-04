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
package esa.restlight.starter;

import esa.commons.Checks;
import esa.commons.annotation.Beta;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.server.handler.Filter;
import esa.restlight.spring.Deployments4Spring;
import esa.restlight.spring.Restlight4Spring;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;

public class ConfigurableRestlight {

    private final Restlight4Spring restlight;
    private final RestlightOptions options;
    SocketAddress address;

    ConfigurableRestlight(Restlight4Spring restlight,
                          RestlightOptions options) {
        Checks.checkNotNull(restlight, "restlight");
        Checks.checkNotNull(options, "options");
        this.options = options;
        this.restlight = restlight;
    }

    /**
     * @see RestlightOptions
     */
    public RestlightOptions options() {
        return options;
    }

    /**
     * @see Restlight4Spring#enableServerAware(boolean)
     */
    public ConfigurableRestlight enableServerAware(boolean enable) {
        restlight.enableServerAware(enable);
        return this;
    }

    /**
     * @see Restlight4Spring#enableIoExecutorAware(boolean)
     */
    public ConfigurableRestlight enableIoExecutorAware(boolean enable) {
        restlight.enableIoExecutorAware(enable);
        return this;
    }

    /**
     * @see Restlight4Spring#enableBizExecutorAware(boolean)
     */
    public ConfigurableRestlight enableBizExecutorAware(boolean enable) {
        restlight.enableBizExecutorAware(enable);
        return this;
    }

    /**
     * @see Restlight4Spring#address(SocketAddress)
     */
    public ConfigurableRestlight address(SocketAddress localAddress) {
        this.address = localAddress;
        return this;
    }

    /**
     * @see Restlight4Spring#address(int)
     */
    public ConfigurableRestlight address(int port) {
        return address(new InetSocketAddress(port));
    }

    /**
     * @see Restlight4Spring#address(String, int)
     */
    public ConfigurableRestlight address(String host, int port) {
        return address(SocketUtils.socketAddress(host, port));
    }

    /**
     * @see Restlight4Spring#domainSocketAddress(String)
     */
    public ConfigurableRestlight domainSocketAddress(String path) {
        return address(new DomainSocketAddress(path));
    }

    /**
     * @see Restlight4Spring#addFilter(Filter)
     */
    public ConfigurableRestlight addFilter(Filter filter) {
        restlight.addFilter(filter);
        return this;
    }

    /**
     * @see Restlight4Spring#addFilters(Collection)
     */
    public ConfigurableRestlight addFilters(Collection<? extends Filter> filters) {
        restlight.addFilters(filters);
        return this;
    }

    /**
     * @see Restlight4Spring#daemon(boolean)
     */
    public ConfigurableRestlight daemon(boolean daemon) {
        restlight.daemon(daemon);
        return this;
    }

    /**
     * @see Restlight4Spring#options(Map)
     */
    public ConfigurableRestlight options(Map<ChannelOption<?>, Object> options) {
        restlight.options(options);
        return this;
    }

    /**
     * @see Restlight4Spring#option(ChannelOption, Object)
     */
    public <T> ConfigurableRestlight option(ChannelOption<T> option, T value) {
        restlight.option(option, value);
        return this;
    }

    /**
     * @see Restlight4Spring#childOptions(Map)
     */
    public ConfigurableRestlight childOptions(Map<ChannelOption<?>, Object> options) {
        restlight.childOptions(options);
        return this;
    }

    /**
     * @see Restlight4Spring#childOption(ChannelOption, Object)
     */
    public <T> ConfigurableRestlight childOption(ChannelOption<T> option, T value) {
        restlight.childOption(option, value);
        return this;
    }

    @Beta
    public ConfigurableRestlight channelHandler(ChannelHandler channelHandler) {
        restlight.channelHandler(channelHandler);
        return this;
    }

    @Beta
    public ConfigurableRestlight channelHandlers(ChannelHandler... channelHandlers) {
        restlight.channelHandlers(channelHandlers);
        return this;
    }

    @Beta
    public ConfigurableRestlight channelHandlers(Collection<? extends ChannelHandler> channelHandlers) {
        restlight.channelHandlers(channelHandlers);
        return this;
    }

    /**
     * @see Restlight4Spring#deployments()
     */
    public Deployments4Spring.Impl deployments() {
        return restlight.deployments();
    }
}
