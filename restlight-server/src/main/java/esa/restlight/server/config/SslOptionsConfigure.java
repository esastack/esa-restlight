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

import io.netty.handler.ssl.ClientAuth;

import java.util.LinkedList;
import java.util.List;

public final class SslOptionsConfigure {
    private boolean enable;
    private ClientAuth clientAuth;
    private List<String> ciphers = new LinkedList<>();
    private List<String> enabledProtocols = new LinkedList<>();
    private String certChainPath;
    private String keyPath;
    private String keyPassword;
    private String trustCertsPath;
    private long sessionTimeout;
    private long sessionCacheSize;
    private long handshakeTimeoutMillis;

    private SslOptionsConfigure() {
    }

    public static SslOptionsConfigure newOpts() {
        return new SslOptionsConfigure();
    }

    public static SslOptions defaultOpts() {
        return newOpts().configured();
    }

    public SslOptionsConfigure enable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public SslOptionsConfigure clientAuth(ClientAuth clientAuth) {
        this.clientAuth = clientAuth;
        return this;
    }

    public SslOptionsConfigure ciphers(List<String> ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public SslOptionsConfigure enabledProtocols(List<String> enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
        return this;
    }

    public SslOptionsConfigure certChainPath(String certChainPath) {
        this.certChainPath = certChainPath;
        return this;
    }

    public SslOptionsConfigure keyPath(String keyPath) {
        this.keyPath = keyPath;
        return this;
    }

    public SslOptionsConfigure keyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
        return this;
    }

    public SslOptionsConfigure trustCertsPath(String trustCertsPath) {
        this.trustCertsPath = trustCertsPath;
        return this;
    }

    public SslOptionsConfigure sessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public SslOptionsConfigure sessionCacheSize(long sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
        return this;
    }

    public SslOptionsConfigure handshakeTimeoutMillis(long handshakeTimeoutMillis) {
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
        return this;
    }

    public SslOptions configured() {
        SslOptions sslOptions = new SslOptions();
        sslOptions.setEnable(enable);
        sslOptions.setClientAuth(clientAuth);
        sslOptions.setCiphers(ciphers);
        sslOptions.setEnabledProtocols(enabledProtocols);
        sslOptions.setCertChainPath(certChainPath);
        sslOptions.setKeyPath(keyPath);
        sslOptions.setKeyPassword(keyPassword);
        sslOptions.setTrustCertsPath(trustCertsPath);
        sslOptions.setSessionTimeout(sessionTimeout);
        sslOptions.setSessionCacheSize(sessionCacheSize);
        sslOptions.setHandshakeTimeoutMillis(handshakeTimeoutMillis);
        return sslOptions;
    }
}
