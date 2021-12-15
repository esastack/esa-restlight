package io.esastack.restlight.core.resolver.param;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

public abstract class StrNameAndValueResolverFactory extends NameAndValueResolverFactory<String> {

    @Override
    protected BiFunction<String, Boolean, Object> initDefaultValueConverter(
            NameAndValueResolver.Converter<String> converter) {
        return (defaultValue, isLazy) -> {
            if (isLazy) {
                return new NameAndValue.LazyDefaultValue(() ->
                        converter.convert(null, null, (name, ctx) -> defaultValue));
            }
            return converter.convert(null, null, (name, ctx) -> defaultValue);
        };
    }

    @Override
    protected NameAndValueResolver.Converter<String> initConverter(Param param,
                                                                   BiFunction<Class<?>, Type, StringConverter> converterLookup) {
        StringConverter converter = converterLookup.apply(param.type(), param.genericType());
        if (converter == null) {
            return null;
        }
        return (name, ctx, valueExtractor) -> converter.fromString(valueExtractor.apply(name, ctx));
    }
}
