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
package esa.restlight.ext.interceptor.signature;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.util.MediaType;
import esa.restlight.ext.interceptor.config.SignatureOptions;
import esa.restlight.server.bootstrap.WebServerException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.InternalThreadLocalMap;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

abstract class AbstractSignatureInterceptor implements InternalInterceptor {

    protected static final int MILLISECOND = 1000;
    protected final SecretProvider distributor;

    private final SignatureOptions options;

    public AbstractSignatureInterceptor(SignatureOptions options, SecretProvider secretProvider) {
        Checks.checkNotNull(options, "options");
        Checks.checkNotNull(secretProvider, "secretProvider");
        if (StringUtils.isEmpty(options.getAppId())
                || StringUtils.isEmpty(options.getSecretVersion())
                || StringUtils.isEmpty(options.getTimestamp())
                || StringUtils.isEmpty(options.getSignature())) {
            throw new IllegalArgumentException("[appId, secretVersion, timestamp, signature]'s name" +
                    " must not be empty!");
        }
        this.options = options;
        this.distributor = secretProvider;
    }

    @Override
    public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
        boolean emptyParams = request.parameterMap() == null || request.parameterMap().isEmpty();
        if (emptyParams && request.contentLength() == 0) {
            return true;
        }
        final String signature = StringUtils.trim(getSignature(request));

        // Send error if we can not get the signature from the request
        if (StringUtils.isEmpty(signature)) {
            throw WebServerException.badRequest("Missing required value: " + signatureName());
        }

        // Validate timestamp
        final String timestamp = StringUtils.trim(getTimestamp(request));
        if (options.getExpireSeconds() > 0 && (System.currentTimeMillis() - Long.valueOf(timestamp)
                > options.getExpireSeconds() * MILLISECOND)) {
            throw WebServerException.badRequest("Signature has expired");
        }

        final String secret = StringUtils.trim(distributor.get(StringUtils.trim(getAppId(request)),
                StringUtils.trim(getSecretVersion(request)), timestamp));
        if (StringUtils.isEmpty(secret)) {
            throw WebServerException.badRequest("Missing required value: " + secretVersionName());
        }

        final byte[] data = buildData(request);

        // Validate signature
        if (data == null || data.length == 0 || validate(data, signature, secret)) {
            return true;
        }
        throw new WebServerException(HttpResponseStatus.UNAUTHORIZED, "Unmatched secret");
    }

    protected byte[] buildData(AsyncRequest request) {
        // Sort all request parameters in ascending order by parameter name
        byte[] paramsData = new byte[0];
        final Map<String, List<String>> params = request.parameterMap();
        if (params != null && params.size() > 0) {
            // Exclude signature params.
            List<ParamValues> paramValues =
                    InternalThreadLocalMap.get().arrayList(params.size());
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                if (options.getSignature().equals(StringUtils.trim(entry.getKey()))) {
                    continue;
                }
                final List<String> paramAfterTrim = new LinkedList<>();
                entry.getValue().forEach((String value) -> paramAfterTrim.add(StringUtils.trim(value)));
                paramValues.add(new ParamValues(StringUtils.trim(entry.getKey()), paramAfterTrim));
            }

            // Sort by parameter name
            Collections.sort(paramValues);
            final StringBuilder builder = InternalThreadLocalMap.get().stringBuilder();
            for (ParamValues parameters : paramValues) {
                Collections.sort(parameters.values);
                for (String value : parameters.values) {
                    builder.append(parameters.name).append("=").append(value).append("&");
                }
            }
            paramsData = (builder.toString().substring(0, Math.max(0, builder.length() - 1)))
                    .getBytes(StandardCharsets.UTF_8);
        }

        if (HttpMethod.POST.equals(request.method())) {
            // Note: If requestHeader contains
            String contentType = request.getHeader(HttpHeaderNames.CONTENT_TYPE);
            if (!StringUtils.isEmpty(contentType)) {
                MediaType mediaType = MediaType.valueOf(contentType);
                if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                    return paramsData;
                }
            }
        }

        byte[] body = request.body();
        if (body != null && body.length > 0) {
            byte[] mergedData = new byte[paramsData.length + body.length];
            System.arraycopy(paramsData, 0, mergedData, 0, paramsData.length);
            System.arraycopy(body, 0, mergedData, paramsData.length, body.length);
            return mergedData;
        } else {
            return paramsData;
        }
    }

    /**
     * To verify whether the signature is legal
     *
     * @param data      data
     * @param signature signature
     * @param sk        secret key
     *
     * @return true if the signature is legal, else false
     */
    protected abstract boolean validate(byte[] data, String signature, String sk);

    protected String getAppId(AsyncRequest request) {
        return getValue(request, options.getAppId());
    }

    protected String getSecretVersion(AsyncRequest request) {
        return getValue(request, options.getSecretVersion());
    }

    protected String getTimestamp(AsyncRequest request) {
        return getValue(request, options.getTimestamp());
    }

    protected String getSignature(AsyncRequest request) {
        return getValue(request, options.getSignature());
    }

    private String getValue(AsyncRequest request, String key) {
        String value;
        return (value = request.getParameter(key)) != null ? value : request.getHeader(key);
    }

    protected String appIdName() {
        return options.getAppId();
    }

    protected String secretVersionName() {
        return options.getSecretVersion();
    }

    protected String timestampName() {
        return options.getTimestamp();
    }

    protected String signatureName() {
        return options.getSignature();
    }

    protected int expireSeconds() {
        return options.getExpireSeconds();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private static class ParamValues implements Comparable<ParamValues> {
        private final String name;
        private final List<String> values;

        private ParamValues(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        public int compareTo(ParamValues o) {
            return this.name.compareTo(o.name);
        }
    }

}
