package io.esastack.restlight.core.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class StrsConverterAdapter extends StringConverterAdapter<Collection<String>> {

    private final static NullPointerException BOTH_CONVERTERS_ARE_NULL = new NullPointerException(
            "Both strConverter and strsConverter are null");

    @Override
    protected BiFunction<String, Boolean, Object> createDefaultValueConverter(Function<Collection<String>, Object> converter) {
        return (defaultValue, isLazy) -> {
            if (defaultValue == null) {
                return null;
            }

            List<String> values = new ArrayList<>(1);
            values.add(defaultValue);
            if (isLazy) {
                return new LazyDefaultValue(() -> converter.apply(values));
            }
            return converter.apply(values);
        };
    }

    @Override
    protected Function<Collection<String>, Object> initConverter(Param param,
                                                                 BiFunction<Class<?>, Type, StringConverter> converterLookup) {
        final StringConverter strConverter = converterLookup.apply(param.type(), param.genericType());

        final Function<Collection<String>, Object> strsConverter = ConverterUtils.strs2ObjectConverter(param.type(),
                param.genericType(),
                converterLookup.andThen((converter) -> converter::fromString));

        if (strConverter == null && strsConverter == null) {
            throw BOTH_CONVERTERS_ARE_NULL;
        }

        return (values) -> {
            if (strsConverter != null) {
                if (values == null || values.isEmpty()) {
                    return null;
                } else if ((values.size() > 1) || (strConverter == null)) {
                    return strsConverter.apply(values);
                } else {
                    return strConverter.fromString(values.iterator().next());
                }
            } else {
                return strConverter.fromString(values.iterator().next());
            }
        };
    }
}
