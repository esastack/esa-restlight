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

import esa.commons.Checks;
import esa.commons.NetworkUtils;
import io.esastack.httpserver.H2OptionsConfigure;
import io.esastack.httpserver.HttpServer;
import io.esastack.httpserver.ServerOptionsConfigure;
import io.esastack.httpserver.SslOptionsConfigure;
import io.esastack.restlight.core.util.ResourceUtils;
import io.esastack.restlight.core.util.RestlightVer;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.config.SslOptions;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.core.impl.HttResponseImpl;
import io.esastack.restlight.server.core.impl.HttpRequestImpl;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.schedule.ExecutorScheduler;
import io.esastack.restlight.server.schedule.Schedulers;
import io.esastack.restlight.server.util.LoggerUtils;
import io.esastack.restlight.server.util.PromiseUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.esastack.restlight.server.context.impl.RequestContextImpl.UNDERLYING_RESPONSE;

public class NettyRestlightServer implements RestlightServer {

    protected final HttpServer httpServer;
    private final SocketAddress address;
    private final RestlightHandler handler;

    /**
     * running state(protected by {@link #lock})
     */
    private boolean running;

    /**
     * lock
     */
    private final Lock lock = new ReentrantLock();

    /**
     * condition for signal threads that called {@link #await()}
     */
    private final Condition shutdown = lock.newCondition();

    NettyRestlightServer(ServerOptions options,
                         RestlightHandler handler,
                         SocketAddress address,
                         boolean daemon,
                         Map<ChannelOption<?>, Object> channelOptions,
                         Map<ChannelOption<?>, Object> childChannelOptions,
                         List<ChannelHandler> channelHandlers) {
        Checks.checkNotNull(options, "options");
        Checks.checkNotNull(handler, "handler");
        this.address = address == null ? new InetSocketAddress(8080) : address;
        this.handler = handler;
        this.httpServer = buildServer(options,
                handler,
                daemon,
                channelOptions,
                childChannelOptions,
                channelHandlers);
    }

    @Override
    public synchronized boolean isStarted() {
        return running;
    }

    @Override
    public void start() {
        lock.lock();
        boolean didStart = !running;
        try {
            if (running) {
                throw new IllegalStateException("Restlight server has already been started.");
            }
            handler.onStart();
            // start NettyHttpServer
            httpServer.listen(address);

            running = true;
            LoggerUtils.logger().info("Restlight server({}) started on {}.",
                    RestlightVer.version(),
                    NetworkUtils.parseAddress(address));
        } finally {
            // registerRoutes gracefully shutdown if we did httpServer.start() operation actually.
            if (didStart) {
                try {
                    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "shutdown-hook-thread"));
                } catch (Throwable t) {
                    LoggerUtils.logger().warn("Could not add shutdown hook for restlight!", t);
                }
            }
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (running) {
                // shutdown NettyHttpServer
                httpServer.close();
                running = false;
                shutdown.signalAll();
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void await() {
        lock.lock();
        try {
            if (running) {
                shutdown.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Executor ioExecutor() {
        return httpServer.ioGroup();
    }

    @Override
    public Executor bizExecutor() {
        return handler.schedulers()
                .stream()
                .filter(Schedulers::isBiz)
                .findAny()
                .map(scheduler -> {
                    if (scheduler instanceof ExecutorScheduler) {
                        return ((ExecutorScheduler) scheduler).executor();
                    }
                    return null;
                })
                .orElse(null);
    }

    @Override
    public SocketAddress address() {
        return address;
    }

    private HttpServer buildServer(ServerOptions options,
                                   RestlightHandler handler,
                                   boolean daemon,
                                   Map<ChannelOption<?>, Object> channelOptions,
                                   Map<ChannelOption<?>, Object> childChannelOptions,
                                   List<ChannelHandler> channelHandlers) {

        ServerOptionsConfigure configure = ServerOptionsConfigure.newOpts()
                .daemon(daemon)
                .preferNativeTransport(options.isUseNativeTransports())
                .h2(H2OptionsConfigure.newOpts()
                        .enabled(options.isHttp2Enable())
                        .gracefulShutdownTimeoutMillis(options.getBizTerminationTimeoutSeconds())
                        .configured())
                .bossThreads(options.getConnectorThreads())
                .ioThreads(options.getIoThreads())
                .compress(options.isCompress())
                .decompress(options.isDecompress())
                .maxContentLength(options.getMaxContentLength())
                .maxInitialLineLength(options.getMaxInitialLineLength())
                .maxHeaderSize(options.getMaxHeaderSize())
                .soBacklog(options.getSoBacklog())
                .writeBufferHighWaterMark(options.getWriteBufferHighWaterMark())
                .writeBufferLowWaterMark(options.getWriteBufferLowWaterMark())
                .idleTimeoutSeconds(options.getIdleTimeSeconds())
                .keepAliveEnable(options.isKeepAliveEnable())
                .logging(options.getLogging())
                .channelHandlers(channelHandlers)
                .options(channelOptions)
                .childOptions(childChannelOptions)
                // TODO: export metrics
                .metricsEnabled(false);

        //configuration for http or https
        if (options.getSsl().isEnable()) {
            SslOptions sslOptions = options.getSsl();

            final SslOptionsConfigure ssl0 = SslOptionsConfigure.newOpts();
            ssl0.clientAuth(sslOptions.getClientAuth());
            if (!sslOptions.getCiphers().isEmpty()) {
                ssl0.ciphers(sslOptions.getCiphers().toArray(new String[0]));
            }
            if (!sslOptions.getEnabledProtocols().isEmpty()) {
                ssl0.enabledProtocols(sslOptions.getEnabledProtocols().toArray(new String[0]));
            }
            ssl0.certificate(ResourceUtils.getFile(sslOptions.getCertChainPath()));
            ssl0.privateKey(ResourceUtils.getFile(sslOptions.getKeyPath()));
            ssl0.keyPassword(sslOptions.getKeyPassword());
            ssl0.trustCertificates(ResourceUtils.getFile(sslOptions.getTrustCertsPath()));
            ssl0.sessionTimeout(sslOptions.getSessionTimeout());
            ssl0.sessionCacheSize(sslOptions.getSessionCacheSize());
            ssl0.handshakeTimeoutMillis(sslOptions.getHandshakeTimeoutMillis());
            configure.ssl(ssl0.configured());
        }

        return HttpServer.create(configure.configured())
                .onConnected(handler::onConnected)
                .onClose(handler::shutdown)
                .onDisconnected(handler::onDisconnected)
                .handle(req -> req.aggregate(true)
                        .onEnd(promise -> {
                            HttpResponse response = new HttResponseImpl(req.response());
                            RequestContext context = new RequestContextImpl(new HttpRequestImpl(req), response);
                            context.attrs().attr(UNDERLYING_RESPONSE).set(req.response());
                            handler.process(context)
                                    .whenComplete((r, t) -> {
                                        if (t == null) {
                                            PromiseUtils.setSuccess(promise);
                                        } else {
                                            PromiseUtils.setFailure(promise, t);
                                        }
                                    });
                            return promise;
                        }));
    }
}
