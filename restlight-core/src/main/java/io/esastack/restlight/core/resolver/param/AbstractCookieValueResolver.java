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

import esa.commons.ClassUtils;
import io.esastack.commons.net.http.Cookie;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.StrNameAndValueResolverFactory;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the CookieValue
 */
public abstract class AbstractCookieValueResolver extends StrNameAndValueResolverFactory {

    private final static NameAndValueResolver.Converter<String> cookieConverter =
            (name, ctx, valueExtractor) -> {
                if (ctx != null) {
                    return ctx.request().getCookie(name);
                }
                //handle when convert defaultValue
                return null;
            };

    private final static NameAndValueResolver.Converter<String> cookiesConverter =
            (name, ctx, valueExtractor) -> {
                if (ctx != null) {
                    return ctx.request().cookies();
                }
                //handle when convert defaultValue
                return null;
            };

    @Override
    protected NameAndValueResolver.Converter<String> initConverter(
            Param param, BiFunction<Class<?>, Type, StringConverter> converterLookup) {

        if (Cookie.class.equals(param.type())) {
            return cookieConverter;
        }

        if (Set.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types != null && types.length == 1 && types[0].equals(Cookie.class)) {
                return cookiesConverter;
            }
        }

        return super.initConverter(param, converterLookup);
    }

    @Override
    protected BiFunction<String, RequestContext, String> initValueExtractor(Param param) {
        return (name, ctx) -> {
            Cookie cookie = ctx.request().getCookie(name);
            return cookie == null ? null : cookie.value();
        };
    }
}
