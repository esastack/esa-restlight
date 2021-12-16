package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class StrsNameAndValueResolverFactory extends NameAndValueResolverFactory<Collection<String>> {

    private final static NullPointerException BOTH_CONVERTERS_ARE_NULL = new NullPointerException(
            "Both strConverter and strsConverter are null");

    @Override
    protected BiFunction<String, Boolean, Object> initDefaultValueConverter(
            NameAndValueResolver.Converter<Collection<String>> converter) {
        return (defaultValue, isLazy) -> {
            if (defaultValue == null) {
                return null;
            }

            List<String> values = new ArrayList<>(1);
            values.add(defaultValue);
            if (isLazy) {
                return new NameAndValue.LazyDefaultValue(() ->
                        converter.convert(null, null, (name, ctx) -> values));
            }
            return converter.convert(null, null, (name, ctx) -> values);
        };
    }

    @Override
    protected NameAndValueResolver.Converter<Collection<String>> initConverter(Param param,
                                                                               BiFunction<Class<?>, Type, StringConverter> converterLookup) {
        final StringConverter strConverter = converterLookup.apply(param.type(), param.genericType());

        final Function<Collection<String>, Object> strsConverter = ConverterUtils.strs2ObjectConverter(param.type(),
                param.genericType(),
                converterLookup.andThen((converter) -> converter::fromString));

        if (strConverter == null && strsConverter == null) {
            throw BOTH_CONVERTERS_ARE_NULL;
        }

        return (name, ctx, valueProvider) -> {
            Collection<String> values = valueProvider.apply(name, ctx);
            if (values == null || values.isEmpty()) {
                return null;
            }
            if (strsConverter != null) {
                if ((values.size() > 1) || (strConverter == null)) {
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
