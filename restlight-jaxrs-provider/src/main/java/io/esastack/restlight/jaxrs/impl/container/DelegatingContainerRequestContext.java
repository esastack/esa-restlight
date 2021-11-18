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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DelegatingContainerRequestContext implements ContainerRequestContext {

    private final AbstractContainerRequestContext underlying;

    public DelegatingContainerRequestContext(AbstractContainerRequestContext underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public Object getProperty(String name) {
        return underlying.getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return underlying.getPropertyNames();
    }

    @Override
    public void setProperty(String name, Object object) {
        underlying.setProperty(name, object);
    }

    @Override
    public void removeProperty(String name) {
        underlying.removeProperty(name);
    }

    @Override
    public UriInfo getUriInfo() {
        return underlying.getUriInfo();
    }

    @Override
    public Request getRequest() {
        return underlying.getRequest();
    }

    @Override
    public String getMethod() {
        return underlying.getMethod();
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return underlying.getHeaders();
    }

    @Override
    public String getHeaderString(String name) {
        return underlying.getHeaderString(name);
    }

    @Override
    public Date getDate() {
        return underlying.getDate();
    }

    @Override
    public Locale getLanguage() {
        return underlying.getLanguage();
    }

    @Override
    public int getLength() {
        return underlying.getLength();
    }

    @Override
    public MediaType getMediaType() {
        return underlying.getMediaType();
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return underlying.getAcceptableMediaTypes();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return underlying.getAcceptableLanguages();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return underlying.getCookies();
    }

    @Override
    public boolean hasEntity() {
        return underlying.hasEntity();
    }

    @Override
    public InputStream getEntityStream() {
        return underlying.getEntityStream();
    }

    @Override
    public SecurityContext getSecurityContext() {
        return underlying.getSecurityContext();
    }

    @Override
    public void setSecurityContext(SecurityContext context) {
        underlying.setSecurityContext(context);
    }

    @Override
    public void abortWith(Response response) {
        underlying.abortWith(response);
    }

    @Override
    public void setRequestUri(URI requestUri) {
        underlying.setRequestUri(requestUri);
    }

    @Override
    public void setRequestUri(URI baseUri, URI requestUri) {
        underlying.setRequestUri(baseUri, requestUri);
    }

    @Override
    public void setMethod(String method) {
        underlying.setMethod(method);
    }

    @Override
    public void setEntityStream(InputStream input) {
        underlying.setEntityStream(input);
    }
}

