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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpOutputStream;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GsonSerializer implements JsonSerializer {

    private final Gson gson;

    public GsonSerializer() {
        this(null);
    }

    public GsonSerializer(GsonBuilder gsonBuilder) {
        if (gsonBuilder != null) {
            gson = gsonBuilder.create();
        } else {
            gson = new GsonBuilder().setDateFormat(DEFAULT_DATE_FORMAT).create();
        }
    }

    @Override
    public byte[] serialize(Object target) {
        return gson.toJson(target).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
        outputStream.write(gson.toJson(target).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), type);
    }

    @Override
    public <T> T deserialize(HttpInputStream inputStream, Type type) throws Exception {
        return gson.fromJson(inputStream.readString(StandardCharsets.UTF_8), type);
    }
}
