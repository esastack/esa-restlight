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
package esa.restlight.core.serialize;

import com.google.gson.Gson;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonSerializerTest {

    private GsonSerializer gsonSerializer;
    private Pojo pojo;
    private byte[] pojoBytes;
    private String pojoString;

    @BeforeEach
    void setUp() {
        gsonSerializer = new GsonSerializer();
        pojo = new Pojo("pojo", 27);
        Gson gson = new Gson();
        pojoBytes = gson.toJson(pojo).getBytes(StandardCharsets.UTF_8);
        pojoString = gson.toJson(pojo);
    }

    @Test
    void serialize() {
        assertArrayEquals(gsonSerializer.serialize(pojo), pojoBytes);
    }

    @Test
    void testSerialize() throws Exception {
        final MockAsyncResponse response = new MockAsyncResponse();
        final HttpOutputStream outputStream = response.outputStream();
        gsonSerializer.serialize(pojo, outputStream);
        outputStream.flush();
        assertEquals(pojoString, response.getSentData().toString(StandardCharsets.UTF_8));
    }

    @Test
    void deSerialize() {
        assertEquals(gsonSerializer.deSerialize(pojoBytes, Pojo.class), pojo);
    }

    @Test
    void testDeSerialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        assertEquals(pojo, gsonSerializer.deSerialize(request.inputStream(), Pojo.class));
    }
}
