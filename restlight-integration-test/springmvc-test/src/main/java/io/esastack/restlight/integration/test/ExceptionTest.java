/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.test;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restclient.RestResponseBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author chenglu
 */
public class ExceptionTest extends BaseIntegrationTest {

    @Test
    public void testException() throws Exception {
        RestResponseBase response = restClient.get(domain + "/exception/get")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.FORBIDDEN.code(), response.status());
        Assert.assertEquals("Forbidden", response.bodyToEntity(String.class));
    }

    @Test
    public void testCustomException() throws Exception {
        RestResponseBase response = restClient.get(domain + "/exception/get/custom")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.UNAUTHORIZED.code(), response.status());
        Assert.assertEquals("Custom", response.bodyToEntity(String.class));
    }
}
