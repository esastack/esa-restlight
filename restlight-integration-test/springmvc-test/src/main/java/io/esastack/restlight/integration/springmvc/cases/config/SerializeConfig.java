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

package io.esastack.restlight.integration.springmvc.cases.config;

import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.ProtoBufHttpBodySerializer;
import io.esastack.restlight.core.util.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializeConfig {

    @Bean
    public HttpBodySerializer bodySerializer() {
        return new ProtoBufHttpBodySerializer();
    }

    @Bean
    public HttpBodySerializer gsonHttpBodySerializer() {
        return new GsonHttpBodySerializer();
    }

    @Bean
    public HttpBodySerializer jacksonHttpBodySerializer() {
        return new JacksonHttpBodySerializer() {
            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }
        };
    }
}
