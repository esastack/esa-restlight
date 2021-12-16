package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class DefaultStringConverterFactory implements StringConverterFactory {
    @Override
    public Optional<StringConverter> createConverter(Param param, Class<?> baseType, Type baseGenericType) {
        return Optional.of(ConverterUtils.str2ObjectConverter(baseType)::apply);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
