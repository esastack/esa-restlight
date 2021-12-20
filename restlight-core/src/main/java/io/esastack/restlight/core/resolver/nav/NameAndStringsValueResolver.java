package io.esastack.restlight.core.resolver.nav;

import esa.commons.Checks;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class NameAndStringsValueResolver implements NameAndValueResolver {

    private static final NullPointerException BOTH_CONVERTERS_ARE_NULL = new NullPointerException(
            "Both strConverter and strsConverter are null");

    private final StringConverter strConverter;
    private final Function<Collection<String>, Object> strsConverter;
    private final BiFunction<String, RequestContext, Collection<String>> valueExtractor;
    private final NameAndValue<Object> nav;

    public NameAndStringsValueResolver(Param param,
                                       HandlerResolverFactory resolverFactory,
                                       BiFunction<String, RequestContext, Collection<String>> valueExtractor,
                                       NameAndValue<String> nav) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        this.valueExtractor = Checks.checkNotNull(valueExtractor, "valueExtractor");
        this.strConverter = resolverFactory.getStringConverter(param.type(),
                param.genericType(),
                param);

        BiFunction<Class<?>, Type, StringConverter> converterLookup = (baseType, baseGenericType) ->
                resolverFactory.getStringConverter(baseType, baseGenericType, param);

        this.strsConverter = ConverterUtils.strs2ObjectConverter(param.type(),
                param.genericType(),
                converterLookup.andThen((converter) -> converter::fromString));
        if (strConverter == null && strsConverter == null) {
            throw BOTH_CONVERTERS_ARE_NULL;
        }

        Supplier<String> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            this.nav = new NameAndValue<>(nav.name(),
                    nav.required(),
                    null);
        } else {
            if (strConverter == null) {
                this.nav = new NameAndValue<>(nav.name(),
                        nav.required(),
                        strsConverter.apply(Collections.singletonList(nav.defaultValue().get())));
            } else {
                this.nav = new NameAndValue<>(nav.name(),
                        nav.required(),
                        () -> strConverter.fromString(defaultValue.get()),
                        strConverter.isLazy());
            }
        }
    }

    @Override
    public Object resolve(String name, RequestContext ctx) {
        return resolve(valueExtractor.apply(name, ctx));
    }

    @Override
    public NameAndValue<Object> createNameAndValue(Param param) {
        return nav;
    }

    private Object resolve(Collection<String> values) {
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
    }
}
