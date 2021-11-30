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

import esa.commons.ClassUtils;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.jaxrs.impl.core.LinkBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.ResponseBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.UriBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.VariantListBuilderImpl;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RuntimeDelegateImpl extends RuntimeDelegate {

    private final Map<Class<?>, HeaderDelegate<?>> headerDelegates;
    private final Set<Class<?>> nullHeaderDelegateClasses = new HashSet<>();

    public RuntimeDelegateImpl() {
        this.headerDelegates = loadHeaderDelegates();
    }

    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        return new ResponseBuilderImpl();
    }

    @Override
    public Variant.VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException,
            UnsupportedOperationException {
        if (application == null) {
            throw new IllegalArgumentException("application must not be null");
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
        if (nullHeaderDelegateClasses.contains(type)) {
            return null;
        }
        HeaderDelegate<T> delegate = findHeaderDelegateRecursively(type);
        if (delegate == null) {
            nullHeaderDelegateClasses.add(type);
        }
        return delegate;
    }

    @Override
    public Link.Builder createLinkBuilder() {
        return new LinkBuilderImpl();
    }

    @SuppressWarnings("unchecked")
    private <T> HeaderDelegate<T> findHeaderDelegateRecursively(Class<T> type) {
        if (Object.class.equals(type)) {
            return null;
        }
        HeaderDelegate<?> delegate = headerDelegates.get(type);
        if (delegate != null) {
            return (HeaderDelegate<T>) delegate;
        }
        for (Class<?> clazz : type.getInterfaces()) {
            delegate = findHeaderDelegateRecursively(clazz);
            if (delegate != null) {
                return (HeaderDelegate<T>) delegate;
            }
        }
        delegate = findHeaderDelegateRecursively(type.getSuperclass());
        if (delegate != null) {
            return (HeaderDelegate<T>) delegate;
        } else {
            return null;
        }
    }

    private Map<Class<?>, HeaderDelegate<?>> loadHeaderDelegates() {
        Map<Class<?>, HeaderDelegate<?>> delegates = new LinkedHashMap<>();
        SpiLoader.cached(HeaderDelegateFactory.class)
                .getAll()
                .stream().map(HeaderDelegateFactory::headerDelegate)
                .collect(Collectors.toList())
                .forEach(delegate -> delegates.put(ClassUtils.getRawType(ClassUtils.getUserType(delegate)), delegate));
        return delegates;
    }
}

