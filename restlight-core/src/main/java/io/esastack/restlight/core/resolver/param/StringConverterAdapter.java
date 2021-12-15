package io.esastack.restlight.core.resolver.param;

import esa.commons.Checks;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.util.Ordered;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class StringConverterAdapter<T> implements ParamResolverProvider, Ordered {

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
                return StringConverterAdapter.this.supports(param);
            }

            @Override
            public ParamResolver createResolver(Param param, List<? extends HttpRequestSerializer> serializers) {
                Optional<HandlerResolverFactory> resolverFactory = ctx.resolverFactory();
                if (!resolverFactory.isPresent()) {
                    throw new NullPointerException("resolverFactory");
                }
                return StringConverterAdapter.this.createResolver(param, resolverFactory.get());
            }

            @Override
            public int getOrder() {
                return StringConverterAdapter.this.getOrder();
            }
        });
    }

    protected ParamResolver createResolver(Param param,
                                           HandlerResolverFactory resolverFactory) {
        return new Resolver(param, (baseType, baseGenericType) ->
                resolverFactory.getParamConverter(param, baseType, baseGenericType));
    }

    public abstract boolean supports(Param param);

    protected abstract NameAndValue createNameAndValue(Param param,
                                                       BiFunction<String, Boolean, Object> defaultValueConverter);

    protected abstract T extractValue(String name, HttpRequest request);

    protected abstract BiFunction<String, Boolean, Object> createDefaultValueConverter(Function<T, Object> converter);

    protected abstract Function<T, Object> initConverter(Param param,
                                                         BiFunction<Class<?>, Type, StringConverter> converterLookup);

    private class Resolver extends AbstractNameAndValueParamResolver {

        private final Function<T, Object> converter;

        private Resolver(Param param,
                         BiFunction<Class<?>, Type, StringConverter> converterLookup) {
            super(param);
            converter = Checks.checkNotNull(initConverter(param, converterLookup), "converter");
        }

        @Override
        protected Object resolveName(String name, HttpRequest request) {
            return converter.apply(extractValue(name, request));
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return StringConverterAdapter.this.createNameAndValue(param, createDefaultValueConverter(converter));
        }
    }
}
