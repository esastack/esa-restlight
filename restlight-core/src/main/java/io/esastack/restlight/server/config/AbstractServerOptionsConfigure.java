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
package io.esastack.restlight.server.config;

import esa.commons.Platforms;
import io.netty.channel.epoll.Epoll;
import io.netty.handler.logging.LogLevel;

public abstract class AbstractServerOptionsConfigure<C extends AbstractServerOptionsConfigure<C, O>,
        O extends ServerOptions> {

    private boolean http2Enable;
    private boolean useNativeTransports =
            Platforms.isLinux() && Epoll.isAvailable();
    private int connectorThreads = 1;
    private int ioThreads =
            Math.min(Platforms.cpuNum() << 1, 64);

    private long bizTerminationTimeoutSeconds = 60L;
    private boolean compress;
    private boolean decompress;
    private int maxContentLength = 4 * 1024 * 1024;
    private int maxInitialLineLength = 4096;
    private int maxHeaderSize = 8192;
    private int soBacklog = 128;
    private int writeBufferHighWaterMark = -1;
    private int writeBufferLowWaterMark = -1;
    private int idleTimeSeconds = 60;
    private boolean keepAliveEnable = true;
    private LogLevel logging;
    private SslOptions ssl =
            SslOptionsConfigure.defaultOpts();
    private SchedulingOptions scheduling
            = SchedulingOptionsConfigure.defaultOpts();
    private RouteOptions route
            = RouteOptionsConfigure.defaultOpts();
    private BizThreadsOptions bizThreads
            = BizThreadsOptionsConfigure.defaultOpts();

    public C http2Enable(boolean http2Enable) {
        this.http2Enable = http2Enable;
        return self();
    }

    public C useNativeTransports(boolean useNativeTransports) {
        this.useNativeTransports = useNativeTransports;
        return self();
    }

    public C connectorThreads(int connectorThreads) {
        this.connectorThreads = connectorThreads;
        return self();
    }

    public C ioThreads(int ioThreads) {
        this.ioThreads = ioThreads;
        return self();
    }

    public C bizTerminationTimeoutSeconds(long bizTerminationTimeoutSeconds) {
        this.bizTerminationTimeoutSeconds = bizTerminationTimeoutSeconds;
        return self();
    }

    public C bizThreads(BizThreadsOptions bizThreads) {
        this.bizThreads = bizThreads;
        return self();
    }

    public C compress(boolean compress) {
        this.compress = compress;
        return self();
    }

    public C decompress(boolean decompress) {
        this.decompress = decompress;
        return self();
    }

    public C maxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return self();
    }

    public C maxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return self();
    }

    public C maxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        return self();
    }

    public C soBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
        return self();
    }

    public C writeBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        return self();
    }

    public C writeBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
        return self();
    }

    public C idleTimeSeconds(int idleTimeSeconds) {
        this.idleTimeSeconds = idleTimeSeconds;
        return self();
    }

    public C keepAliveEnable(boolean keepAliveEnable) {
        this.keepAliveEnable = keepAliveEnable;
        return self();
    }

    public C logging(LogLevel logging) {
        this.logging = logging;
        return self();
    }

    public C ssl(SslOptions https) {
        this.ssl = https;
        return self();
    }

    public C scheduling(SchedulingOptions scheduling) {
        this.scheduling = scheduling;
        return self();
    }

    public C route(RouteOptions route) {
        this.route = route;
        return self();
    }

    @SuppressWarnings("unchecked")
    protected C self() {
        return (C) this;
    }

    public O configured() {
        O options = newOptions();
        options.setHttp2Enable(http2Enable);
        options.setUseNativeTransports(useNativeTransports);
        options.setConnectorThreads(connectorThreads);
        options.setIoThreads(ioThreads);
        options.setBizTerminationTimeoutSeconds(bizTerminationTimeoutSeconds);
        options.setCompress(compress);
        options.setDecompress(decompress);
        options.setMaxContentLength(maxContentLength);
        options.setMaxInitialLineLength(maxInitialLineLength);
        options.setMaxHeaderSize(maxHeaderSize);
        options.setSoBacklog(soBacklog);
        options.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        options.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        options.setIdleTimeSeconds(idleTimeSeconds);
        options.setKeepAliveEnable(keepAliveEnable);
        options.setLogging(logging);
        options.setSsl(ssl);
        options.setScheduling(scheduling);
        options.setRoute(route);
        options.setBizThreads(bizThreads);
        return options;
    }

    protected abstract O newOptions();

}
