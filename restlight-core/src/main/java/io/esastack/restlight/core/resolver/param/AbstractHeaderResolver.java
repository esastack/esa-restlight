/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.core.resolver.param;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndStringsValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;

import java.util.List;

/**
 * Implementation of {@link NameAndValueResolverFactory} for resolving argument that annotated by
 * the RequestHeader.
 */
public abstract class AbstractHeaderResolver extends NameAndValueResolverFactory {

    @Override
    public NameAndValueResolver createResolver(Param param, HandlerResolverFactory resolverFactory) {
        if (HttpHeaders.class.equals(param.type())) {
            return new HeadersResolver();
        }
        return new NameAndStringsValueResolver(param,
                resolverFactory,
                this::extractHeaderValues,
                createNameAndValue(param));
    }

    private List<String> extractHeaderValues(String name, RequestContext ctx) {
        return ctx.request().headers().getAll(name);
    }

    protected abstract NameAndValue<String> createNameAndValue(Param param);

    private class HeadersResolver implements NameAndValueResolver {

        @Override
        public Object resolve(String name, RequestContext ctx) {
            return ctx.request().headers();
        }

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractHeaderResolver.this.createNameAndValue(param);
        }
    }
}
