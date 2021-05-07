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
package esa.restlight.starter.autoconfigure;

import esa.restlight.core.config.RestlightOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = AutoRestlightServerOptions.PREFIX)
public class AutoRestlightServerOptions extends RestlightOptions {

    private static final long serialVersionUID = 1748370990996967262L;
    public static final String PREFIX = "restlight.server";

    private String host;
    private int port = 8080;
    private String unixDomainSocketFile;
    private boolean printBanner = true;
    private WarmUpOptions warmUp = new WarmUpOptions();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUnixDomainSocketFile() {
        return unixDomainSocketFile;
    }

    public void setUnixDomainSocketFile(String unixDomainSocketFile) {
        this.unixDomainSocketFile = unixDomainSocketFile;
    }

    public boolean isPrintBanner() {
        return printBanner;
    }

    public void setPrintBanner(boolean printBanner) {
        this.printBanner = printBanner;
    }

    public WarmUpOptions getWarmUp() {
        return warmUp;
    }

    public void setWarmUp(WarmUpOptions warmUp) {
        this.warmUp = warmUp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Options{");
        sb.append("host=").append(getHost());
        sb.append(", port=").append(getPort());
        sb.append(", unixDomainSocketFile=").append(getUnixDomainSocketFile());
        sb.append(", http2Enable=").append(isHttp2Enable());
        sb.append(", useNativeTransports=").append(isUseNativeTransports());
        sb.append(", connectorThreads=").append(getConnectorThreads());
        sb.append(", ioThreads=").append(getIoThreads());
        sb.append(", bizThreads=").append(getBizThreads());
        sb.append(", bizTerminationTimeoutSeconds=").append(getBizTerminationTimeoutSeconds());
        sb.append(", validationMessageFile='").append(getValidationMessageFile()).append('\'');
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
        sb.append(", warmUp=").append(getWarmUp());
        sb.append(", printBanner=").append(isPrintBanner());
        sb.append(", ext=").append(getExt());
        sb.append('}');
        return sb.toString();
    }
}
