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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class SslOptions implements Serializable {

    private static final long serialVersionUID = 8402798941945536820L;

    private boolean enable;
    private ClientAuth clientAuth;
    private List<String> ciphers = new LinkedList<>();
    private List<String> enabledProtocols = new LinkedList<>();
    /**
     * X.509 certificate chain file in PEM format
     */
    private String certChainPath;
    /**
     * PKCS#8 private key file in PEM format
     */
    private String keyPath;
    private String keyPassword;
    private String trustCertsPath;
    private long sessionTimeout;
    private long sessionCacheSize;
    private long handshakeTimeoutMillis;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public ClientAuth getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(ClientAuth clientAuth) {
        this.clientAuth = clientAuth;
    }

    public List<String> getCiphers() {
        return ciphers;
    }

    public void setCiphers(List<String> ciphers) {
        this.ciphers = ciphers;
    }

    public List<String> getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(List<String> enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public String getCertChainPath() {
        return certChainPath;
    }

    public void setCertChainPath(String certChainPath) {
        this.certChainPath = certChainPath;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getTrustCertsPath() {
        return trustCertsPath;
    }

    public void setTrustCertsPath(String trustCertsPath) {
        this.trustCertsPath = trustCertsPath;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public long getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(long sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
    }

    public long getHandshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SslOptions{");
        sb.append("enable=").append(enable);
        sb.append(", clientAuth=").append(clientAuth);
        sb.append(", ciphers=").append(ciphers);
        sb.append(", enabledProtocols=").append(enabledProtocols);
        sb.append(", certChainPath='").append(certChainPath).append('\'');
        sb.append(", keyPath='").append(keyPath).append('\'');
        sb.append(", keyPassword='").append(keyPassword).append('\'');
        sb.append(", trustCertsPath='").append(trustCertsPath).append('\'');
        sb.append(", sessionTimeout=").append(sessionTimeout);
        sb.append(", sessionCacheSize=").append(sessionCacheSize);
        sb.append(", handshakeTimeoutMillis=").append(handshakeTimeoutMillis);
        sb.append('}');
        return sb.toString();
    }
}
