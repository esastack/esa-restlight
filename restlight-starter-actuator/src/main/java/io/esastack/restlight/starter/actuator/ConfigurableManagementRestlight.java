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
package io.esastack.restlight.starter.actuator;

import esa.commons.Checks;
import esa.commons.annotation.Beta;
import io.esastack.restlight.core.Deployments;
import io.esastack.restlight.core.Restlight;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.spring.Restlight4Spring;
import io.esastack.restlight.starter.actuator.autoconfigurer.ManagementOptions;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;

public class ConfigurableManagementRestlight {

    private final Restlight restlight;
    private final ManagementOptions options;
    SocketAddress address;

    ConfigurableManagementRestlight(Restlight restlight,
                                    ManagementOptions options) {
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
     * @see Restlight4Spring#address(SocketAddress)
     */
    public ConfigurableManagementRestlight address(SocketAddress localAddress) {
        this.address = localAddress;
        return this;
    }

    /**
     * @see Restlight4Spring#address(int)
     */
    public ConfigurableManagementRestlight address(int port) {
        return address(new InetSocketAddress(port));
    }

    /**
     * @see Restlight4Spring#address(String, int)
     */
    public ConfigurableManagementRestlight address(String host, int port) {
        return address(SocketUtils.socketAddress(host, port));
    }

    /**
     * @see Restlight4Spring#domainSocketAddress(String)
     */
    public ConfigurableManagementRestlight domainSocketAddress(String path) {
        return address(new DomainSocketAddress(path));
    }

    /**
     * @see Restlight4Spring#daemon(boolean)
     */
    public ConfigurableManagementRestlight daemon(boolean daemon) {
        restlight.daemon(daemon);
        return this;
    }

    /**
     * @see Restlight4Spring#options(Map)
     */
    public ConfigurableManagementRestlight options(Map<ChannelOption<?>, Object> options) {
        restlight.options(options);
        return this;
    }

    /**
     * @see Restlight4Spring#option(ChannelOption, Object)
     */
    public <T> ConfigurableManagementRestlight option(ChannelOption<T> option, T value) {
        restlight.option(option, value);
        return this;
    }

    /**
     * @see Restlight4Spring#childOptions(Map)
     */
    public ConfigurableManagementRestlight childOptions(Map<ChannelOption<?>, Object> options) {
        restlight.childOptions(options);
        return this;
    }

    /**
     * @see Restlight4Spring#childOption(ChannelOption, Object)
     */
    public <T> ConfigurableManagementRestlight childOption(ChannelOption<T> option, T value) {
        restlight.childOption(option, value);
        return this;
    }

    @Beta
    public ConfigurableManagementRestlight channelHandler(ChannelHandler channelHandler) {
        restlight.channelHandler(channelHandler);
        return this;
    }

    @Beta
    public ConfigurableManagementRestlight channelHandlers(ChannelHandler... channelHandlers) {
        restlight.channelHandlers(channelHandlers);
        return this;
    }

    @Beta
    public ConfigurableManagementRestlight channelHandlers(Collection<? extends ChannelHandler> channelHandlers) {
        restlight.channelHandlers(channelHandlers);
        return this;
    }

    /**
     * @see Restlight4Spring#deployments()
     */
    public Deployments.Impl deployments() {
        return restlight.deployments();
    }
}
