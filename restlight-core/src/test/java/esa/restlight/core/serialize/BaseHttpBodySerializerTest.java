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
import esa.restlight.core.util.MediaType;
import esa.restlight.core.util.Ordered;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BaseHttpBodySerializerTest {

    private JacksonSerializer serializer;
    private Pojo pojo;
    private byte[] pojoBytes;
    private BaseHttpBodySerializer baseHttpBodySerializer;

    @BeforeEach
    void setUp() {
        serializer = new JacksonSerializer();
        pojo = new Pojo("pojo", 27);
        try {
            pojoBytes = serializer.serialize(pojo);
        } catch (JsonProcessingException ignore) {
        }
        baseHttpBodySerializer = new BaseHttpBodySerializer() {
            @Override
            protected Serializer serializer() {
                return serializer;
            }
        };
    }

    @Test
    void supportsRead() {
        assertFalse(baseHttpBodySerializer.supportsRead(null, null));
        assertFalse(baseHttpBodySerializer.supportsRead(MediaType.APPLICATION_XML, null));
        assertTrue(baseHttpBodySerializer.supportsRead(MediaType.ALL, null));
        assertTrue(baseHttpBodySerializer.supportsRead(MediaType.APPLICATION_JSON, null));
    }

    @Test
    void supportsWrite() {
        assertFalse(baseHttpBodySerializer.supportsWrite(null, null));
        assertFalse(baseHttpBodySerializer.supportsWrite(MediaType.APPLICATION_XML, null));
        assertTrue(baseHttpBodySerializer.supportsWrite(MediaType.ALL, null));
        assertTrue(baseHttpBodySerializer.supportsWrite(MediaType.APPLICATION_JSON, null));
    }

    @Test
    void preferStream() {
        assertFalse(baseHttpBodySerializer.preferStream());
    }

    @Test
    void getOrder() {
        assertEquals(Ordered.LOWEST_PRECEDENCE, baseHttpBodySerializer.getOrder());
    }

    @Test
    void serializer() {
        assertEquals(serializer, baseHttpBodySerializer.serializer());
    }

    @Test
    void customResponse() {
        final MockAsyncResponse response = new MockAsyncResponse();
        final MockAsyncRequest request = new MockAsyncRequest();
        final Object o = baseHttpBodySerializer.customResponse(request, response, pojo);
        assertEquals(pojo, o);
        assertEquals(response.getHeader(HttpHeaderNames.CONTENT_TYPE), MediaType.APPLICATION_JSON_UTF8.value());
    }

    @Test
    void serialize() throws Exception {
        final byte[] serialize = baseHttpBodySerializer.serialize(pojo);
        assertArrayEquals(serialize, pojoBytes);
    }

    @Test
    void deserialize() throws Exception {
        final Pojo pojoReduction = baseHttpBodySerializer.deserialize(pojoBytes, Pojo.class);
        assertNull(baseHttpBodySerializer.deserialize((byte[]) null, Pojo.class));
        assertNull(baseHttpBodySerializer.deserialize(new byte[]{}, Pojo.class));
        assertEquals(pojo, pojoReduction);
    }

    @Test
    void deSerialize() throws Exception {
        final Pojo pojoReduction = baseHttpBodySerializer.deSerialize(pojoBytes, Pojo.class);
        assertNull(baseHttpBodySerializer.deSerialize((byte[]) null, Pojo.class));
        assertNull(baseHttpBodySerializer.deSerialize(new byte[]{}, Pojo.class));
        assertEquals(pojo, pojoReduction);
    }

    @Test
    void testSerialize() throws Exception {
        final String pojoString = JacksonSerializer.getDefaultMapper().writeValueAsString(pojo);
        final MockAsyncResponse response = new MockAsyncResponse();
        baseHttpBodySerializer.serialize(pojo, response.outputStream());
        assertEquals(pojoString, response.getSentData().toString(StandardCharsets.UTF_8));
    }

    @Test
    void testDeserialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        final Pojo pojoReduction = serializer.deserialize(request.inputStream(), Pojo.class);
        assertEquals(pojo, pojoReduction);
    }

    @Test
    void testDeSerialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        final Pojo pojoReduction = serializer.deSerialize(request.inputStream(), Pojo.class);
        assertEquals(pojo, pojoReduction);
    }
}
