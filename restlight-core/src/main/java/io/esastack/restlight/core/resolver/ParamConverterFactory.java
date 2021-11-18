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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface ParamConverterFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link ParamConverterAdapter} to {@link ParamConverterFactory} which
     * always use the given {@link ParamConverterAdapter} as the result of
     * {@link #createConverter(Param, List)}
     *
     * @param converter converter
     * @return of factory bean
     */
    static ParamConverterFactory singleton(ParamConverterAdapter converter) {
        return new ParamConverterFactory.Singleton(converter);
    }

    /**
     * Creates an instance of {@link ParamConverter} for given {@link Param}.
     *
     * @param param param
     * @param serializers all the {@link HttpRequestSerializer}s in the context
     * @return resolver
     */
    ParamConverter createConverter(Param param, List<? extends HttpRequestSerializer> serializers);

    class Singleton implements ParamConverterFactory {

        private final ParamConverterAdapter converter;

        Singleton(ParamConverterAdapter converter) {
            Checks.checkNotNull(converter, "resolver");
            this.converter = converter;
        }

        @Override
        public boolean supports(Param param) {
            return converter.supports(param);
        }

        @Override
        public ParamConverter createConverter(Param param,
                                              List<? extends HttpRequestSerializer> serializers) {
            return converter;
        }
    }

}

