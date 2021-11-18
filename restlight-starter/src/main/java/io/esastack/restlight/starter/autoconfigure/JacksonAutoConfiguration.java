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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.commons.reflect.ReflectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Configuration
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JacksonAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(JacksonProperties properties) {
        Map<Object, Boolean> features = new LinkedHashMap<>();
        safeConfig(() -> features.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false));
        safeConfig(() -> features.put(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false));

        ObjectMapper objectMapper = new ObjectMapper();
        safeConfig(() -> {
            if (properties.getDefaultPropertyInclusion() != null) {
                objectMapper.setDefaultPropertyInclusion(properties.getDefaultPropertyInclusion());
            }
        });

        safeConfig(() -> configureFeatures(properties.getDeserialization(), features));
        safeConfig(() -> configureFeatures(properties.getSerialization(), features));
        safeConfig(() -> configureFeatures(properties.getMapper(), features));
        safeConfig(() -> configureFeatures(properties.getParser(), features));
        safeConfig(() -> configureFeatures(properties.getGenerator(), features));
        safeConfig(() -> features.forEach((feature, enable) -> configureFeature(objectMapper, feature, enable)));
        safeConfig(() -> configureVisibility(objectMapper, properties.getVisibility()));
        safeConfig(() -> configureTimeZone(properties, objectMapper));
        safeConfig(() -> configureDateFormat(properties, objectMapper));
        safeConfig(() -> configureLocale(properties, objectMapper));
        safeConfig(() -> configurePropertyNamingStrategy(properties, objectMapper));
        return objectMapper;
    }

    private static void safeConfig(Runnable r) {
        try {
            r.run();
        } catch (IncompatibleClassChangeError t) {
            logger.debug("Failed to configure ObjectMapper of jackson.", t);
        }
    }

    private void configureDateFormat(JacksonProperties jacksonProperties, ObjectMapper objectMapper) {
        String dateFormat = jacksonProperties.getDateFormat();
        if (dateFormat != null) {
            Class<?> dateFormatClass = ClassUtils.forName(dateFormat);
            if (dateFormatClass == null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                TimeZone timeZone = jacksonProperties.getTimeZone();
                if (timeZone == null) {
                    timeZone = new ObjectMapper().getSerializationConfig().getTimeZone();
                }
                simpleDateFormat.setTimeZone(timeZone);
                objectMapper.setDateFormat(simpleDateFormat);
            } else {
                objectMapper.setDateFormat((DateFormat) BeanUtils.instantiateClass(dateFormatClass));
            }
        }
    }


    private void configurePropertyNamingStrategy(JacksonProperties jacksonProperties,
                                                 ObjectMapper objectMapper) {
        String strategy = jacksonProperties.getPropertyNamingStrategy();
        if (strategy != null) {
            Class<?> strategyClass = ClassUtils.forName(strategy);
            if (strategyClass == null) {
                configurePropertyNamingStrategyField(objectMapper, strategy);
            } else {
                configurePropertyNamingStrategyClass(objectMapper, strategyClass);
            }
        }
    }

    private void configureVisibility(ObjectMapper objectMapper,
                                     Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities) {
        visibilities.forEach(objectMapper::setVisibility);
    }

    private void configurePropertyNamingStrategyClass(ObjectMapper objectMapper,
                                                      Class<?> propertyNamingStrategyClass) {
        objectMapper.setPropertyNamingStrategy(
                (PropertyNamingStrategy) BeanUtils.instantiateClass(propertyNamingStrategyClass));
    }

    private void configurePropertyNamingStrategyField(ObjectMapper objectMapper, String fieldName) {
        // Find the field (this way we automatically support new constants
        // that may be added by Jackson in the future)
        Optional<Field> fieldOptional = ReflectionUtils.getAllDeclaredFields(PropertyNamingStrategy.class)
                .stream()
                .filter(field -> {
                    if (fieldName == null || fieldName.equals(Checks.checkNotNull(field).getName())) {
                        assert field != null;
                        return PropertyNamingStrategy.class.equals(field.getType());
                    }
                    return false;
                }).findAny();
        if (fieldOptional.isPresent()) {
            try {
                objectMapper.setPropertyNamingStrategy((PropertyNamingStrategy) fieldOptional.get().get(null));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            throw new NullPointerException("Constant named '" + fieldName + "' not found on "
                    + PropertyNamingStrategy.class.getName());
        }
    }

    private void configureLocale(JacksonProperties jacksonProperties, ObjectMapper objectMapper) {
        Locale locale = jacksonProperties.getLocale();
        if (locale != null) {
            objectMapper.setLocale(locale);
        } else {
            objectMapper.setLocale(Locale.CHINA);
        }
    }

    private void configureTimeZone(JacksonProperties jacksonProperties, ObjectMapper objectMapper) {
        TimeZone timeZone = jacksonProperties.getTimeZone();
        if (timeZone != null) {
            objectMapper.setTimeZone(timeZone);
        } else {
            objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        }
    }

    private void configureFeatures(Map<?, Boolean> propertiesFeatures, Map<Object, Boolean> features) {
        propertiesFeatures.forEach((feature, value) -> {
            if (value != null) {
                features.put(feature, value);
            }
        });
    }

    private void configureFeature(ObjectMapper objectMapper, Object feature, boolean enabled) {
        if (feature instanceof JsonParser.Feature) {
            objectMapper.configure((JsonParser.Feature) feature, enabled);
        } else if (feature instanceof JsonGenerator.Feature) {
            objectMapper.configure((JsonGenerator.Feature) feature, enabled);
        } else if (feature instanceof SerializationFeature) {
            objectMapper.configure((SerializationFeature) feature, enabled);
        } else if (feature instanceof DeserializationFeature) {
            objectMapper.configure((DeserializationFeature) feature, enabled);
        } else if (feature instanceof MapperFeature) {
            objectMapper.configure((MapperFeature) feature, enabled);
        } else {
            logger.warn("Unknown feature class: {}", feature.getClass().getName());
        }
    }
}
