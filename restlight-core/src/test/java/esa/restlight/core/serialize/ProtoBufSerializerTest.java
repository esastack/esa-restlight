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

import esa.httpserver.core.HttpOutputStream;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtoBufSerializerTest {

    private ProtoBufSerializer protoBufSerializer;
    private PojoProtobuf.Pojo pojo;
    private byte[] pojoBytes;

    @BeforeEach
    void setUp() {
        protoBufSerializer = new ProtoBufSerializer();
        PojoProtobuf.Pojo.Builder pojoBuilder = PojoProtobuf.Pojo.newBuilder().setName("pojo").setAge(27);
        pojo = pojoBuilder.build();
        pojoBytes = pojo.toByteArray();
    }

    @Test
    void serialize() {
        assertNull(protoBufSerializer.serialize(null));
        assertThrows(UnsupportedOperationException.class,
                () -> protoBufSerializer.serialize(new Pojo("pojo", 27)));
        assertArrayEquals(protoBufSerializer.serialize(pojo), pojoBytes);
    }

    @Test
    void testSerialize() throws Exception {
        final MockAsyncResponse response = new MockAsyncResponse();
        final HttpOutputStream outputStream = response.outputStream();
        protoBufSerializer.serialize(pojo, outputStream);
        outputStream.flush();
        final byte[] serialize = new byte[response.getSentData().readableBytes()];
        response.getSentData().readBytes(serialize);
        assertArrayEquals(serialize, pojoBytes);
    }

    @Test
    void deSerialize() throws Exception {
        final PojoProtobuf.Pojo pojoReduction = protoBufSerializer.deSerialize(pojoBytes, PojoProtobuf.Pojo.class);
        assertEquals(pojo.getName(), pojoReduction.getName());
        assertEquals(pojo.getAge(), pojoReduction.getAge());
    }

    @Test
    void testDeSerialize() throws Exception {
        final MockAsyncRequest.Builder builder = MockAsyncRequest.aMockRequest();
        final MockAsyncRequest request = builder.withBody(pojoBytes).build();
        final PojoProtobuf.Pojo pojoReduction = protoBufSerializer.deSerialize(request.inputStream(),
                PojoProtobuf.Pojo.class);
        assertEquals(pojo.getName(), pojoReduction.getName());
        assertEquals(pojo.getAge(), pojoReduction.getAge());
    }
}
