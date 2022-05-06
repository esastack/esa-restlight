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
package io.esastack.restlight.starter.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JacksonAutoConfigurationTest {

    @Test
    void objectMapper() {
        final JacksonProperties properties = mock(JacksonProperties.class);
        when(properties.getDateFormat()).thenReturn("yyyy-MM-dd HH:mm:ss");
        when(properties.getTimeZone()).thenReturn(TimeZone.getTimeZone("GMT"));
        when(properties.getLocale()).thenReturn(Locale.CHINESE);
        when(properties.getPropertyNamingStrategy()).thenReturn("SNAKE_CASE");
        when(properties.getDefaultPropertyInclusion()).thenReturn(JsonInclude.Include.NON_NULL);

        Map<DeserializationFeature, Boolean> deserialization = new HashMap<>();
        deserialization.put(DeserializationFeature.ACCEPT_FLOAT_AS_INT, true);
        when(properties.getDeserialization()).thenReturn(deserialization);

        Map<JsonParser.Feature, Boolean> parser = new HashMap<>();
        parser.put(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        when(properties.getParser()).thenReturn(parser);

        Map<JsonGenerator.Feature, Boolean> generator = new HashMap<>();
        generator.put(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        when(properties.getGenerator()).thenReturn(generator);

        Map<SerializationFeature, Boolean> serialization = new HashMap<>();
        serialization.put(SerializationFeature.INDENT_OUTPUT, true);
        when(properties.getSerialization()).thenReturn(serialization);

        Map<MapperFeature, Boolean> mapper = new HashMap<>();
        mapper.put(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        when(properties.getMapper()).thenReturn(mapper);

        final JacksonAutoConfiguration jacksonAutoConfiguration = new JacksonAutoConfiguration();
        ObjectMapper objectMapper = jacksonAutoConfiguration.objectMapper(properties);
        assertEquals("yyyy-MM-dd HH:mm:ss", ((SimpleDateFormat) objectMapper.getDateFormat()).toPattern());
        assertEquals(TimeZone.getTimeZone("GMT").getID(), objectMapper.getDateFormat().getTimeZone().getID());
        assertEquals(JsonInclude.Include.NON_NULL,
                objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getContentInclusion());
        assertEquals(Locale.CHINESE, objectMapper.getSerializationConfig().getLocale());
        assertEquals(objectMapper.getPropertyNamingStrategy(), PropertyNamingStrategy.SNAKE_CASE);
        assertFalse(objectMapper.getSerializationConfig()
                .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(objectMapper.getSerializationConfig()
                .isEnabled(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS));
        assertTrue(objectMapper.getDeserializationConfig()
                .isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT));
        assertTrue(objectMapper.getFactory()
                .isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
        assertTrue(objectMapper.getFactory()
                .isEnabled(JsonGenerator.Feature.IGNORE_UNKNOWN));
        assertTrue(objectMapper.getSerializationConfig()
                .isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(objectMapper.getSerializationConfig()
                .isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertTrue(objectMapper.getDeserializationConfig()
                .isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));


        when(properties.getTimeZone()).thenReturn(null);
        ObjectMapper objectMapperTimeZone = jacksonAutoConfiguration.objectMapper(properties);
        assertEquals(TimeZone.getTimeZone("GMT+8").getID(),
                objectMapperTimeZone.getSerializationConfig().getDateFormat().getTimeZone().getID());
        assertEquals(TimeZone.getTimeZone("GMT+8").getID(),
                objectMapperTimeZone.getSerializationConfig().getTimeZone().getID());


        when(properties.getDateFormat()).thenReturn("java.text.SimpleDateFormat");
        ObjectMapper objectMapperDateFormat = jacksonAutoConfiguration.objectMapper(properties);
        assertTrue(objectMapperDateFormat.getDateFormat() instanceof SimpleDateFormat);
        when(properties.getDateFormat()).thenReturn(null);
        ObjectMapper objectMapperDateFormatNull = jacksonAutoConfiguration.objectMapper(properties);
        assertTrue(objectMapperDateFormatNull.getDateFormat() instanceof SimpleDateFormat);
        assertEquals("yyyy-MM-dd HH:mm:ss", ((SimpleDateFormat)
                objectMapperDateFormatNull.getDateFormat()).toPattern());


        when(properties.getLocale()).thenReturn(null);
        ObjectMapper objectMapperLocale = jacksonAutoConfiguration.objectMapper(properties);
        assertEquals(Locale.CHINA, objectMapperLocale.getSerializationConfig().getLocale());


        when(properties.getPropertyNamingStrategy()).thenReturn(null);
        ObjectMapper objectMapperPropertyNamingStrategyNull = jacksonAutoConfiguration.objectMapper(properties);
        assertNull(objectMapperPropertyNamingStrategyNull.getPropertyNamingStrategy());
        when(properties.getPropertyNamingStrategy()).thenReturn("xxx");
        assertThrows(NullPointerException.class, () -> jacksonAutoConfiguration.objectMapper(properties));
        when(properties.getPropertyNamingStrategy())
                .thenReturn("com.fasterxml.jackson.databind.PropertyNamingStrategy$UpperCamelCaseStrategy");
        ObjectMapper objectMapperUpperCamelCaseStrategy = jacksonAutoConfiguration.objectMapper(properties);
        assertTrue(objectMapperUpperCamelCaseStrategy.getSerializationConfig()
                .getPropertyNamingStrategy() instanceof PropertyNamingStrategy.UpperCamelCaseStrategy);


    }
}
