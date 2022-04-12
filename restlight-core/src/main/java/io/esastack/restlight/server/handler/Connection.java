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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletionStage;

/**
 * connection.
 */
public interface Connection {

    /**
     * Returns the id of {@link Connection}
     */
    String id();

    /**
     * Returns {@code true} if the {@link Connection} is open and may get active later
     */
    boolean isOpen();

    /**
     * Returns {@code true} if the {@link Connection} is registered.
     */
    boolean isRegistered();

    /**
     * Return {@code true} if the {@link Connection} is active and so connected.
     */
    boolean isActive();

    /**
     * Returns the local address where this channel is bound to. The returned
     * {@link SocketAddress} is supposed to be down-cast into more concrete
     * type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     */
    SocketAddress localAddress();

    /**
     * Returns the remote address where this channel is connected to. The
     * returned {@link SocketAddress} is supposed to be down-cast into more
     * concrete type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     */
    SocketAddress remoteAddress();

    /**
     * Close the connection.
     */
    CompletionStage<Void> close();
}
