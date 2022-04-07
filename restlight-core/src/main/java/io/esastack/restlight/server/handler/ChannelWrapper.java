/*
 *
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

package io.esastack.restlight.server.handler;

import esa.commons.Checks;
import io.netty.channel.Channel;

/**
 * the ChannelWrapper mainly shield the detail use of {@link io.netty.channel.Channel}.
 *
 * @author chenglu
 */
public final class ChannelWrapper {

    private final Channel channel;

    public ChannelWrapper(Channel channel) {
        Checks.checkNotNull(channel, "channel");
        this.channel = channel;
    }

    public void close() {
        channel.close();
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
