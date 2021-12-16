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
package io.esastack.restlight.core.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpOutputStream;

import java.lang.reflect.Type;

public class FastJsonSerializer implements JsonSerializer {

    static {
        //global date format
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteDateUseDateFormat.getMask();
    }

    @Override
    public byte[] serialize(Object target) {
        return JSON.toJSONBytes(target);
    }

    @Override
    public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
        JSON.writeJSONString(outputStream, target);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return JSON.parseObject(data, type);
    }

    @Override
    public <T> T deserialize(HttpInputStream inputStream, Type type) throws Exception {
        return JSON.parseObject(inputStream, type);
    }
}
