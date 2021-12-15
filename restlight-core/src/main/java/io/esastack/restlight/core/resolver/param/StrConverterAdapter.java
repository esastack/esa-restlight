package io.esastack.restlight.core.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class StrConverterAdapter extends StringConverterAdapter<String> {

    @Override
    protected BiFunction<String, Boolean, Object> createDefaultValueConverter(Function<String, Object> converter) {
        return (defaultValue, isLazy) -> {
            if (isLazy) {
                return new LazyDefaultValue(() -> converter.apply(defaultValue));
            }
            return converter.apply(defaultValue);
        };
    }

    @Override
    protected Function<String, Object> initConverter(Param param, BiFunction<Class<?>, Type, StringConverter> converterLookup) {
        StringConverter converter = converterLookup.apply(param.type(), param.genericType());
        if (converter == null) {
            return null;
        }
        return converter::fromString;
    }
}
