package io.esastack.restlight.core.resolver.nav;

import esa.commons.Checks;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NameAndStringValueResolver implements NameAndValueResolver {

    private final StringConverter converter;
    private final BiFunction<String, RequestContext, String> valueExtractor;
    private final NameAndValue<Object> nav;

    public NameAndStringValueResolver(Param param,
                                      HandlerResolverFactory resolverFactory,
                                      BiFunction<String, RequestContext, String> valueExtractor,
                                      NameAndValue<String> nav) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        this.converter = Checks.checkNotNull(resolverFactory.getStringConverter(param.type(),
                param.genericType(),
                param), "converter");
        this.valueExtractor = Checks.checkNotNull(valueExtractor, "valueExtractor");

        Supplier<String> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            this.nav = new NameAndValue<>(nav.name(), nav.required(), null);
        } else {
            this.nav = new NameAndValue<>(nav.name(),
                    nav.required(),
                    () -> converter.fromString(defaultValue.get()),
                    converter.isLazy());
        }
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
