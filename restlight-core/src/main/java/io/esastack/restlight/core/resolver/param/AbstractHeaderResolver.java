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
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.StrsNameAndValueResolverFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Implementation of {@link StrsNameAndValueResolverFactory} for resolving argument that annotated by
 * the RequestHeader.
 */
public abstract class AbstractHeaderResolver extends StrsNameAndValueResolverFactory {

    private static final NameAndValueResolver.Converter<Collection<String>> HEADERS_CONVERTER =
            (name, ctx, valueProvider) -> {
                if (ctx != null) {
                    return ctx.request().headers();
                }
                //handle when convert defaultValue
                return null;
            };

    @Override
    protected NameAndValueResolver.Converter<Collection<String>> initConverter(
            Param param, BiFunction<Class<?>, Type, StringConverter> converterLookup) {

        if (HttpHeaders.class.equals(param.type())) {
            return HEADERS_CONVERTER;
        }
        return super.initConverter(param, converterLookup);
    }

    @Override
    protected BiFunction<String, RequestContext, Collection<String>> initValueProvider(Param param) {
        return (name, ctx) -> ctx.request().headers().getAll(name);
    }
}
