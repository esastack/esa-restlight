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
package io.esastack.restlight.spring.serialize;

import com.google.gson.GsonBuilder;
import io.esastack.restlight.core.serialize.BaseHttpBodySerializer;
import io.esastack.restlight.core.serialize.GsonSerializer;
import io.esastack.restlight.core.serialize.Serializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class GsonHttpBodySerializerAdapter extends BaseHttpBodySerializer implements InitializingBean {

    @Autowired
    private GsonBuilder builder;
    private Serializer serializer;

    @Override
    protected Serializer serializer() {
        return this.serializer;
    }

    @Override
    public void afterPropertiesSet() {
        this.serializer = new GsonSerializer(builder);
    }
}
