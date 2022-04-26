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

package io.esastack.restlight.integration.jaxrs.test;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import org.junit.Assert;
import org.junit.Test;

public class ValidationTest extends BaseIntegrationTest {

    @Test
    public void testRequestParam() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/validation/request/param").addParam("name", "test")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());

        responseBase = restClient.get(domain + "/validation/request/param").execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), responseBase.status());
    }

    @Test
    public void testRequestEntity() throws Exception {
        UserData entity = UserData.Builder.anUserData().build();
        RestResponseBase responseBase = restClient.post(domain + "/validation/request/entity").entity(entity)
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testResponseParam() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/validation/response/param")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), responseBase.status());
    }

    @Test
    public void testResponseEntity() throws Exception {
        UserData entity = UserData.Builder.anUserData().build();
        RestResponseBase responseBase = restClient.post(domain + "/validation/response/entity").entity(entity)
                .execute().toCompletableFuture().get();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), responseBase.status());
    }
}
