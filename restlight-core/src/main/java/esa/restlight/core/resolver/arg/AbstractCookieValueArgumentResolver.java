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
package esa.restlight.core.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the CookieValue
 */
public abstract class AbstractCookieValueArgumentResolver implements ArgumentResolverFactory {

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        if (Cookie.class.isAssignableFrom(param.type())) {
            return new CookieResolver(param);
        }

        if (Set.class.isAssignableFrom(param.type())) {
            return new CookiesResolver(param);
        }
        return new StringResolver(param);
    }

    protected abstract NameAndValue createNameAndValue(Param param);

    private abstract class BaseResolver extends AbstractNameAndValueArgumentResolver {

        BaseResolver(Param param) {
            super(param);
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractCookieValueArgumentResolver.this.createNameAndValue(param);
        }
    }

    /**
     * Implementation for resolving argument type of {@link String}
     */
    private class StringResolver extends BaseResolver {

        StringResolver(Param param) {
            super(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            Cookie cookie = request.getCookie(name);
            return cookie == null ? null : cookie.value();
        }
    }

    /**
     * Implementation for resolving argument type of {@link Cookie}
     */
    private class CookieResolver extends BaseResolver {

        CookieResolver(Param param) {
            super(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            return request.getCookie(name);
        }
    }

    /**
     * Implementation for resolving argument type of {@link Set} of {@link Cookie}
     */
    private class CookiesResolver extends BaseResolver {

        CookiesResolver(Param param) {
            super(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            return request.cookies();
        }


    }
}
