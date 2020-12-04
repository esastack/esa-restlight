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
package esa.restlight.server.bootstrap;

import esa.restlight.core.util.RestlightVer;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

public abstract class AbstractDelegatedRestlightServer implements RestlightServer {

    private RestlightServer delegate;

    @Override
    public synchronized boolean isStarted() {
        return delegate != null && delegate.isStarted();
    }

    @Override
    public void start() {
        checkAndGetServer().start();
    }

    @Override
    public void shutdown() {
        checkAndGetServer().shutdown();
    }

    @Override
    public void await() {
        checkAndGetServer().await();
    }

    @Override
    public Executor ioExecutor() {
        return checkAndGetServer().ioExecutor();
    }

    @Override
    public Executor bizExecutor() {
        return checkAndGetServer().bizExecutor();
    }

    @Override
    public String version() {
        final RestlightServer server = getServer();
        return server == null ? RestlightVer.version() : server.version();
    }

    @Override
    public SocketAddress address() {
        return checkAndGetServer().address();
    }

    protected synchronized RestlightServer getServer() {
        return delegate;
    }

    protected synchronized void setServer(RestlightServer server) {
        delegate = server;
    }

    protected synchronized RestlightServer checkAndGetServer() {
        if (delegate == null) {
            throw new IllegalStateException(serverName() + " server haven't been initialized.");
        }
        return delegate;
    }

    protected String serverName() {
        return "Restlight";
    }

    public RestlightServer unWrap() {
        return delegate;
    }
}
