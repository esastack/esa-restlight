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
import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestHeader.
 */
public abstract class AbstractHeaderArgumentResolver implements ArgumentResolverFactory {

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        if (HttpHeaders.class.isAssignableFrom(param.type())) {
            return new HeadersResolver(param);
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
            return AbstractHeaderArgumentResolver.this.createNameAndValue(param);
        }

    }

    private class StringResolver extends BaseResolver {

        StringResolver(Param param) {
            super(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            return request.getHeader(name);
        }
    }

    private class HeadersResolver extends BaseResolver {

        HeadersResolver(Param param) {
            super(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) {
            return request.headers();
        }
    }

}
