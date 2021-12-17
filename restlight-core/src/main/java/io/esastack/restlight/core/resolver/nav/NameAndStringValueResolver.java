package io.esastack.restlight.core.resolver.nav;

import esa.commons.Checks;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;

import java.util.function.BiFunction;

public class NameAndStringValueResolver implements NameAndValueResolver {

    private final StringConverter converter;
    public final BiFunction<String, RequestContext, String> valueExtractor;
    public final NameAndValue<Object> nav;

    public NameAndStringValueResolver(Param param,
                                      HandlerResolverFactory resolverFactory,
                                      BiFunction<String, RequestContext, String> valueExtractor,
                                      NameAndValue<String> nav) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        this.converter = Checks.checkNotNull(resolverFactory.getStringConverter(param,
                param.type(),
                param.genericType()), "converter");
        this.valueExtractor = Checks.checkNotNull(valueExtractor, "valueExtractor");
        Object defaultValue;
        if (converter.isLazy()) {
            defaultValue = new NameAndValue.LazyDefaultValue(() ->
                    converter.fromString(nav.defaultValue));
        } else {
            defaultValue = converter.fromString(nav.defaultValue);
        }
        this.nav = new NameAndValue<>(nav.name, nav.required, defaultValue, nav.hasDefaultValue);
    }

    @Override
    public Object resolve(String name, RequestContext ctx) {
        return converter.fromString(valueExtractor.apply(name, ctx));
    }

    @Override
    public NameAndValue<Object> createNameAndValue(Param param) {
        return nav;
    }
}
