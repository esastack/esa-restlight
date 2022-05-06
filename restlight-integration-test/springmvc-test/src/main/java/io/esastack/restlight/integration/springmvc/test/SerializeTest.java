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
import io.esastack.restclient.codec.impl.ProtoBufCodec;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import io.esastack.restlight.integration.springmvc.entity.UserProtobufData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializeTest extends BaseIntegrationTest {

    @Test
    void testJson() throws Exception {
        UserData user = UserData.Builder.anUserData()
                .name("test").age(10).birthDay(new Date())
                .weight(BigDecimal.valueOf(123.01)).build();
        RestResponseBase response = restClient.post(domain + "/serialize/json").entity(user).execute()
                .toCompletableFuture().get();
        UserData userResult = response.bodyToEntity(UserData.class);
        assertEquals(user.getName(), userResult.getName());
    }

    @Test
    void testProtobuf() throws Exception {
        UserProtobufData.Data user = UserProtobufData.Data
                .newBuilder().setName("test").setAge(11).build();
        RestResponseBase response = restClient.post(domain + "/serialize/protobuf").addParam("format", "pb")
                .encoder(new ProtoBufCodec()).decoder(new ProtoBufCodec()).entity(user)
                .contentType(ProtoBufCodec.PROTO_BUF)
                .execute().toCompletableFuture().get();
        UserProtobufData.Data userResult = response.bodyToEntity(UserProtobufData.Data.class);
        assertEquals(user.getName(), userResult.getName());
    }
}
