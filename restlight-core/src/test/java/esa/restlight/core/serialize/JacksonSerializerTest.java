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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JacksonSerializerTest {

    private ObjectMapper defaultObjectMapper;
    private JacksonSerializer defaultJacksonSerializer;
    private Pojo pojo;
    private byte[] pojoBytes;

    @BeforeEach
    void setUp() {
        defaultObjectMapper = JacksonSerializer.getDefaultMapper();
        defaultJacksonSerializer = new JacksonSerializer(defaultObjectMapper);
        pojo = new Pojo("pojo", 27);
        try {
            pojoBytes = defaultObjectMapper.writeValueAsBytes(pojo);
        } catch (JsonProcessingException ignore) {
        }
    }

    @Test
    void serialize() throws IOException {
        assertArrayEquals(defaultJacksonSerializer.serialize(pojo), pojoBytes);
        Pojo mock = mock(Pojo.class);
        when(mock.toString()).thenReturn(mock.getClass().getName());
        assertThrows(JsonProcessingException.class, () -> defaultJacksonSerializer.serialize(mock));
    }

    @Test
    void testSerialize() throws Exception {
        String pojoString = defaultObjectMapper.writeValueAsString(pojo);
        final MockAsyncResponse response = new MockAsyncResponse();
        defaultJacksonSerializer.serialize(pojo, response.outputStream());
        assertEquals(pojoString, response.getSentData().toString(StandardCharsets.UTF_8));
    }

    @Test
    void deserialize() throws IOException {
        assertEquals(pojo, defaultJacksonSerializer.deserialize(pojoBytes, Pojo.class));
    }

    @Test
    void testDeserialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        assertEquals(pojo, defaultJacksonSerializer.deserialize(request.inputStream(), Pojo.class));
    }

    @Test
    void deSerialize() throws IOException {
        assertEquals(pojo, defaultJacksonSerializer.deSerialize(pojoBytes, Pojo.class));
    }

    @Test
    void testDeSerialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        assertEquals(pojo, defaultJacksonSerializer.deSerialize(request.inputStream(), Pojo.class));
    }

    @Test
    void getDefaultMapper() {
        assertEquals(defaultObjectMapper, JacksonSerializer.getDefaultMapper());
    }
}
