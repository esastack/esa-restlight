package io.esastack.restlight.core.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;

import java.util.List;
import java.util.Optional;

public class DefaultStringConverterFactory implements StringConverterFactory {
    @Override
    public Optional<StringConverter> createConverter(Param param, List<? extends HttpRequestSerializer> serializers) {
        return Optional.of(ConverterUtils.str2ObjectConverter(param.type())::apply);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
