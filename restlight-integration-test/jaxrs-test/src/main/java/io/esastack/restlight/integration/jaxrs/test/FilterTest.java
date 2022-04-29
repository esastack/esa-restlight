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

import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterTest extends BaseIntegrationTest {

    @Test
    public void testRequestGlobal() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/filter/request/global")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    public void testResponseGlobal() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/filter/response/global")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    public void testRequestBinding() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/filter/request/binding")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    public void testResponseBinding() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/filter/response/binding")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    public void testDynamicFeature() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/filter/dynamic/feature")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }
}
