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
package io.esastack.restlight.ext.interceptor.signature;

import esa.commons.SecurityUtils;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.ext.interceptor.config.SignatureOptionsConfigure;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.test.mock.MockHttpRequest;
import io.esastack.restlight.test.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSha1SignatureHandlerInterceptorTest extends AbstractSignatureInterceptorTest {

    private static final String SECRET = "abcdefght";

    private HmacSha1SignatureHandlerInterceptor signInterceptor;

    @BeforeEach
    void init() {
        signInterceptor = new HmacSha1SignatureHandlerInterceptor(SignatureOptionsConfigure.newOpts()
                .appId(APP_ID_PARAM_NAME)
                .secretVersion(SECRET_VERSION_PARAM_NAME)
                .timestamp(TIMESTAMP_PARAM_NAME)
                .signature(SIGNATURE_PARAM)
                .expireSeconds(TIMESTAMP_ACTIVE_SECONDS)
                .configured(),
                (appId, secretVersion, timestamp) -> SECRET,
                new SignValidationScope() {
                    @Override
                    public String[] includes() {
                        return new String[]{"/**"};
                    }

                    @Override
                    public String[] excludes() {
                        return new String[0];
                    }
                });
    }

    @Test
    void testParamsIsEmpty() {
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final HttpRequest request = MockHttpRequest.aMockRequest().withBody(new byte[0]).build();
        assertTrue(signInterceptor.preHandle(new RequestContextImpl(request, response), null));
    }

    @Test
    void testSignCorrectly() {
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final MockHttpRequest.Builder builder = MockHttpRequest.aMockRequest();
        builder.withBody("afadfadfadbabbafdbeqwet14ty231@4&~".getBytes(StandardCharsets.UTF_8));
        builder.withUri("/test");

        final String timestamp = String.valueOf(System.currentTimeMillis());

        byte[] data =
                ("anme=543253256&appId=appId&bgag=gjaajfdcjvaljal&cdfas=qewrqwr&sv=1.0&timestamp=" + timestamp)
                        .getBytes(StandardCharsets.UTF_8);
        byte[] body = "afadfadfadbabbafdbeqwet14ty231@4&~".getBytes(StandardCharsets.UTF_8);

        final byte[] mergedData = buildData(data, body);

        String signature = SecurityUtils.getHmacSHA1(mergedData, "abcdefght");
        builder.withParameter(SIGNATURE_PARAM, signature);
        builder.withParameter(APP_ID_PARAM_NAME, "appId");
        builder.withParameter(SECRET_VERSION_PARAM_NAME, "1.0");
        builder.withParameter(TIMESTAMP_PARAM_NAME, timestamp);
        builder.withParameter("anme", "543253256");
        builder.withParameter("bgag", "gjaajfdcjvaljal");
        builder.withParameter("cdfas", "qewrqwr");
        final MockHttpRequest request = builder.build();

        assertTrue(signInterceptor.preHandle(new RequestContextImpl(request, response), null));
    }

    @Test
    void testSignCorrectlyWithFormData() {
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final MockHttpRequest.Builder builder = MockHttpRequest.aMockRequest();
        builder.withBody("anme=543253256&bgag=gjaajfdcjvaljal&cdfas=qewrqwr".getBytes(StandardCharsets.UTF_8));
        builder.withUri("/test");

        final String timestamp = String.valueOf(System.currentTimeMillis());
        byte[] data = ("anme=543253256&appId=appId&bgag=gjaajfdcjvaljal&cdfas=qewrqwr&parms=dfasfaga&sv=1" +
                ".0&timestamp=" + timestamp).getBytes(StandardCharsets.UTF_8);

        String signature = SecurityUtils.getHmacSHA1(data, SECRET);

        builder.withParameter(SIGNATURE_PARAM, signature);
        builder.withParameter(APP_ID_PARAM_NAME, "appId");
        builder.withParameter(SECRET_VERSION_PARAM_NAME, "1.0");
        builder.withParameter(TIMESTAMP_PARAM_NAME, timestamp);
        builder.withParameter("anme", "543253256");
        builder.withParameter("bgag", "gjaajfdcjvaljal");
        builder.withParameter("cdfas", "qewrqwr");
        builder.withParameter("parms", "dfasfaga");

        Exception exception = null;
        // If Content-Type not equals "x-www-form-urlencoded";
        try {
            HttpRequest request0 = builder.build();
            signInterceptor.preHandle(new RequestContextImpl(request0, response), null);
        } catch (WebServerException ex) {
            exception = ex;
        }
        assertNotNull(exception);

        builder.withMethod("POST");
        builder.withHeader("Content-Type", "application/x-www-form-urlencoded");
        final MockHttpRequest request = builder.build();

        assertTrue(signInterceptor.preHandle(new RequestContextImpl(request, response), null));
    }

    @Test
    void testIllegalSignature() {
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final MockHttpRequest.Builder builder = MockHttpRequest.aMockRequest();
        builder.withBody("afadfadfadbabbafdbeqwet14ty231@4&~".getBytes(StandardCharsets.UTF_8));
        builder.withUri("/test");

        final String timestamp = String.valueOf(System.currentTimeMillis());

        byte[] data =
                ("anme=543253256&appId=appId&bgag=gjaajfdcjvaljal&cdfas=qewrqwr&sv=1.0&timestamp=" + timestamp)
                        .getBytes(StandardCharsets.UTF_8);
        byte[] body = "afadfadfadbabbafdbeqwet14ty231@4&~".getBytes(StandardCharsets.UTF_8);

        final byte[] mergedData = buildData(data, body);

        String signature = SecurityUtils.getHmacSHA1(mergedData, "abcdefght");
        builder.withParameter(SIGNATURE_PARAM, signature);

        builder.withParameter("anme", "543253256");
        builder.withParameter("bgag", "gjaajfdcjvaljal");
        builder.withParameter("cdfas", "qewrqwr");
        builder.withParameter(TIMESTAMP_PARAM_NAME, timestamp);
        final MockHttpRequest request = builder.build();

        assertThrows(WebServerException.class, () -> signInterceptor.preHandle(new RequestContextImpl(request,
                response), null));
    }

    private byte[] buildData(byte[] data, byte[] body) {
        byte[] mergedData = new byte[data.length + body.length];
        System.arraycopy(data, 0, mergedData, 0, data.length);
        System.arraycopy(body, 0, mergedData, data.length, body.length);

        return mergedData;
    }

}
