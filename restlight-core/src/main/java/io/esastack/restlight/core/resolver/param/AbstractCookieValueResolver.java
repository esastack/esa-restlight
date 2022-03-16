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
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverterProvider;
import io.esastack.restlight.core.resolver.nav.NameAndStringValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.server.context.RequestContext;

import java.util.Set;

/**
 * Implementation of {@link NameAndValueResolverFactory} for resolving argument that annotated by the
 * CookieValue
 */
public abstract class AbstractCookieValueResolver extends NameAndValueResolverFactory {

    @Override
    protected NameAndValueResolver createResolver(Param param,
                                                  StringConverterProvider converters) {
        if (Cookie.class.equals(param.type())) {
            return new CookieResolver();
        }

        if (Set.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types != null && types.length == 1 && types[0].equals(Cookie.class)) {
                return new CookiesResolver();
            }
        }

        return new NameAndStringValueResolver(param,
                converters,
                this::extractCookieValue,
                createNameAndValue(param)
        );
    }

    protected abstract NameAndValue<String> createNameAndValue(Param param);

    private String extractCookieValue(String name, RequestContext ctx) {
        Cookie cookie = ctx.request().getCookie(name);
        return cookie == null ? null : cookie.value();
    }

    private class CookieResolver implements NameAndValueResolver {

        @Override
        public Object resolve(String name, RequestContext ctx) {
            return ctx.request().getCookie(name);
        }

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractCookieValueResolver.this.createNameAndValue(param);
        }
    }

    private class CookiesResolver implements NameAndValueResolver {

        @Override
        public Object resolve(String name, RequestContext ctx) {
            return ctx.request().cookies();
        }

        @Override
        public NameAndValue<String> createNameAndValue(Param param) {
            return AbstractCookieValueResolver.this.createNameAndValue(param);
        }
    }
}
