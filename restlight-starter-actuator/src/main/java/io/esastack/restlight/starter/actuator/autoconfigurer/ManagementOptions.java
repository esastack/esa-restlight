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
package io.esastack.restlight.starter.actuator.autoconfigurer;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.server.config.BizThreadsOptionsConfigure;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ManagementOptions
 *
 */
@ConfigurationProperties(prefix = "management.server.restlight")
public class ManagementOptions extends RestlightOptions {
    private static final long serialVersionUID = -2187295833236096900L;

    private String unixDomainSocketFile;

    public ManagementOptions() {
        setIoThreads(1);
        setBizThreads(BizThreadsOptionsConfigure.newOpts()
                .core(2)
                .max(2)
                .blockingQueueLength(128)
                .keepAliveTimeSeconds(30L).configured());
        setMaxContentLength(8 * 1024 * 1024);
        setMaxHeaderSize(8192);
        setMaxInitialLineLength(8192);
        // TODO: set ssl by ManagementServerProperties
    }

    public String getUnixDomainSocketFile() {
        return unixDomainSocketFile;
    }

    public void setUnixDomainSocketFile(String unixDomainSocketFile) {
        this.unixDomainSocketFile = unixDomainSocketFile;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ManagementOptions{");
        sb.append("unixDomainSocketFile='").append(unixDomainSocketFile).append('\'');
        sb.append(", http2Enable=").append(isHttp2Enable());
        sb.append(", useNativeTransports=").append(isUseNativeTransports());
        sb.append(", connectorThreads=").append(getConnectorThreads());
        sb.append(", ioThreads=").append(getIoThreads());
        sb.append(", bizThreads=").append(getBizThreads());
        sb.append(", bizTerminationTimeoutSeconds=").append(getBizTerminationTimeoutSeconds());
        sb.append(", contextPath='").append(getContextPath()).append('\'');
        sb.append(", serialize=").append(getSerialize());
        sb.append(", compress=").append(isCompress());
        sb.append(", decompress=").append(isDecompress());
        sb.append(", maxContentLength=").append(getMaxContentLength());
        sb.append(", maxInitialLineLength=").append(getMaxInitialLineLength());
        sb.append(", maxHeaderSize=").append(getMaxHeaderSize());
        sb.append(", soBacklog=").append(getSoBacklog());
        sb.append(", writeBufferHighWaterMark=").append(getWriteBufferHighWaterMark());
        sb.append(", writeBufferLowWaterMark=").append(getWriteBufferLowWaterMark());
        sb.append(", idleTimeSeconds=").append(getIdleTimeSeconds());
        sb.append(", keepAliveEnable=").append(isKeepAliveEnable());
        sb.append(", scheduling=").append(getScheduling());
        sb.append(", route=").append(getRoute());
        sb.append(", ssl=").append(getSsl());
        sb.append(", ext=").append(getExt());
        sb.append('}');
        return sb.toString();
    }
}
