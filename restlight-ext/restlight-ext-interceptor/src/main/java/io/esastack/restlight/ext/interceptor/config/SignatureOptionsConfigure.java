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
package io.esastack.restlight.ext.interceptor.config;

public final class SignatureOptionsConfigure {

    private String appId = "appId";
    private String secretVersion = "sv";
    private String timestamp = "ts";
    private String signature = "sign";
    private int expireSeconds = 0;

    private SignatureOptionsConfigure() {
    }

    public static SignatureOptionsConfigure newOpts() {
        return new SignatureOptionsConfigure();
    }

    public static SignatureOptions defaultOpts() {
        return newOpts().configured();
    }

    public SignatureOptionsConfigure appId(String appId) {
        this.appId = appId;
        return this;
    }

    public SignatureOptionsConfigure secretVersion(String secretVersion) {
        this.secretVersion = secretVersion;
        return this;
    }

    public SignatureOptionsConfigure timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public SignatureOptionsConfigure expireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
        return this;
    }

    public SignatureOptionsConfigure signature(String signature) {
        this.signature = signature;
        return this;
    }

    public SignatureOptions configured() {
        SignatureOptions options = new SignatureOptions();
        options.setAppId(appId);
        options.setExpireSeconds(expireSeconds);
        options.setSecretVersion(secretVersion);
        options.setSignature(signature);
        options.setTimestamp(timestamp);
        return options;
    }
}
