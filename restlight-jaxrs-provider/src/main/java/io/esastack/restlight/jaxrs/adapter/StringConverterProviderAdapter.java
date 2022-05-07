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
import esa.commons.ClassUtils;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import java.util.Optional;

public class StringConverterProviderAdapter implements StringConverterFactory {

    private final ParamConverterProvider underlying;
    private final int order;

    public StringConverterProviderAdapter(ParamConverterProvider underlying, int order) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
        this.order = order;
    }

    @Override
    public Optional<StringConverter> createConverter(Key key) {
        ParamConverter<?> converter = underlying.getConverter(key.type(), key.genericType(),
                key.param().annotations());
        if (converter == null) {
            return Optional.empty();
        }

        boolean isLazy = AnnotationUtils.hasAnnotation(ClassUtils.getUserType(converter), ParamConverter.Lazy.class);
        return Optional.of(new StringConverter() {
            @Override
            public Object fromString(String value) {
                return converter.fromString(value);
            }

            @Override
            public boolean isLazy() {
                return isLazy;
            }
        });
    }

    @Override
    public int getOrder() {
        return order;
    }
}

