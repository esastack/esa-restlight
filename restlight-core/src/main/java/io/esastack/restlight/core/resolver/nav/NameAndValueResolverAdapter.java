package io.esastack.restlight.core.resolver.nav;

import esa.commons.Checks;
import esa.commons.ObjectUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.server.bootstrap.WebServerException;

import java.util.Optional;
import java.util.function.Supplier;

public class NameAndValueResolverAdapter implements ParamResolver {

    private final NameAndValue<?> nav;
    private final NameAndValueResolver resolver;

    public NameAndValueResolverAdapter(Param param,
                                       NameAndValueResolver resolver) {
        Checks.checkNotNull(param, "param");
        this.resolver = Checks.checkNotNull(resolver, "converter");
        this.nav = getNameAndValue(param, resolver.createNameAndValue(param));
    }

    @Override
    public Object resolve(Param param, RequestContext ctx) {
        Object arg = resolver.resolve(nav.name(), ctx);
        if (arg == null) {
            Supplier<?> defaultValue = nav.defaultValue();
            if (defaultValue != null) {
                arg = defaultValue.get();
            }
            if (nav.required() && arg == null) {
                throw WebServerException.badRequest("Missing required value: " + nav.name());
            }
        }
        return arg;
    }

    private NameAndValue<?> getNameAndValue(Param param, NameAndValue<?> nav) {
        Checks.checkNotNull(nav);
        return updatedNamedValue(param, nav);
    }

    private NameAndValue<?> updatedNamedValue(Param param, NameAndValue<?> nav) {
        String name = nav.name();
        if (name.isEmpty()) {
            name = param.name();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + param.type().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }

        Supplier<?> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            if (!nav.required() && (useObjectDefaultValueIfRequired(param))) {
                defaultValue = () -> defaultValue(param.type());
            } else if (Optional.class.equals(param.type())) {
                defaultValue = Optional::empty;
            }
        }

        return new NameAndValue<>(name, nav.required(), defaultValue, false);
    }

    private boolean useObjectDefaultValueIfRequired(Param param) {
        return !param.isFieldParam();
    }

    private static Object defaultValue(Class<?> type) {
        if (Optional.class.equals(type)) {
            return Optional.empty();
        }

        return ObjectUtils.defaultValue(type);
    }
}
