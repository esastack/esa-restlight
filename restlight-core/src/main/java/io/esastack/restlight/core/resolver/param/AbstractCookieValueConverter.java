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
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

import java.util.Set;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the CookieValue
 */
public abstract class AbstractCookieValueConverter extends StrConverterAdapter {

    @Override
    public ParamResolver createResolver(Param param,
                                        HandlerResolverFactory resolverFactory) {
        if (Cookie.class.equals(param.type())) {
            return new CookieResolver(param);
        }

        if (Set.class.equals(param.type())) {
            Class<?>[] types = ClassUtils.retrieveGenericTypes(param.genericType());
            if (types != null && types.length == 1 && types[0].equals(Cookie.class)) {
                return new CookiesResolver(param);
            }
        }
        return super.createResolver(param, resolverFactory);
    }

    protected String extractValue(String name, HttpRequest request) {
        Cookie cookie = request.getCookie(name);
        return cookie == null ? null : cookie.value();
    }

    private abstract class BaseResolver extends AbstractNameAndValueParamResolver {

        private BaseResolver(Param param) {
            super(param);
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractCookieValueConverter.this.createNameAndValue(param, (defaultValue, isLazy) -> defaultValue);
        }
    }

    /**
     * Implementation for resolving argument type of {@link Cookie}
     */
    private class CookieResolver extends BaseResolver {

        private CookieResolver(Param param) {
            super(param);
        }

        @Override
        protected Cookie resolveName(String name, HttpRequest request) {
            return request.getCookie(name);
        }
    }

    /**
     * Implementation for resolving argument type of {@link Set} of {@link Cookie}
     */
    private class CookiesResolver extends BaseResolver {

        private CookiesResolver(Param param) {
            super(param);
        }

        @Override
        protected Set<Cookie> resolveName(String name, HttpRequest request) {
            return request.cookies();
        }
    }
}
