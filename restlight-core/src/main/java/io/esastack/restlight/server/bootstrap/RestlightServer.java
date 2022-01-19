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

import io.esastack.restlight.core.util.RestlightVer;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

public interface RestlightServer {

    /**
     * Is server started in running mode.
     *
     * @return {@code true} if server is started
     */
    boolean isStarted();

    /**
     * start server
     */
    void start();

    /**
     * shutdown
     */
    void shutdown();

    /**
     * waits to the server shutdown.
     */
    void await();

    /**
     * Gets the {@link Executor} which is used to reading and writing i/o data.
     *
     * @return io {@link Executor} or {@code null} if there's no {@link Executor}.
     */
    Executor ioExecutor();

    /**
     * Gets the {@link Executor} which is used to handle the biz logic.
     *
     * @return io {@link Executor} or {@code null} if there's no {@link Executor}.
     */
    Executor bizExecutor();

    /**
     * Current version of Restlight
     *
     * @return version
     */
    default String version() {
        return RestlightVer.version();
    }

    /**
     * Return the local address which server is binding on, it would be a {@code ip:port} or a socket file.
     *
     * @return local address which server is binding on.
     */
    SocketAddress address();
}
