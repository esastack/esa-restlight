package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;
import java.util.Optional;

public abstract class NameAndValueResolverFactory implements ParamResolverProvider, Ordered {

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
            public NameAndValueResolverAdapter createResolver(Param param,
                                                              List<? extends HttpRequestSerializer> serializers) {

                return new NameAndValueResolverAdapter(param,
                        NameAndValueResolverFactory
                                .this.createResolver(param, ctx.resolverFactory().orElse(null)));
            }

            @Override
            public int getOrder() {
                return NameAndValueResolverFactory.this.getOrder();
            }
        });
    }

    public abstract NameAndValueResolver createResolver(Param param, HandlerResolverFactory resolverFactory);

    public abstract boolean supports(Param param);
}
