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
package esa.restlight.server.config;

import esa.commons.Platforms;
import io.netty.channel.epoll.Epoll;
import io.netty.handler.logging.LogLevel;

import java.io.Serializable;

public class ServerOptions implements Serializable {

    private static final long serialVersionUID = 2611801193782941798L;

    /**
     * http1.1 or http2
     */
    private boolean http2Enable;

    /**
     * Use EpollEventLoopGroup or else NIOEventLoopGroup
     */
    private boolean useNativeTransports =
            Platforms.isLinux() && Epoll.isAvailable();
    /**
     * connector thread count
     */
    private int connectorThreads = 1;

    /**
     * io thread count, cpu * 2
     */
    private int ioThreads =
            Math.min(Platforms.cpuNum() << 1, 64);

    /**
     * core biz thread count, cpu * 4 (must between 32 and 128)
     */
    private int bizThreadsCore =
            Math.min(Math.max(64, Platforms.cpuNum() << 2), 128);

    /**
     * maximum biz thread count, cpu * 6 (must between 128 and 256)
     */
    private int bizThreadsMax =
            Math.min(Math.max(128, (Platforms.cpuNum() << 2) + (Platforms.cpuNum() << 1)), 256);

    /**
     * maximum waiting queue length
     */
    private int bizThreadsBlockingQueueLength = 512;

    /**
     * thread pool keepAlive time(default to 180s)
     */
    private long bizThreadsKeepAliveTimeSeconds = 180L;

    /**
     * Max time to wait for the biz thread to terminate(default to 60s)
     */
    private long bizTerminationTimeoutSeconds = 60L;
    /**
     * is compress
     */
    private boolean compress;
    /**
     * is decompress
     */
    private boolean decompress;

    /**
     * maxContentLength, 4M
     */
    private int maxContentLength = 4 * 1024 * 1024;

    /**
     * Max initial line length of http protocol.
     */
    private int maxInitialLineLength = 4096;

    /**
     * Max size of http header.
     */
    private int maxHeaderSize = 8192;

    /**
     * netty configuration of ChannelOption.SO_BACKLOG
     */
    private int soBacklog = 128;
    /**
     * writeBufferHighWaterMark
     */
    private int writeBufferHighWaterMark = -1;

    /**
     * writeBufferLowWaterMark
     */
    private int writeBufferLowWaterMark = -1;
    /**
     * netty idle state handler#allIdleTime(default to 60s)
     */
    private int idleTimeSeconds = 60;

    /**
     * Is server keep alive enable
     */
    private boolean keepAliveEnable = true;

    private LogLevel logging;

    /**
     * configuration for ssl
     */
    private SslOptions ssl =
            SslOptionsConfigure.defaultOpts();

    private SchedulingOptions scheduling
            = SchedulingOptionsConfigure.defaultOpts();

    private RouteOptions route
            = RouteOptionsConfigure.defaultOpts();

    public boolean isHttp2Enable() {
        return http2Enable;
    }

    public void setHttp2Enable(boolean http2Enable) {
        this.http2Enable = http2Enable;
    }

    public boolean isUseNativeTransports() {
        return useNativeTransports;
    }

    public void setUseNativeTransports(boolean useNativeTransports) {
        this.useNativeTransports = useNativeTransports;
    }

    public int getConnectorThreads() {
        return connectorThreads;
    }

    public void setConnectorThreads(int connectorThreads) {
        this.connectorThreads = connectorThreads;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }

    /**
     * @deprecated use {@link #getBizThreadsCore()}
     *
     * @return  biz biz-threads count
     */
    @Deprecated
    public int getCoreBizThreads() {
        return getBizThreadsCore();
    }

    /**
     * @deprecated use {@link #setBizThreadsCore(int)}
     *
     * @param coreBizThreads core size of biz-threads
     */
    @Deprecated
    public void setCoreBizThreads(int coreBizThreads) {
        setBizThreadsCore(coreBizThreads);
    }

    /**
     * @deprecated use {@link #getBizThreadsMax()}
     *
     * @return  max biz-threads count
     */
    @Deprecated
    public int getMaxBizThreads() {
        return getBizThreadsMax();
    }

    /**
     * @deprecated use {@link #setBizThreadsMax(int)}
     *
     * @param maxBizThreads max size of biz-threads
     */
    @Deprecated
    public void setMaxBizThreads(int maxBizThreads) {
        setBizThreadsMax(maxBizThreads);
    }

    /**
     * @deprecated use {@link #getBizThreadsBlockingQueueLength()}
     *
     * @return  blocking queue size of biz-threads
     */
    @Deprecated
    public int getBlockingQueueLength() {
        return getBizThreadsBlockingQueueLength();
    }

    /**
     * @deprecated use {@link #setBizThreadsBlockingQueueLength(int)}
     *
     * @param blockingQueueLength blocking queue size of biz-threads
     */
    @Deprecated
    public void setBlockingQueueLength(int blockingQueueLength) {
        setBizThreadsBlockingQueueLength(blockingQueueLength);
    }

    /**
     * @deprecated use {@link #getBizThreadsKeepAliveTimeSeconds()}
     *
     * @return  keep alive time of biz-threads
     */
    @Deprecated
    public long getKeepAliveTimeSeconds() {
        return getBizThreadsKeepAliveTimeSeconds();
    }

    /**
     * @deprecated use {@link #setBizThreadsKeepAliveTimeSeconds(long)}
     *
     * @param keepAliveTimeSeconds  keep alive time of biz-threads
     */
    @Deprecated
    public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
        setBizThreadsKeepAliveTimeSeconds(keepAliveTimeSeconds);
    }

    public int getBizThreadsCore() {
        return bizThreadsCore;
    }

    public void setBizThreadsCore(int bizThreadsCore) {
        this.bizThreadsCore = bizThreadsCore;
    }

    public int getBizThreadsMax() {
        return bizThreadsMax;
    }

    public void setBizThreadsMax(int bizThreadsMax) {
        this.bizThreadsMax = bizThreadsMax;
    }

    public int getBizThreadsBlockingQueueLength() {
        return bizThreadsBlockingQueueLength;
    }

    public void setBizThreadsBlockingQueueLength(int bizThreadsBlockingQueueLength) {
        this.bizThreadsBlockingQueueLength = bizThreadsBlockingQueueLength;
    }

    public long getBizThreadsKeepAliveTimeSeconds() {
        return bizThreadsKeepAliveTimeSeconds;
    }

    public void setBizThreadsKeepAliveTimeSeconds(long bizThreadsKeepAliveTimeSeconds) {
        this.bizThreadsKeepAliveTimeSeconds = bizThreadsKeepAliveTimeSeconds;
    }

    public long getBizTerminationTimeoutSeconds() {
        return bizTerminationTimeoutSeconds;
    }

    public void setBizTerminationTimeoutSeconds(long bizTerminationTimeoutSeconds) {
        this.bizTerminationTimeoutSeconds = bizTerminationTimeoutSeconds;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isDecompress() {
        return decompress;
    }

    public void setDecompress(boolean decompress) {
        this.decompress = decompress;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public void setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public void setMaxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public void setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getIdleTimeSeconds() {
        return idleTimeSeconds;
    }

    public void setIdleTimeSeconds(int idleTimeSeconds) {
        this.idleTimeSeconds = idleTimeSeconds;
    }

    public boolean isKeepAliveEnable() {
        return keepAliveEnable;
    }

    public void setKeepAliveEnable(boolean keepAliveEnable) {
        this.keepAliveEnable = keepAliveEnable;
    }

    public LogLevel getLogging() {
        return logging;
    }

    public void setLogging(LogLevel logging) {
        this.logging = logging;
    }

    public SslOptions getSsl() {
        return ssl;
    }

    public void setSsl(SslOptions ssl) {
        this.ssl = ssl;
    }

    public SchedulingOptions getScheduling() {
        return scheduling;
    }

    public void setScheduling(SchedulingOptions scheduling) {
        this.scheduling = scheduling;
    }

    public RouteOptions getRoute() {
        return route;
    }

    public void setRoute(RouteOptions route) {
        this.route = route;
    }
}
