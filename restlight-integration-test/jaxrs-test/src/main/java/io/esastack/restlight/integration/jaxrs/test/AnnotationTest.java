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
import io.esastack.restlight.integration.jaxrs.cases.resources.AnnotationResource;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.impl.ext.ProvidersImpl;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class AnnotationTest extends BaseIntegrationTest {

    @Test
    public void testGet() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get").addParam("name", "test")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testPost() throws Exception {
        UserData entity = UserData.Builder.anUserData()
                .name("test").build();
        RestResponseBase responseBase = restClient.post(domain + "/annotation/post").entity(entity)
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals(entity.getName(), userData.getName());
    }

    @Test
    public void testDelete() throws Exception {
        RestResponseBase responseBase = restClient.delete(domain + "/annotation/delete").addParam("name", "test")
                .addHeader("age", "10").addCookie("weight", "100").execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
        Assert.assertTrue(10 == userData.getAge());
        Assert.assertTrue(BigDecimal.valueOf(100).equals(userData.getWeight()));
    }

    @Test
    public void testPut() throws Exception {
        RestResponseBase responseBase = restClient.put(domain + "/annotation/put/test")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testMatrix() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/matrix/10;name=test")
                .execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
        Assert.assertTrue(10 == userData.getAge());
    }

    @Test
    public void testContextHeaders() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/headers")
                .addHeader("name", "test").execute().toCompletableFuture().get();
        UserData userData = responseBase.bodyToEntity(UserData.class);
        Assert.assertEquals("test", userData.getName());
    }

    @Test
    public void testContextProviders() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/providers")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(ProvidersImpl.class.getName(), responseBase.bodyToEntity(String.class));
    }

    @Test
    public void testRequest() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/request")
                .execute().toCompletableFuture().get();
        Assert.assertEquals("GET", responseBase.bodyToEntity(String.class));
    }

    @Test
    public void testResource() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/resource")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(AnnotationResource.class.getName(), responseBase.bodyToEntity(String.class));
    }

    @Test
    public void testUri() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/uri")
                .execute().toCompletableFuture().get();
        Assert.assertEquals("/integration/test/annotation/get/context/uri", responseBase.bodyToEntity(String.class));
    }

    @Test
    public void testConfiguration() throws Exception {
        RestResponseBase responseBase = restClient.get(domain + "/annotation/get/context/configuration")
                .execute().toCompletableFuture().get();
        Assert.assertEquals(ConfigurationImpl.class.getName(), responseBase.bodyToEntity(String.class));
    }
}
