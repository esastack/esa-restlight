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

package io.esastack.restlight.integration.jaxrs.cases.providers;

import io.esastack.restlight.integration.jaxrs.cases.resources.MessageBodyResource;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Component
public class BodyReader implements MessageBodyReader<UserData> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type.equals(MessageBodyResource.class)) {
            return true;
        }
        return false;
    }

    @Override
    public UserData readFrom(Class<UserData> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                             InputStream entityStream) throws WebApplicationException {
        return UserData.Builder.anUserData().name("test").build();
    }
}
