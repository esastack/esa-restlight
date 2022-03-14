/*
 * Copyright 2022 OPPO ESA Stack Project
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

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class StringConverterProviderAdapterTest {

    @Test
    void testAll() {
        assertThrows(NullPointerException.class, () -> new StringConverterProviderAdapter(null, 100));

        final ParamConverterProvider provider = mock(ParamConverterProvider.class);
        final StringConverterProviderAdapter adapter = new StringConverterProviderAdapter(provider, 100);
        assertEquals(100, adapter.getOrder());

        assertFalse(adapter.createConverter(StringConverterFactory.Key.of(null, null,
                mock(Param.class))).isPresent());

        doReturn(new ParamConverterImpl()).when(provider).getConverter(any(), any(), any());
        Optional<StringConverter> converter = adapter.createConverter(StringConverterFactory.Key
                .of(null, null, mock(Param.class)));
        assertTrue(converter.isPresent());
        assertEquals("ABC", converter.get().fromString(null));
        assertTrue(converter.get().isLazy());
    }

    @ParamConverter.Lazy
    private static final class ParamConverterImpl implements ParamConverter<String> {

        @Override
        public String fromString(String value) {
            return "ABC";
        }

        @Override
        public String toString(String value) {
            return "DEF";
        }
    }

}

