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

public class FailFastTest extends BaseIntegrationTest {

    @Test
    public void testQueued() throws Exception {
        RestResponseBase response = restClient.get(domain + "/failfast/get/queued").execute()
                .toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }

    @Test
    public void testTTFB() throws Exception {
        RestResponseBase response = restClient.post(domain + "/failfast/post/ttfb")
                .entity(new byte[1024 * 1024 * 4])
                .execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }
}
