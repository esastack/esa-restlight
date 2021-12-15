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
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestHeader.
 */
public abstract class AbstractHeaderConverterAdapter extends StrsConverterAdapter {

    @Override
    public ParamResolver createResolver(Param param,
                                        HandlerResolverFactory resolverFactory) {
        if (HttpHeaders.class.equals(param.type())) {
            return new HeadersResolver(param);
        }
        return super.createResolver(param, resolverFactory);
    }

    @Override
    protected BiFunction<String, HttpRequest, Collection<String>> valueExtractor(Param param) {
        return (name, request) -> request.headers().getAll(name);
    }

    private class HeadersResolver extends AbstractNameAndValueParamResolver {

        private HeadersResolver(Param param) {
            super(param);
        }

        @Override
        protected HttpHeaders resolveName(String name, HttpRequest request) {
            return request.headers();
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractHeaderConverterAdapter.this.createNameAndValue(param, (defaultValue, isLazy) -> defaultValue);
        }
    }

}
