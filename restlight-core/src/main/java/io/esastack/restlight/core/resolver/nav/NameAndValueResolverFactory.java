package io.esastack.restlight.core.resolver.nav;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.util.Ordered;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class NameAndValueResolverFactory<T> implements ParamResolverProvider, Ordered {

    /**
     * Produces a an optional instance of {@link ParamResolverFactory}.
     *
     * @param ctx deploy context
     * @return Optional value of {@link ParamResolverFactory}.
     */
    @Override
    public Optional<ParamResolverFactory> factoryBean(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new ParamResolverFactory() {

            @Override
            public boolean supports(Param param) {
                return NameAndValueResolverFactory.this.supports(param);
            }

            @Override
            public NameAndValueResolver<T> createResolver(Param param,
                                                          List<? extends HttpRequestSerializer> serializers) {
                return NameAndValueResolverFactory.this.createResolver(param, ctx.resolverFactory().orElse(null));
            }

            @Override
            public int getOrder() {
                return NameAndValueResolverFactory.this.getOrder();
            }
        });
    }

    public NameAndValueResolver<T> createResolver(Param param,
                                                  HandlerResolverFactory resolverFactory) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        BiFunction<Class<?>, Type, StringConverter> converterLookup = (baseType, baseGenericType) ->
                resolverFactory.getStringConverter(param, baseType, baseGenericType);
        NameAndValueResolver.Converter<T> converter = initConverter(param, converterLookup);
        return new NameAndValueResolver<>(param,
                converter,
                initValueProvider(param),
                initNameAndValueCreator(initDefaultValueConverter(converter))
        );
    }

    public abstract boolean supports(Param param);

    protected abstract NameAndValueResolver.Converter<T> initConverter(Param param,
                                                                       BiFunction<Class<?>,
                                                                               Type,
                                                                               StringConverter> converterLookup);

    protected abstract BiFunction<String, RequestContext, T> initValueProvider(Param param);

    protected abstract Function<Param, NameAndValue> initNameAndValueCreator(BiFunction<String,
            Boolean,
            Object> defaultValueConverter);

    protected abstract BiFunction<String,
            Boolean,
            Object> initDefaultValueConverter(NameAndValueResolver.Converter<T> converter);

}
