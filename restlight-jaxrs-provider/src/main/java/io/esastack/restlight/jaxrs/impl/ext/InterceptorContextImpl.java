/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.impl.ext;

import esa.commons.Checks;
import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.resolver.HttpEntityResolverContext;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.InterceptorContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class InterceptorContextImpl implements InterceptorContext {

    private final HttpEntityResolverContext underlying;

    InterceptorContextImpl(HttpEntityResolverContext underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public Object getProperty(String name) {
        return underlying.context().attrs().attr(AttributeKey.valueOf(name)).get();
    }

    @Override
    public Collection<String> getPropertyNames() {
        List<String> names = new ArrayList<>(underlying.context().attrs().size());
        underlying.context().attrs().forEach((name, value) -> names.add(name.name()));
        return names;
    }

    @Override
    public void setProperty(String name, Object object) {
        underlying.context().attrs().attr(AttributeKey.valueOf(name)).set(object);
    }

    @Override
    public void removeProperty(String name) {
        underlying.context().attrs().attr(AttributeKey.valueOf(name)).remove();
    }

    @Override
    public Annotation[] getAnnotations() {
        return underlying.httpEntity().annotations();
    }

    @Override
    public void setAnnotations(Annotation[] annotations) {
        underlying.httpEntity().annotations(annotations);
    }

    @Override
    public Class<?> getType() {
        return underlying.httpEntity().type();
    }

    @Override
    public void setType(Class<?> type) {
        underlying.httpEntity().type(type);
    }

    @Override
    public Type getGenericType() {
        return underlying.httpEntity().genericType();
    }

    @Override
    public void setGenericType(Type genericType) {
        underlying.httpEntity().genericType(genericType);
    }

    @Override
    public MediaType getMediaType() {
        return MediaTypeUtils.convert(underlying.httpEntity().mediaType());
    }

    @Override
    public void setMediaType(MediaType mediaType) {
        underlying.httpEntity().mediaType(MediaTypeUtils.convert(mediaType));
    }
}

