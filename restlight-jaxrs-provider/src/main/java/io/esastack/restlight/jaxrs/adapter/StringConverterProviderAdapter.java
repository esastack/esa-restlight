/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import jakarta.ws.rs.ext.ParamConverterProvider;

import java.util.List;

public class StringConverterProviderAdapter implements StringConverterFactory {

    private final ParamConverterProvider underlying;

    public StringConverterProviderAdapter(ParamConverterProvider underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public StringConverter createConverter(Param param, List<? extends HttpRequestSerializer> serializers) {
        jakarta.ws.rs.ext.ParamConverter<?> converter = underlying.getConverter(param.type(),
                param.genericType(), param.annotations());
        if (converter == null) {
            return null;
        }
        return converter::fromString;
    }

    @Override
    public boolean supports(Param param) {
        return true;
    }
}

