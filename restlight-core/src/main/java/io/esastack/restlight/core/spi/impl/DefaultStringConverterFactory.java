package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

public class DefaultStringConverterFactory implements StringConverterFactory {

    @Override
    public Optional<StringConverter> createConverter(Class<?> type, Type genericType, Param relatedParam) {
        Function<String, Object> converter = ConverterUtils.str2ObjectConverter(genericType);

        if (converter == null) {
            return Optional.empty();
        }

        return Optional.of(new StringConverter() {
            @Override
            public Object fromString(String value) {
                return converter.apply(value);
            }

            @Override
            public boolean isLazy() {
                return false;
            }
        });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
