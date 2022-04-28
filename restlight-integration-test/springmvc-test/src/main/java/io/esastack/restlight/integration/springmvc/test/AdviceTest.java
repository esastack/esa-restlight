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

package io.esastack.restlight.integration.springmvc.test;

import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import org.junit.Assert;
import org.junit.Test;

public class AdviceTest extends BaseIntegrationTest {

    @Test
    public void testCustomParamAdviceByFactory() throws Exception {
        String name = "test";
        RestResponseBase response = restClient.get(domain + "/advice/get/param/factory")
                .addParam("name", name).execute().toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        Assert.assertEquals(name + "-advice-factory", user.getName());
    }

    @Test
    public void testCustomParamAdviceByAdaptor() throws Exception {
        String name = "test";
        RestResponseBase response = restClient.get(domain + "/advice/get/param/adaptor")
                .addParam("name", name).execute().toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        Assert.assertEquals(name + "-advice-adaptor", user.getName());
    }

    @Test
    public void testCustomEntityAdviceByFactory() throws Exception {
        UserData user = UserData.Builder.anUserData()
                .name("test").build();
        RestResponseBase response = restClient.post(domain + "/advice/post/entity/factory")
                .entity(user).execute().toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals(user.getName() + "-advice-factory", userData.getName());
    }

    @Test
    public void testCustomEntityAdviceByAdaptor() throws Exception {
        UserData user = UserData.Builder.anUserData()
                .name("test").build();
        RestResponseBase response = restClient.post(domain + "/advice/post/entity/adaptor")
                .entity(user).execute().toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        Assert.assertEquals(user.getName() + "-advice-adaptor", userData.getName());
    }

    @Test
    public void testCustomResponseEntityAdviceByFactory() throws Exception {
        RestResponseBase response = restClient.get(domain + "/advice/get/response/entity/factory")
                .addParam("name", "test").execute().toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test-advice-factory", user.getName());
    }

    @Test
    public void testCustomResponseEntityAdviceByAdaptor() throws Exception {
        RestResponseBase response = restClient.get(domain + "/advice/get/response/entity/adaptor")
                .addParam("name", "test").execute().toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        Assert.assertEquals("test-advice-adaptor", user.getName());
    }
}