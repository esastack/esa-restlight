/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.server.handler;

import esa.commons.Checks;
import io.netty.channel.Channel;

import java.net.SocketAddress;

/**
 * ChannelConnection is default implement of {@link Connection} which wraps {@link io.netty.channel.Channel}.
 */
public class ChannelConnection implements Connection {

    private final Channel channel;

    public ChannelConnection(Channel channel) {
        Checks.checkNotNull(channel, "channel");
        this.channel = channel;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return channel.isRegistered();
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
