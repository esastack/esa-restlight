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

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restclient.RestResponseBase;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationTest extends BaseIntegrationTest {

    @Test
    void testGet() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get").addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testPost() throws Exception {
        UserData user = UserData.Builder.anUserData()
                .name("test").age(10).birthDay(new Date())
                .weight(BigDecimal.valueOf(123.01)).build();
        RestResponseBase response = restClient.post(domain + "/annotation/post").entity(user).execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        assertEquals(user.getName(), userResult.getName());
    }

    @Test
    void testDelete() throws Exception {
        RestResponseBase response = restClient.delete(domain + "/annotation/delete/test").execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        assertEquals("test", userResult.getName());
    }

    @Test
    void testPut() throws Exception {
        RestResponseBase response = restClient.put(domain + "/annotation/put").addCookie("name", "test").execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        assertEquals("test", userResult.getName());
    }

    @Test
    void testQueryBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/querybean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testRequestBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/requestbean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testHeader() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/header")
                .addHeader("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testMatrix() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/matrix/10;name=test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
        assertTrue(10 == userData.getAge());
    }

    @Test
    void testAttribute() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/attribute").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testResponseStatus() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/responsestatus").execute()
                .toCompletableFuture().get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }

    @Test
    void testCustomBean() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/custom/bean")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData userData = response.bodyToEntity(UserData.class);
        assertEquals("test", userData.getName());
    }

    @Test
    void testCustomBody() throws Exception {
        UserData user = UserData.Builder.anUserData()
                .name("test").age(10).birthDay(new Date())
                .weight(BigDecimal.valueOf(123.01)).build();
        RestResponseBase response = restClient.post(domain + "/annotation/post/custom/body").entity(user).execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        assertEquals(user.getName(), userResult.getName());
    }

    @Test
    void testCustomResponseBody() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/custom/responsebody")
                .addParam("name", "test").execute()
                .toCompletableFuture().get();
        UserData user = response.bodyToEntity(UserData.class);
        assertEquals("test", user.getName());
    }

    @Test
    void testParamWrong() throws Exception {
        RestResponseBase response = restClient.get(domain + "/annotation/get/param/wrong")
                .addParam("user", "").execute().toCompletableFuture().get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }
}
